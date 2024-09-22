// File: CapturedFragment.kt
package com.thesis.dishdetective_xml.ui.capture

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.thesis.dishdetective_xml.BoundingBox
import com.thesis.dishdetective_xml.MainActivity
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentCapturedBinding
import com.thesis.dishdetective_xml.ui.captured.CapturedDetailsFragment
import com.thesis.dishdetective_xml.ui.details.SelectedDishFragment

class CapturedFragment : Fragment() {

    private var _binding: FragmentCapturedBinding? = null
    private val binding get() = _binding!!

    private var capturedBitmap: Bitmap? = null
    private var boundingBoxes: List<BoundingBox>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            capturedBitmap = it.getParcelable(ARG_BITMAP)
            boundingBoxes = it.getParcelableArrayList(ARG_BOUNDING_BOXES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCapturedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        capturedBitmap?.let { bitmap ->
            binding.capturedImageView.setImageBitmap(bitmap)
            binding.capturedImageView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Handle action down event
                        handleTouch(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        // Handle action up event
                        v.performClick()
                    }
                }
                true
            }
        }
        showCapturedDetails()

        binding.showCapturedDetailsButton.setOnClickListener {
            showCapturedDetails()
        }

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.showDetailsButton.setOnClickListener {
            showCapturedDetails()
        }

        binding.showDetailsButton.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.green),
            PorterDuff.Mode.SRC_IN
        )
    }
    private fun showCapturedDetails() {
        val capturedDetailsFragment = CapturedDetailsFragment()
        capturedDetailsFragment.show(parentFragmentManager, "CapturedDetailsFragment")
    }

    private fun handleTouch(x: Float, y: Float) {
        // Convert touch coordinates to bitmap coordinates
        val imageView = binding.capturedImageView
        val bitmap = capturedBitmap ?: return
        val bitmapX = x * bitmap.width / imageView.width
        val bitmapY = y * bitmap.height / imageView.height

        // Check if the touch is inside any of the bounding boxes
        val touchedBox = boundingBoxes?.find { box ->
            bitmapX >= box.x1 * bitmap.width && bitmapX <= box.x2 * bitmap.width &&
                    bitmapY >= box.y1 * bitmap.height && bitmapY <= box.y2 * bitmap.height
        }

        if (touchedBox != null) {
            // The touch was inside a bounding box, handle it here
            Log.d(TAG, "Touched box: ${touchedBox.clsName}")
            val food = touchedBox.clsName
            val caloriesPerServing = "96mg"
            val ironIntakePerServing = "69mg"
            val caloriesReason = "Calories are Too High!"
            val ironIntakeReason = "Iron intake is Too Low."

            // Show the details fragment
            val detailsFragment = SelectedDishFragment.newInstance(
                food,
                caloriesPerServing,
                caloriesReason,
                ironIntakePerServing,
                ironIntakeReason
            )
            detailsFragment.show(parentFragmentManager, "SelectedDishFragment")
        }
    }

    companion object {
        private const val ARG_BITMAP = "bitmap"
        private const val ARG_BOUNDING_BOXES = "bounding_boxes"

        fun newInstance(bitmap: Bitmap, boundingBoxes: List<BoundingBox>) =
            CapturedFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BITMAP, bitmap)
                    putParcelableArrayList(ARG_BOUNDING_BOXES, ArrayList(boundingBoxes))
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Show the views again
//        binding.viewFinder.visibility = View.VISIBLE
//        binding.overlay.visibility = View.VISIBLE
//        binding.captureButton.visibility = View.VISIBLE
//        binding.inferenceTime.visibility = View.VISIBLE
        // Add other views you want to show here
    }
}