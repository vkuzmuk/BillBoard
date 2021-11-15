package com.raywenderlich.billboard.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.raywenderlich.billboard.adapters.ImageAdapter
import com.raywenderlich.billboard.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.io.InputStream
import java.util.ArrayList

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResize(uris: ArrayList<Uri>, act: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()

            for (n in uris.indices) {
                val size = getImageSize(uris[n], act)
                val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

                if (imageRatio > 1) {

                    if (size[WIDTH] > MAX_IMAGE_SIZE) {

                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))

                    } else {

                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }

                } else {

                    if (size[HEIGHT] > MAX_IMAGE_SIZE) {

                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))

                    } else {

                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }
                }
            }
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList
                        .add(
                            Picasso.get().load(uris[i])
                                .resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get()
                        )
                }
            }

            return@withContext bitmapList
        }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {
            val bitmapList = ArrayList<Bitmap>()
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(Picasso.get().load(uris[i]).get())
                }
            }
            return@withContext bitmapList
        }

     fun fillImageArray(ad: Ad, adapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = getBitmapFromUris(listUris)
            adapter.update(bitmapList as ArrayList<Bitmap>)
        }
    }

}