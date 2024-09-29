package com.thesis.dishdetective_xml

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.thesis.dishdetective_xml.util.FirebaseUtil.firebaseDatabase

object FoodRepository {
    private val _foodList = mutableListOf<Food>()
    private val _dishList = mutableListOf<Food>()
    val foodList: List<Food> get() = _foodList
    val dishList: List<Food> get() = _dishList

    fun fetchAllFoods(onComplete: (List<Food>) -> Unit, onError: (String) -> Unit) {
        val database = firebaseDatabase.reference.child("foods")
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

   fun fetchAllDishes(onComplete: (List<Food>) -> Unit, onError: (String) -> Unit) {
        val database = firebaseDatabase.reference.child("dishes")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dishList = mutableListOf<Food>()
                for (dishSnapshot in dataSnapshot.children) {
                    val dish = dishSnapshot.getValue(Food::class.java)
                    dish?.let { dishList.add(it) }
                }
                _dishList.clear()
                _dishList.addAll(dishList)
                onComplete(dishList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase RDB Error:", "loadPost:onCancelled", databaseError.toException())
                onError("Failed to load data: ${databaseError.message}")
            }
        })
    }
}
