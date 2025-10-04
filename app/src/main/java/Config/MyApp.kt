// file: MyApp.kt
package com.example.app_tim_viec

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = HashMap<String, String>()
        config["cloud_name"] = getString(R.string.cloud_name)
        MediaManager.init(this, config)
    }
}
