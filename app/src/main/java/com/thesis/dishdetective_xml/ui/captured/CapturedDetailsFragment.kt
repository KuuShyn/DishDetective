// File: CapturedDetailsFragment.kt
package com.thesis.dishdetective_xml.ui.captured

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentCapturedDetailsBinding

class CapturedDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCapturedDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCapturedDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        """Goal: Weight loss\n${getString(R.string.goal_loss)}""".also { binding.goals.text = it }
        """Calories\n${getString(R.string.calories_desc)}""".also {binding.nutrients.text = it}
        """Prioritizing lower calorie content ${getString(R.string.lower_calorie_content)}""" .also{ binding.recommendations.text = it}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}