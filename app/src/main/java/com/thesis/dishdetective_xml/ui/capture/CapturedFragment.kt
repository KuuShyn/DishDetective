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
import com.thesis.dishdetective_xml.FoodRepository
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.RecommendationSystem
import com.thesis.dishdetective_xml.databinding.FragmentCapturedBinding
import com.thesis.dishdetective_xml.ui.captured.CapturedDetailsFragment
import com.thesis.dishdetective_xml.ui.details.SelectedDishFragment

class CapturedFragment : Fragment() {

    private var _binding: FragmentCapturedBinding? = null
    private val binding get() = _binding!!

    private var capturedBitmap: Bitmap? = null
    private var boundingBoxes: List<BoundingBox>? = null

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
        val food = boundingBoxes?.map { capitalizeWords(it.clsName) } ?: emptyList()
        val recDish =
            RecommendationSystem.recommendationsList.map { capitalizeWords(it["dish"] as String) }
        val recDishScore =
            RecommendationSystem.recommendationsList.map { it["sorting_score"] as Double }
        Log.d("RecDish:", "$recDish")
        Log.d("RecDishScore:", "$recDishScore")
        Log.d("Food:", capitalizeWords("halo-halo"))
        RecommendationSystem.logRecommendations()
        val capturedDetailsFragment = CapturedDetailsFragment.newInstance(food)
        capturedDetailsFragment.show(parentFragmentManager, "CapturedDetailsFragment")
    }

    private fun handleTouch(x: Float, y: Float) {
        val imageView = binding.capturedImageView
        val bitmap = capturedBitmap ?: return
        val bitmapX = x * bitmap.width / imageView.width
        val bitmapY = y * bitmap.height / imageView.height

        // Check if the touch is inside any of the bounding boxes
        val touchedBox = boundingBoxes?.find { box ->
            bitmapX >= box.x1 * bitmap.width && bitmapX <= box.x2 * bitmap.width &&
                    bitmapY >= box.y1 * bitmap.height && bitmapY <= box.y2 * bitmap.height
        }

        touchedBox?.let { box ->
            val food = capitalizeWords(box.clsName)

            // Crop the bitmap using bounding box coordinates
            val croppedBitmap = cropBitmap(bitmap, box)

            // Show the details fragment and pass the cropped image
            val detailsFragment = SelectedDishFragment.newInstance(food, croppedBitmap)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit()

            Log.d(TAG, "Touched box: ${box.clsName}, Cropped Bitmap: ${croppedBitmap}")
        }
    }

    private fun cropBitmap(bitmap: Bitmap, box: BoundingBox): Bitmap {
        val x1 = (box.x1 * bitmap.width).toInt()
        val y1 = (box.y1 * bitmap.height).toInt()
        val x2 = (box.x2 * bitmap.width).toInt()
        val y2 = (box.y2 * bitmap.height).toInt()

        // Ensure the bounding box is within the bitmap boundaries
        val width = x2 - x1
        val height = y2 - y1

        return Bitmap.createBitmap(bitmap, x1, y1, width, height)
    }


    private fun capitalizeWords(input: String): String {
        return input.split('_').joinToString(" ") { word ->
            word.split('-').joinToString("-") { part ->
                part.lowercase().replaceFirstChar { it.uppercase() }
            }
        }

    }
}
