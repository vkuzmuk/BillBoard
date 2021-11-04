package com.raywenderlich.billboard.act

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.raywenderlich.billboard.MainActivity
import com.raywenderlich.billboard.R
import com.raywenderlich.billboard.adapters.ImageAdapter
import com.raywenderlich.billboard.model.Ad
import com.raywenderlich.billboard.model.DbManager
import com.raywenderlich.billboard.databinding.ActivityEditAdcBinding
import com.raywenderlich.billboard.dialogs.DialogSpinnerHelper
import com.raywenderlich.billboard.frag.FragmentCloseInterface
import com.raywenderlich.billboard.frag.ImageListFrag
import com.raywenderlich.billboard.utils.CityHelper
import com.raywenderlich.billboard.utils.ImagePicker

class EditAdcAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdcBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdcBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        checkEditState()
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if(isEditState){
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if(ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState():Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(rootElement) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        edTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithDelivery.isChecked = ad.withDelivery.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)

    }

    private fun init() {
        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }


    //OnClicks
    fun onClickSelectCountry(view: View) {
        val listCountry =  CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvCountry)
        if(rootElement.tvCity.toString() != getString(R.string.select_city)) {
            rootElement.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = rootElement.tvCountry.text.toString()
        if(selectedCountry != getString(R.string.select_country)) {
        val listCity =  CityHelper.getAllCities(selectedCountry,this)
        dialog.showSpinnerDialog(this, listCity, rootElement.tvCity)
        } else  {
            Toast.makeText(this, R.string.no_country_selected, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {
            val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
            dialog.showSpinnerDialog(this, listCat, rootElement.tvCat)

    }

    fun onClickGetImages(view: View) {
        if(imageAdapter.mainArray.size == 0) {
            ImagePicker.launcher(this, 3)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) {
        val adTemp = fillAd()
        if(isEditState) {
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())
        } else {
            dbManager.publishAd(adTemp, onPublishFinish())
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener{

            override fun onFinish() {
                finish()
            }

        }
    }

    private fun fillAd(): Ad {
        val ad: Ad
        rootElement.apply {
            ad = Ad(tvCountry.text.toString(),
                    tvCity.text.toString(),
                    edTel.text.toString(),
                    edIndex.text.toString(),
                    checkBoxWithDelivery.isChecked.toString(),
                    tvCat.text.toString(),
                    edTitle.text.toString(),
                    edPrice.text.toString(),
                    edDescription.text.toString(),
                    dbManager.db.push().key,
                    "0",
                    dbManager.auth.uid)
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseImageFragment(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this, newList)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
}