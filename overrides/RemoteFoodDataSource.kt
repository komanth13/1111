package com.example.data.network

import com.example.data.model.FoodItem

/**
 * Remote product lookup seam. The app can add more providers later without coupling
 * the UI or repository contract to a specific public product database.
 */
interface RemoteFoodDataSource {
    suspend fun findByBarcode(barcode: String): FoodItem?
}