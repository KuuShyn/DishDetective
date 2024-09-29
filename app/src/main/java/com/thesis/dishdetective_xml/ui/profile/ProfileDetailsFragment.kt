package com.thesis.dishdetective_xml.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.dishdetective_xml.Profile
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.util.FirebaseUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileDetailsFragment : Fragment() {

    private val db by lazy { Firebase.firestore }
    private val firebaseAuth by lazy { FirebaseUtil.firebaseAuth }

    // Declare UI elements
    private lateinit var usernameTextView: TextView
    private lateinit var caloriesValueTextView: TextView
    private lateinit var bmiValueTextView: TextView
    private lateinit var idealWeightValueTextView: TextView
    private lateinit var intakeContainer: LinearLayout
    private lateinit var ageValueTextView: TextView
    private lateinit var genderValueTextView: TextView
    private lateinit var heightValueTextView: TextView
    private lateinit var weightValueTextView: TextView
    private lateinit var modeValueTextView: TextView
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_profile_details, container, false)

        // Initialize UI elements
        usernameTextView = view.findViewById(R.id.username)
        caloriesValueTextView = view.findViewById(R.id.caloriesValue)
        bmiValueTextView = view.findViewById(R.id.bmiValue)
        idealWeightValueTextView = view.findViewById(R.id.idealWeightValue)
        intakeContainer = view.findViewById(R.id.intakeContainer)
        ageValueTextView = view.findViewById(R.id.ageValue)
        genderValueTextView = view.findViewById(R.id.genderValue)
        heightValueTextView = view.findViewById(R.id.heightValue)
        weightValueTextView = view.findViewById(R.id.weightValue)
        modeValueTextView = view.findViewById(R.id.modeValue)
        editProfileButton = view.findViewById(R.id.editProfileButton)

        // Fetch profile data
        fetchProfileData()

        return view
    }

    private fun fetchProfileData() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileDocuments = db.collection("users")
                    .document(userId)
                    .collection("profile")
                    .get()
                    .await()

                if (profileDocuments.isEmpty) {
                    Log.d("ProfileDetailsFragment", "No profiles found for user")
                    return@launch
                }

                for (document in profileDocuments) {
                    val profile = Profile(
                        name = document.getString("name") ?: "",
                        age = document.getLong("age")?.toInt() ?: 0,
                        gender = document.getString("gender")?.lowercase() ?: "male",
                        heightFeet = document.getLong("heightFeet")?.toInt() ?: 0,
                        heightInches = document.getLong("heightInches")?.toInt() ?: 0,
                        weight = document.getDouble("weight") ?: 0.0,
                        isAnemiaChecked = document.getBoolean("isAnemiaChecked") ?: false,
                        isCardioChecked = document.getBoolean("isCardioChecked") ?: false
                    )

                    // Calculate height in cm
                    val totalHeightIn = profile.heightFeet * 12 + profile.heightInches
                    val heightCm = totalHeightIn * 2.54

                    // Compute IBW, BMI, TER, and nutrient intakes
                    val ibw = computeIbw(profile.age, heightCm)
                    val (bmi, bmiCategory) = computeBmi(profile.weight, heightCm)
                    val ter = computeTer(ibw)
                    val weightChangeMode = defineWeightChangeMode(bmiCategory)

                    // Pass the dynamic gender for nutrient calculations
                    val recommendedSodiumIntake = calculateNutrientIntake(profile.age, profile.gender, "sodium")
                    val recommendedIronIntake = calculateNutrientIntake(profile.age, profile.gender, "iron")
                    val recommendedCholesterolIntake = calculateNutrientIntake(profile.age, profile.gender, "cholesterol", bmiCategory)

                    // Determine recommendation based on cardio/anemia check
                    val recommendation = when {
                        profile.isCardioChecked -> "cardiovascular"
                        profile.isAnemiaChecked -> "anemia"
                        else -> ""
                    }

                    // Update UI on the main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        updateUI(
                            profile.name,
                            ter,
                            bmi,
                            bmiCategory,
                            ibw,
                            profile.age,
                            profile.gender,
                            profile.heightFeet,
                            profile.heightInches,
                            profile.weight,
                            weightChangeMode,
                            recommendation,
                            recommendedIronIntake,
                            recommendedSodiumIntake,
                            recommendedCholesterolIntake
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileDetailsFragment", "Error fetching profiles: ${e.message}")
            }
        }
    }

    private fun updateUI(
        name: String,
        ter: Int,
        bmi: Double,
        bmiCategory: String,
        ibw: Double,
        age: Int,
        gender: String,
        heightFt: Int,
        heightIn: Int,
        weight: Double,
        weightChangeMode: String,
        recommendation: String,
        recommendedIronIntake: String,
        recommendedSodiumIntake: String,
        recommendedCholesterolIntake: String
    ) {
        // Update TextViews with the fetched data
        usernameTextView.text = name
        caloriesValueTextView.text = "$ter kcal"
        bmiValueTextView.text = "($bmiCategory)"
        idealWeightValueTextView.text = "${ibw} kg"
        ageValueTextView.text = "Age: $age"
        genderValueTextView.text = "Gender: ${gender.capitalize()}"
        heightValueTextView.text = "Height: $heightFt ft, $heightIn in"
        weightValueTextView.text = "Weight: ${weight} kg"
        modeValueTextView.text = "Mode: ${weightChangeMode.capitalize()} weight"

        // Clear previous intake values and add new ones
        intakeContainer.removeAllViews()
        if (recommendation == "cardiovascular") {
            addIntakeTextView("SODIUM INTAKE PER DAY", recommendedSodiumIntake, true)
            addIntakeTextView("CHOLESTEROL INTAKE PER DAY", recommendedCholesterolIntake, true)
        } else if (recommendation == "anemia") {
            addIntakeTextView("IRON INTAKE PER DAY", recommendedIronIntake)
        }
    }

    private fun addIntakeTextView(title: String, value: String, isCardio: Boolean = false) {
        val titleTextView = TextView(requireContext()).apply {
            text = title
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#B0B0B0"))
            setPadding(0, 12, 0, 0)
        }

        val valueTextView = TextView(requireContext()).apply {
            "$value mg".also { text = it }
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#00b389"))
            setPadding(0, 12, 0, 0)
        }

        // Apply extra padding if it's "cardio" (i.e., two views to be displayed)
        if (isCardio) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 16, 0, 16) // Add extra top and bottom margin for cardio views
            titleTextView.layoutParams = params
            valueTextView.layoutParams = params
        }

        intakeContainer.addView(titleTextView)
        intakeContainer.addView(valueTextView)
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



private fun logProfile(
        name: String,
        ter: Int,
        bmi: Double,
        bmiCategory: String,
        ibw: Double,
        age: Int,
        gender: String,
        heightFt: Int,
        heightIn: Int,
        weight: Double,
        weightChangeMode: String,
        recommendation: String,
        recommendedIronIntake: String,
        recommendedSodiumIntake: String,
        recommendedCholesterolIntake: String
    ) {
        Log.d("ProfileDetailsFragment", "\nPROFILE")
        Log.d("ProfileDetailsFragment", "$name")
        Log.d("ProfileDetailsFragment", "Calories per day: ${ter} kcal per day")
        Log.d("ProfileDetailsFragment", "BMI: $bmi")
        Log.d("ProfileDetailsFragment", "BMI Category: $bmiCategory")
        Log.d("ProfileDetailsFragment", "Ideal Body Weight: $ibw kg")

        if (recommendation == "cardiovascular") {
            Log.d("ProfileDetailsFragment", "Sodium Intake per day: $recommendedSodiumIntake mg")
            Log.d(
                "ProfileDetailsFragment",
                "Cholesterol Intake per day: $recommendedCholesterolIntake mg"
            )
        } else if (recommendation == "anemia") {
            Log.d("ProfileDetailsFragment", "Iron Intake per day: $recommendedIronIntake mg")
        }

        Log.d("ProfileDetailsFragment", "\nUser Summary:")
        Log.d("ProfileDetailsFragment", "Age: $age")
        Log.d("ProfileDetailsFragment", "Gender: ${gender.uppercase()}")
        Log.d("ProfileDetailsFragment", "Height: $heightFt ft, $heightIn in")
        Log.d("ProfileDetailsFragment", "Weight: ${weight} kg")
        Log.d("ProfileDetailsFragment", "Mode: ${weightChangeMode.uppercase()} weight")
    }
}

