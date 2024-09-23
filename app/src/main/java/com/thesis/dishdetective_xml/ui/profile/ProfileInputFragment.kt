// File: ProfileInputFragment.kt
package com.thesis.dishdetective_xml.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.SignInActivity

class ProfileInputFragment : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var ageInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private lateinit var heightFeetInput: TextInputEditText
    private lateinit var heightInchesInput: TextInputEditText
    private lateinit var weightGoalInput: TextInputEditText
    private lateinit var weightChangePerWeekInput: TextInputEditText
    private lateinit var anemiaSwitch: SwitchMaterial
    private lateinit var osteoporosisSwitch: SwitchMaterial
    private lateinit var saveButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_profile_input, container, false)

        nameInput = view.findViewById(R.id.nameInput)
        ageInput = view.findViewById(R.id.ageInput)
        weightInput = view.findViewById(R.id.weightInput)
        heightFeetInput = view.findViewById(R.id.heightFeetInput)
        heightInchesInput = view.findViewById(R.id.heightInchesInput)
        weightGoalInput = view.findViewById(R.id.weightGoalInput)
        weightChangePerWeekInput = view.findViewById(R.id.weightChangePerWeekInput)
        anemiaSwitch = view.findViewById(R.id.anemiaSwitch)
        osteoporosisSwitch = view.findViewById(R.id.osteoporosisSwitch)
        saveButton = view.findViewById(R.id.saveButton)

        // Retrieve the arguments
        val email = arguments?.getString("email")
        val pass = arguments?.getString("pass")

        // Bind save button
        saveButton.setOnClickListener {
            firebaseAuth.createUserWithEmailAndPassword(email!!, pass!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileInputFragment", "User created successfully")
                        saveProfileData()

                        val intent = Intent(activity, SignInActivity::class.java)
                        startActivity(intent)
                        activity?.finish()

                    } else {
                        Log.w("ProfileInputFragment", "User creation failed", task.exception)
                    }
                }
        }

        return view
    }

    private fun saveProfileData() {
        val name = nameInput.text.toString()
        val age = ageInput.text.toString().toIntOrNull()
        val weight = weightInput.text.toString().toFloatOrNull()
        val heightFeet = heightFeetInput.text.toString().toIntOrNull()
        val heightInches = heightInchesInput.text.toString().toIntOrNull()
        val weightGoal = weightGoalInput.text.toString()
        val weightChangePerWeek = weightChangePerWeekInput.text.toString().toFloatOrNull()
        val isAnemiaChecked = anemiaSwitch.isChecked
        val isOsteoporosisChecked = osteoporosisSwitch.isChecked

        val userId = firebaseAuth.uid ?: return


        val profileData = hashMapOf(
            "name" to name,
            "age" to age,
            "weight" to weight,
            "heightFeet" to heightFeet,
            "heightInches" to heightInches,
            "weightGoal" to weightGoal,
            "weightChangePerWeek" to weightChangePerWeek,
            "isAnemiaChecked" to isAnemiaChecked,
            "isOsteoporosisChecked" to isOsteoporosisChecked
        )

        firestore.collection("users").document(userId.toString())
            .collection("profile")
            .add(profileData)
            .addOnSuccessListener {
                Log.d("ProfileInputFragment", "Profile data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("ProfileInputFragment", "Error saving profile data", e)
            }
    }
}