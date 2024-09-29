// File: ProfileInputFragment.kt
package com.thesis.dishdetective_xml.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
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
    private lateinit var anemiaSwitch: SwitchMaterial
    private lateinit var cardioSwitch: SwitchMaterial
    private lateinit var saveButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var genderMale: RadioButton
    private lateinit var genderFemale: RadioButton

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
        anemiaSwitch = view.findViewById(R.id.anemiaSwitch)
        cardioSwitch = view.findViewById(R.id.cardioSwitch)
        saveButton = view.findViewById(R.id.saveButton)
        genderMale = view.findViewById(R.id.genderMale)
        genderFemale = view.findViewById(R.id.genderFemale)

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
        val isAnemiaChecked = anemiaSwitch.isChecked
        val isCardioChecked = cardioSwitch.isChecked
        val gender = if (genderMale.isChecked) "male" else if (genderFemale.isChecked) "female" else ""

        val userId = firebaseAuth.uid ?: return

        val profileData = hashMapOf(
            "name" to name,
            "age" to age,
            "weight" to weight,
            "heightFeet" to heightFeet,
            "heightInches" to heightInches,
            "isAnemiaChecked" to isAnemiaChecked,
            "isCardioChecked" to isCardioChecked,
            "gender" to gender
        )

        firestore.collection("users").document(userId)
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