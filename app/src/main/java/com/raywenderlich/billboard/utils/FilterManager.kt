package com.raywenderlich.billboard.utils

import com.raywenderlich.billboard.model.Ad
import com.raywenderlich.billboard.model.AdFilter

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
}