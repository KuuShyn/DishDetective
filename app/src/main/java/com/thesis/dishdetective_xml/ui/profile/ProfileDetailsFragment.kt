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
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.ProfileRec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileDetailsFragment : Fragment() {

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

        // Fetch and calculate profile data using ProfileReco
        fetchAndDisplayProfileData()

        return view
    }

    private fun fetchAndDisplayProfileData() {
        val cachedProfile = ProfileRec.profileDetail.firstOrNull()
        cachedProfile?.let { profileResults ->
            // Update the UI with cached data
            updateUI(
                profileResults.name,
                profileResults.ter,
                profileResults.bmi,
                profileResults.bmiCategory,
                profileResults.ibw,
                profileResults.age,
                profileResults.gender,
                profileResults.heightFt,
                profileResults.heightIn,
                profileResults.weight,
                profileResults.weightChangeMode,
                profileResults.recommendation,
                profileResults.recommendedIronIntake,
                profileResults.recommendedSodiumIntake,
                profileResults.recommendedCholesterolIntake
            )
        } ?: Log.d("ProfileDetailsFragment", "No cached profile available")

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
        bmiValueTextView.text = "$bmiCategory"
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
            textSize = 16f
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
}
