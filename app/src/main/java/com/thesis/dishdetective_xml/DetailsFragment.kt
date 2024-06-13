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
        val reasons = arguments?.getString(ARG_REASONS)
        val isDiabetic = arguments?.getString(ARG_IS_DIABETIC)

        // Set the text of the TextViews
        binding.foodValue.text = food
        binding.reasonsValue.text = reasons
        binding.isDiabeticValue.text = isDiabetic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FOOD = "arg_food"
        private const val ARG_REASONS = "arg_reasons"
        private const val ARG_IS_DIABETIC = "arg_is_diabetic"

        fun newInstance(food: String, reasons: String, isDiabetic: String): DetailsFragment {
            val args = Bundle().apply {
                putString(ARG_FOOD, food)
                putString(ARG_REASONS, reasons)
                putString(ARG_IS_DIABETIC, isDiabetic)
            }
            val fragment = DetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}