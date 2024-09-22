package com.thesis.dishdetective_xml

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class DishDetectiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}