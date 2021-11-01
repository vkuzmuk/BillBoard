package com.raywenderlich.billboard.model


import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val database = Firebase.database("https://billboard-cba46-default-rtdb.europe-west1.firebasedatabase.app")
    val ref = database.getReference("main")
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishListener: FinishWorkListener) {
        if(auth.uid != null) {
            ref.child(ad.key ?: "empty")
           .child(auth.uid!!)
           .child("ad")
           .setValue(ad)
           .addOnCompleteListener {
               finishListener.onFinish()
           }
       }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = ref.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)

    }

    fun getAllAds(readDataCallback: ReadDataCallback?) {
        val query = ref.orderByChild(auth.uid + "/ad/price")
        readDataFromDb(query, readDataCallback)

    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if(ad.key == null || ad.uid == null) return
        ref.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if(it.isSuccessful) listener.onFinish()

        }
    }

   private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    val ad = item.children.iterator().next().child("ad").getValue(Ad :: class.java)
                    if (ad != null) adArray.add(ad)
                }
                readDataCallback?.readData(adArray)

            }
            override fun onCancelled(error: DatabaseError) { }

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish()

    }

}