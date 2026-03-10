package com.weavyr

import android.app.Application
import com.cloudinary.android.MediaManager

class WeavyrApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "de8zkzele" // Replace with your actual cloud name
        )
        MediaManager.init(this, config)
    }
}