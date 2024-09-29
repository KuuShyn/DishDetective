package com.thesis.dishdetective_xml.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val firebaseDatabase by lazy { FirebaseDatabase.getInstance() }
}