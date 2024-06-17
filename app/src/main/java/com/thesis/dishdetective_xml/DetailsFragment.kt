package com.thesis.dishdetective_xml

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.thesis.dishdetective_xml.databinding.FragmentDetailsBinding

class DetailsFragment : DialogFragment() {
    private var _binding: FragmentDetailsBinding? = null
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
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the arguments
        val food = arguments?.getString(ARG_FOOD)
        val caloriesPerServing = arguments?.getString(ARG_CALORIES_PER_SERVING)
        val ironIntakePerServing = arguments?.getString(ARG_IRON_INTAKE_PER_SERVING)
        val allergenInformation = arguments?.getString(ARG_ALLERGEN_INFORMATION)
        val caloriesReason = arguments?.getString(ARG_CALORIES_REASON)
        val ironIntakeReason = arguments?.getString(ARG_IRON_INTAKE_REASON)
        val allergenReason = arguments?.getString(ARG_ALLERGEN_REASON)

        // Set the text of the TextViews
        binding.foodValue.text = food
        binding.caloriesValue.text = caloriesPerServing
        binding.ironValue.text = ironIntakePerServing
        binding.allergenValue.text = allergenInformation

        // Set the text of the TextViews
        binding.caloriesReason.text = caloriesReason
        binding.ironReason.text = ironIntakeReason
        binding.allergenReason.text = allergenReason
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FOOD = "arg_food"
        private const val ARG_CALORIES_PER_SERVING = "arg_calories_per_serving"
        private const val ARG_CALORIES_REASON = "arg_calories_reasons"
        private const val ARG_IRON_INTAKE_PER_SERVING = "arg_iron_intake_per_serving"
        private const val ARG_IRON_INTAKE_REASON = "arg_iron_intake_reasons"
        private const val ARG_ALLERGEN_INFORMATION = "arg_allergen_information"
        private const val ARG_ALLERGEN_REASON = "arg_allergen_reasons"

        fun newInstance(
            food: String,
            caloriesPerServing: String,
            caloriesReason: String,
            ironIntakePerServing: String,
            ironReason: String,
            allergenInformation: String,
            allergenReason: String
        ): DetailsFragment {
            val args = Bundle().apply {
                putString(ARG_FOOD, food)
                putString(ARG_CALORIES_PER_SERVING, caloriesPerServing)
                putString(ARG_CALORIES_REASON, caloriesReason)
                putString(ARG_IRON_INTAKE_PER_SERVING, ironIntakePerServing)
                putString(ARG_IRON_INTAKE_REASON, ironReason)
                putString(ARG_ALLERGEN_INFORMATION, allergenInformation)
                putString(ARG_ALLERGEN_REASON, allergenReason)


            }
            val fragment = DetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}