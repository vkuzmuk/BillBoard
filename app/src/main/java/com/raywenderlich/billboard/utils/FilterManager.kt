package com.raywenderlich.billboard.utils

import com.raywenderlich.billboard.model.Ad
import com.raywenderlich.billboard.model.AdFilter
import java.lang.StringBuilder

object FilterManager {
    fun createFilter(ad: Ad): AdFilter {
        return AdFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withDelivery}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withDelivery}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withDelivery}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withDelivery}_${ad.time}",
            "${ad.category}_${ad.withDelivery}_${ad.time}",

            "${ad.country}_${ad.withDelivery}_${ad.time}",
            "$${ad.country}_${ad.city}_${ad.withDelivery}_${ad.time}",
            "$${ad.country}_${ad.city}_${ad.index}_${ad.withDelivery}_${ad.time}",
            "${ad.index}_${ad.withDelivery}_${ad.time}",
            "${ad.withDelivery}_${ad.time}"
        )
    }
    fun getFilter(filter: String): String {
        val sBuilder = StringBuilder()
        val tempArray = filter.split("_")
        if (tempArray[0] != "empty") sBuilder.append("country_")
        if (tempArray[1] != "empty") sBuilder.append("city_")
        if (tempArray[2] != "empty") sBuilder.append("index_")
        sBuilder.append("withSent_time")
        return sBuilder.toString()
    }
}



