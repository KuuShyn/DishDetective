package com.thesis.dishdetective_xml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            // User is logged in, redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        finish() // Close AuthCheckActivity
    }
}