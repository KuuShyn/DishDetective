// File: ProfileFragment.kt
package com.thesis.dishdetective_xml.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.thesis.dishdetective_xml.UserPro
import com.thesis.dishdetective_xml.UserProfileManager
import com.thesis.dishdetective_xml.databinding.FragmentProfileInputBinding


class ProfileInputFragment : Fragment() {

    private var _binding: FragmentProfileInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userProfile = UserProfileManager.getUserProfile()
        userProfile?.let {
            binding.ageInput.setText(it.age.toString())
            binding.weightInput.setText(it.weight.toString())
            binding.heightFeetInput.setText(it.heightFeet.toString())
            binding.heightInchesInput.setText(it.heightInches.toString())
            binding.genderInput.setText(it.gender)
            binding.weightGoalInput.setText(it.weightGoal)
            binding.weightChangePerWeekInput.setText(it.weightChangePerWeek.toString())
        }

        binding.saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val age = binding.ageInput.text.toString().toIntOrNull()
        val weight = binding.weightInput.text.toString().toFloatOrNull()
        val heightFeet = binding.heightFeetInput.text.toString().toIntOrNull()
        val heightInches = binding.heightInchesInput.text.toString().toIntOrNull()
        val gender = binding.genderInput.text.toString()
        val weightGoal = binding.weightGoalInput.text.toString()
        val weightChangePerWeek = binding.weightChangePerWeekInput.text.toString().toFloatOrNull()

        if (age != null && weight != null && heightFeet != null && heightInches != null && weightChangePerWeek != null) {
            val userProfile = UserPro(
                age = age,
                weight = weight,
                heightFeet = heightFeet,
                heightInches = heightInches,
                gender = gender,
                weightGoal = weightGoal,
                weightChangePerWeek = weightChangePerWeek

            )
            UserProfileManager.setUserProfile(userProfile)
            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}