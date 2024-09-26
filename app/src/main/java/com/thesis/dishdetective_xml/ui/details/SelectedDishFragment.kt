// File: SelectedDishDetailsFragment.kt
package com.thesis.dishdetective_xml.ui.details

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.thesis.dishdetective_xml.databinding.FragmentSelectedDishBinding

class SelectedDishFragment : DialogFragment() {
    private var _binding: FragmentSelectedDishBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()

        // Get the screen width
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        // Set the dialog width to match the screen width and set the height as per your requirement
        dialog?.window?.setLayout(
            screenWidth, // Width
            ViewGroup.LayoutParams.WRAP_CONTENT  // Height
        )

        // Set the dialog gravity to bottom
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the arguments
        val food = arguments?.getString(ARG_FOOD)
        val caloriesPerServing = arguments?.getString(ARG_CALORIES_PER_SERVING)
        val ironIntakePerServing = arguments?.getString(ARG_IRON_INTAKE_PER_SERVING)

        val caloriesReason = arguments?.getString(ARG_CALORIES_REASON)
        val ironIntakeReason = arguments?.getString(ARG_IRON_INTAKE_REASON)

        // Set the text of the TextViews
        binding.foodValue.text = food
        binding.caloriesValue.text = caloriesPerServing
        binding.ironValue.text = ironIntakePerServing

        // Set the text of the TextViews
        binding.caloriesReason.text = caloriesReason
        binding.ironReason.text = ironIntakeReason
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FOOD = "arg_food"
        private const val ARG_CALORIES_PER_SERVING = "arg_calories_per_serving"
        private const val ARG_CALORIES_REASON = "arg_calories_reason"
        private const val ARG_IRON_INTAKE_PER_SERVING = "arg_iron_intake_per_serving"
        private const val ARG_IRON_INTAKE_REASON = "arg_iron_intake_reason"

        fun newInstance(
            food: String,
            caloriesPerServing: String,
            caloriesReason: String,
            ironIntakePerServing: String,
            ironReason: String
        ): SelectedDishFragment {
            val args = Bundle().apply {
                putString(ARG_FOOD, food)
                putString(ARG_CALORIES_PER_SERVING, caloriesPerServing)
                putString(ARG_CALORIES_REASON, caloriesReason)
                putString(ARG_IRON_INTAKE_PER_SERVING, ironIntakePerServing)
                putString(ARG_IRON_INTAKE_REASON, ironReason)
            }
            val fragment = SelectedDishFragment()
            fragment.arguments = args
            return fragment
        }
    }
}