package com.thesis.dishdetective_xml

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.dishdetective_xml.util.FirebaseUtil
import com.thesis.dishdetective_xml.util.FirebaseUtil.firebaseAuth
import com.thesis.dishdetective_xml.util.FirebaseUtil.firestore
import kotlinx.coroutines.tasks.await

data class ProfileResults(
    val name: String,
    val ter: Int,
    val bmi: Double,
    val bmiCategory: String,
    val ibw: Double,
    val age: Int,
    val gender: String,
    val heightFt: Int,
    val heightIn: Int,
    val weight: Double,
    val weightChangeMode: String,
    val recommendation: String,
    val recommendedIronIntake: String,
    val recommendedSodiumIntake: String,
    val recommendedCholesterolIntake: String
)

object ProfileRec {


    private val _profileDetail = mutableListOf<ProfileResults>()
    val profileDetail: List<ProfileResults> get() = _profileDetail

    suspend fun fetchProfileData(onComplete: (ProfileResults) -> Unit, onError: (String) -> Unit) {
        val profile = fetchProfile()
        try {
            profile?.let {
                val profileResults = calculateProfileData(profile)
                _profileDetail.clear()
                _profileDetail.add(profileResults)
                onComplete(profileResults)
            }
        } catch (e: Exception) {
            onError("Error fetching profiles: ${e.message}")
        }
    }

    private suspend fun fetchProfile(): Profile? {
        val userId = firebaseAuth.currentUser?.uid ?: return null
        val db = firestore

        try {
            val profileDocuments = db.collection("users")
                .document(userId)
                .collection("profile")
                .get()
                .await()

            if (profileDocuments.isEmpty) {
                return null
            }

            val document = profileDocuments.firstOrNull() ?: return null

            return Profile(
                name = document.getString("name") ?: "",
                age = document.getLong("age")?.toInt() ?: 0,
                gender = document.getString("gender")?.lowercase() ?: "male",
                heightFeet = document.getLong("heightFeet")?.toInt() ?: 0,
                heightInches = document.getLong("heightInches")?.toInt() ?: 0,
                weight = document.getDouble("weight") ?: 0.0,
                isAnemiaChecked = document.getBoolean("isAnemiaChecked") ?: false,
                isCardioChecked = document.getBoolean("isCardioChecked") ?: false
            )



        } catch (e: Exception) {
            throw Exception("Error fetching profile: ${e.message}")
        }
    }

    private fun calculateProfileData(profile: Profile): ProfileResults {
        val totalHeightIn = profile.heightFeet * 12 + profile.heightInches
        val heightCm = totalHeightIn * 2.54

        val ibw = computeIbw(profile.age, heightCm)
        val (bmi, bmiCategory) = computeBmi(profile.weight, heightCm)
        val ter = computeTer(ibw)
        val weightChangeMode = defineWeightChangeMode(bmiCategory)

        val recommendedSodiumIntake = calculateNutrientIntake(profile.age, profile.gender, "sodium")
        val recommendedIronIntake = calculateNutrientIntake(profile.age, profile.gender, "iron")
        val recommendedCholesterolIntake = calculateNutrientIntake(
            profile.age, profile.gender, "cholesterol", bmiCategory
        )

        val recommendation = if (profile.isCardioChecked) {
            "cardiovascular"
        } else if (profile.isAnemiaChecked) {
            "anemia"
        } else {
            ""
        }
        Log.d("ProfileRec", "Calculated Profile Data")

        return ProfileResults(
            name = profile.name,
            ter = ter,
            bmi = bmi,
            bmiCategory = bmiCategory,
            ibw = ibw,
            age = profile.age,
            gender = profile.gender,
            heightFt = profile.heightFeet,
            heightIn = profile.heightInches,
            weight = profile.weight,
            weightChangeMode = weightChangeMode,
            recommendation = recommendation,
            recommendedIronIntake = recommendedIronIntake,
            recommendedSodiumIntake = recommendedSodiumIntake,
            recommendedCholesterolIntake = recommendedCholesterolIntake
        )

    }

    private fun computeIbw(age: Int, heightCm: Double): Double {
        return if (age in 1..12) {
            (age * 2) + 8.0
        } else {
            val x = heightCm - 100
            val ibw = x - (0.10 * x)
            Math.round(ibw).toDouble()
        }
    }

    private fun computeBmi(weightKg: Double, heightCm: Double): Pair<Double, String> {
        val heightM = heightCm / 100
        val bmiIndex = weightKg / (heightM * heightM)

        val bmiCategory = when {
            bmiIndex < 18.5 -> "Underweight"
            bmiIndex in 18.5..22.9 -> "Normal"
            bmiIndex in 23.0..24.9 -> "Overweight"
            else -> "Obese"
        }

        return Pair(bmiIndex, bmiCategory)
    }

    private fun computeTer(ibw: Double): Int {
        return (ibw * 30).toInt()
    }

    private fun defineWeightChangeMode(bmiCategory: String): String {
        return when (bmiCategory) {
            "Underweight" -> "gain"
            "Overweight", "Obese" -> "lose"
            else -> "maintain"
        }
    }

    private fun calculateNutrientIntake(
        age: Int,
        sex: String,
        nutrient: String,
        bmiCategory: String? = null
    ): String {
        val intakeTables = mapOf(
            "sodium" to mapOf(
                1..2 to mapOf("male" to 225, "female" to 225),
                3..5 to mapOf("male" to 300, "female" to 300),
                6..9 to mapOf("male" to 400, "female" to 400),
                10..12 to mapOf("male" to 500, "female" to 500),
                13..15 to mapOf("male" to 500, "female" to 500),
                16..18 to mapOf("male" to 500, "female" to 500),
                19..29 to mapOf("male" to 500, "female" to 500),
                30..49 to mapOf("male" to 500, "female" to 500),
                50..59 to mapOf("male" to 500, "female" to 500),
                60..69 to mapOf("male" to 500, "female" to 500),
                70..100 to mapOf("male" to 500, "female" to 500)
            ),
            "iron" to mapOf(
                1..2 to mapOf("male" to 8, "female" to 8),
                3..5 to mapOf("male" to 9, "female" to 9),
                6..9 to mapOf("male" to 10, "female" to 9),
                10..12 to mapOf("male" to 12, "female" to 20),
                13..15 to mapOf("male" to 19, "female" to 28),
                16..18 to mapOf("male" to 14, "female" to 28),
                19..29 to mapOf("male" to 12, "female" to 28),
                30..49 to mapOf("male" to 12, "female" to 28),
                50..59 to mapOf("male" to 12, "female" to 10),
                60..69 to mapOf("male" to 12, "female" to 10),
                70..100 to mapOf("male" to 12, "female" to 10)
            ),
            "cholesterol" to mapOf(
                "heart_risk" to 200,
                "no_heart_risk" to 300
            )
        )

        return when (nutrient) {
            "sodium", "iron" -> {
                val intakeTable = intakeTables[nutrient] as? Map<IntRange, Map<String, Int>>
                val intake = intakeTable?.entries?.firstOrNull { age in it.key }?.value?.get(sex)
                intake?.toString() ?: "Invalid intake for $nutrient"
            }

            "cholesterol" -> {
                if (bmiCategory in listOf("Overweight", "Obese")) {
                    intakeTables["cholesterol"]?.get("heart_risk").toString()
                } else {
                    intakeTables["cholesterol"]?.get("no_heart_risk").toString()
                }
            }

            else -> "Invalid nutrient type"
        }
    }
}
