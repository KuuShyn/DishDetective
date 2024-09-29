// File: SelectedDishFragment.kt
package com.thesis.dishdetective_xml.ui.details

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentSelectedDishBinding

class SelectedDishFragment : Fragment() {
    private var _binding: FragmentSelectedDishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the arguments
        val food = arguments?.getString(ARG_FOOD)
        val imageResId = arguments?.getInt(ARG_IMAGE_RES_ID)
        val caloriesPerServing = arguments?.getString(ARG_CALORIES_PER_SERVING)
        val ironIntakePerServing = arguments?.getString(ARG_IRON_INTAKE_PER_SERVING)

        val caloriesReason = arguments?.getString(ARG_CALORIES_REASON)
        val ironIntakeReason = arguments?.getString(ARG_IRON_INTAKE_REASON)

        // Set the text of the TextViews
        binding.dishName.text = food
        binding.caloriesValue.text = caloriesPerServing
        binding.ironValue.text = ironIntakePerServing

        // Set the image
        imageResId?.let { binding.dishImage.setImageResource(it) }

        // Apply color to "high" in caloriesReason and "low" in ironReason
        applyColorToHigh(binding.caloriesReason, caloriesReason, R.color.red, "high")
        applyColorToHigh(binding.ironReason, ironIntakeReason, R.color.green, "low")

        updateCaloriesValueBackgroundColor(R.color.green_background)
    }

    private fun applyColorToHigh(textView: TextView, text: String?, color: Int, word: String) {
        val highColor = ContextCompat.getColor(requireContext(), color)
        if (!text.isNullOrEmpty()) {
            val spannableString = SpannableString(text)
            // Convert both text and word to lowercase for case-insensitive search
            val start = text.lowercase().indexOf(word.lowercase())
            if (start != -1) {
                val end = start + word.length
                spannableString.setSpan(
                    ForegroundColorSpan(highColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                textView.text = spannableString
            } else {
                textView.text = text // Fallback if "high" or "low" is not found
            }
        }
    }

    private fun updateCaloriesValueBackgroundColor(colorResId: Int) {
        val color = ContextCompat.getColor(requireContext(), colorResId)
        val background = binding.caloriesValue.background
        if (background is GradientDrawable) {
            background.setColor(color)
        }
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

        private const val ARG_IMAGE_RES_ID = "arg_image_res_id"

        fun newInstance(
            food: String,
            caloriesPerServing: String,
            caloriesReason: String,
            ironIntakePerServing: String,
            ironReason: String,
            imageResId: Int
        ): SelectedDishFragment {
            val args = Bundle().apply {
                putString(ARG_FOOD, food)
                putString(ARG_CALORIES_PER_SERVING, caloriesPerServing)
                putString(ARG_CALORIES_REASON, caloriesReason)
                putString(ARG_IRON_INTAKE_PER_SERVING, ironIntakePerServing)
                putString(ARG_IRON_INTAKE_REASON, ironReason)
                putInt(ARG_IMAGE_RES_ID, imageResId)
            }
            val fragment = SelectedDishFragment()
            fragment.arguments = args
            return fragment
        }
    }
}