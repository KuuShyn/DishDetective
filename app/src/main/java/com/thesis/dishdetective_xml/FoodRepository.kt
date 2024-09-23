package com.thesis.dishdetective_xml

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thesis.dishdetective_xml.ui.recipe_analyzer.Food

object FoodRepository {
    private val _foodList = mutableListOf<Food>()
    val foodList: List<Food> get() = _foodList

    fun fetchAllFoods(onComplete: (List<Food>) -> Unit, onError: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("foods")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val foodList = mutableListOf<Food>()
                for (foodSnapshot in dataSnapshot.children) {
                    val food = foodSnapshot.getValue(Food::class.java)
                    food?.let { foodList.add(it) }
                }
                _foodList.clear()
                _foodList.addAll(foodList)
                onComplete(foodList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase RDB Error:", "loadPost:onCancelled", databaseError.toException())
                onError("Failed to load data: ${databaseError.message}")
            }
        })
    }
}
