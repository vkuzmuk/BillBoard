package com.raywenderlich.billboard.model


import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.raywenderlich.billboard.utils.FilterManager

class DbManager {
    val database =
        Firebase.database("https://billboard-cba46-default-rtdb.europe-west1.firebasedatabase.app")
    val db = database.getReference(MAIN_NODE)

    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishListener: FinishWorkListener) {
        if (auth.uid != null)
            db.child(ad.key ?: "empty")
                .child(auth.uid!!)
                .child(AD_NODE)
                .setValue(ad)
                .addOnCompleteListener {
                    val adFilter = FilterManager.createFilter(ad)
                    db.child(ad.key ?: "empty")
                        .child(FILTER_NODE)
                        .setValue(adFilter)
                        .addOnCompleteListener {
                            finishListener.onFinish()
                        }
                }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFirstPage(filter: String, readDataCallback: ReadDataCallback?) {
        val query = if (filter.isEmpty()) {
            db.orderByChild("/adFilter/time").limitToLast(ADS_LIMIT)
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsByFilterFirstPage(tempFilter: String): Query {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    }

    fun getAllAdsNextPage(time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/adFilter/time").endBefore(time).limitToLast(ADS_LIMIT)
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsByFilterNextPage(filter, time, readDataCallback)
        }

    }

    private fun getAllAdsByFilterNextPage(tempFilter: String, time: String, readDataCallBack: ReadDataCallback?) {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_$time").limitToLast(ADS_LIMIT)
        readNextPageFromDb(query, filter, orderBy, readDataCallBack)
    }

    fun getAllAdsFromCatFirstPage(
        cat: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        val query = if (filter.isEmpty()) {
            db.orderByChild("/adFilter/cat_time")
                .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(ADS_LIMIT)
        } else {
            getAllAdsFromCatByFilterFirstPage(cat, filter)
        }
        readDataFromDb(query, readDataCallback)

    }

    fun getAllAdsFromCatByFilterFirstPage(cat: String, tempFilter: String): Query {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    }


    fun getAllAdsFromCatNextPage(cat: String, time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/adFilter/cat_time")
                .endBefore(cat + "_" + time).limitToLast(ADS_LIMIT)
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readDataCallback)
        }
    }

    private fun getAllAdsFromCatByFilterNextPage(cat: String, time: String, tempFilter: String, readDataCallback: ReadDataCallback?) {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(ADS_LIMIT)
        readNextPageFromDb(query, filter, orderBy, readDataCallback)
    }


    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()

        }
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewsCounter.toInt()
        counter++

        if (auth.uid != null) {
            db.child(ad.key ?: "empty")
                .child(INFO_NODE)
                .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))
        }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) {
            removeFromFavs(ad, listener)
        } else {
            addToFavs(ad, listener)
        }
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NOTE)
                    .child(uid)
                    .setValue(uid)
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NOTE)
                    .child(uid)
                    .removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val favCounter = item.child(FAVS_NOTE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NOTE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null) adArray.add(ad!!)
                }
                readDataCallback?.readData(adArray)

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun readNextPageFromDb(query: Query, filter: String, orderBy: String, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()
                    val favCounter = item.child(FAVS_NOTE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NOTE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)
                }
                readDataCallback?.readData(adArray)

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }


    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish()

    }

    companion object {
        const val AD_NODE = "ad"
        const val INFO_NODE = "info"
        const val FILTER_NODE = "adFilter"
        const val MAIN_NODE = "main"
        const val FAVS_NOTE = "favs"
        const val ADS_LIMIT = 2
    }

}