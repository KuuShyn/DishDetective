package com.thesis.dishdetective_xml.ui.details

import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.thesis.dishdetective_xml.FoodRepository
import com.thesis.dishdetective_xml.ProfileRec
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.RecommendationSystem

class SelectedDishFragment : Fragment() {

    companion object {
        private const val ARG_FOOD_NAME = "food_name"
        private const val ARG_CROPPED_BITMAP = "cropped_bitmap"


        fun newInstance(
            foodName: String,
            croppedBitmap: Bitmap,

            ): SelectedDishFragment {
            val fragment = SelectedDishFragment()
            val args = Bundle().apply {
                putString(ARG_FOOD_NAME, foodName)
                putParcelable(ARG_CROPPED_BITMAP, croppedBitmap)

            }
            fragment.arguments = args
            return fragment
        }
    }

    private var foodName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            foodName = it.getString(ARG_FOOD_NAME)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected_dish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val croppedBitmap = arguments?.getParcelable<Bitmap>(ARG_CROPPED_BITMAP)
        val foodName = arguments?.getString(ARG_FOOD_NAME)

        val dishNameTextView = view.findViewById<MaterialTextView>(R.id.dishName)
        dishNameTextView.text = foodName

        // Display cropped bitmap in an ImageView
        val imageView = view.findViewById<ImageView>(R.id.dish_image)
        croppedBitmap?.let {
            imageView.setImageBitmap(it)
        }

        val dishIngredients =
            FoodRepository.dishList.find { it.Food_Name_and_Description == foodName }

        // Dynamically add sections based on recommendation type
        val nutrientContainer = view.findViewById<LinearLayout>(R.id.nutrientContainer)
        renderNutrientsBasedOnRecommendation(nutrientContainer)

        val detailsButton = view.findViewById<MaterialButton>(R.id.detailsButton)
        detailsButton.setOnClickListener {
            // Ensure the dish ingredients exist
            dishIngredients?.let {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        SelectedDishDetailsFragment.newInstance(
                            foodName = foodName ?: "",
                            calories = it.Energy_calculated.toString(),
                            proteins = it.Protein.toString(),
                            fats = it.Total_Fat.toString(),
                            carbs = it.Carbohydrate_total.toString(),
                            fiber = it.Fiber_total_dietary.toString(),
                            ingredients = listOf(
                                "750g Chicken",
                                "1/3 cup Soy Sauce"
                            ) // Example ingredients
                        )
                    )
                    .addToBackStack(null)
                    .commit()
            } ?: run {
                Toast.makeText(
                    requireContext(),
                    "Dish information not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Render nutrients based on the recommendation type (anemia, cardiovascular, etc.).
     */
    private fun renderNutrientsBasedOnRecommendation(container: LinearLayout) {
        val profileDetail = ProfileRec.profileDetail.firstOrNull()
        val recDish = RecommendationSystem.recommendationsList.find { it["dish"] == foodName }

        recDish?.let {
            when (profileDetail?.recommendation) {
                "anemia" -> {
                    // Display iron-related information for anemia
                    addNutrientSection(
                        container,
                        title = "Iron Intake per Serving",
                        amount = "${it["iron_content"].toString()} mg",
                        classification = it["iron_classification"].toString()
                    )
                }

                "cardiovascular" -> {
                    // Display sodium and cholesterol-related information for cardiovascular health
                    addNutrientSection(
                        container,
                        title = "Sodium Intake per Serving",
                        amount = "${it["sodium_content"].toString()} mg",
                        classification = it["sodium_classification"].toString()
                    )
                    addNutrientSection(
                        container,
                        title = "Cholesterol Intake per Serving",
                        amount = "${it["cholesterol_content"].toString()} mg",
                        classification = it["cholesterol_classification"].toString(),
                        addPaddingTop = true // Add padding before this section
                    )
                }
            }
        }
    }

    /**
     * Adds a nutrient section with a title, amount, and reason based on the content level.
     */
    private fun addNutrientSection(
        container: LinearLayout,
        title: String,
        amount: String,
        classification: String,
        addPaddingTop: Boolean = false // New parameter for padding
    ) {
        // Title for the Nutrient section
        val nutrientTitleTextView = MaterialTextView(requireContext()).apply {
            text = title
            textSize = 20f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))

            // Apply padding if required (for example, between sodium and cholesterol)
            if (addPaddingTop) {
                setPadding(0, 16, 0, 0)
            }
        }

        // Serving size TextView
        val nutrientServingTextView = MaterialTextView(requireContext()).apply {
            text = amount
            textSize = 28f
            setTextColor(getClassificationColor(classification))

        }

        val firstWord = title.split(" ").first()

        // Reason TextView
        val nutrientReasonTextView = MaterialTextView(requireContext()).apply {
            val reasonText = when (classification.lowercase()) {
                "high" -> "This dish contains a high amount of $firstWord content."
                "moderate", "medium" -> "This dish has moderate $firstWord content."
                else -> "This dish has low $firstWord content."
            }

            val spannableString = SpannableString(reasonText)
            val color = getClassificationColor(classification)

            val classificationIndex = reasonText.indexOf(classification.lowercase())
            if (classificationIndex != -1) {
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    classificationIndex,
                    classificationIndex + classification.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            text = spannableString
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        }

        // Add the views to the container
        container.addView(nutrientTitleTextView)
        container.addView(nutrientServingTextView)
        container.addView(nutrientReasonTextView)
    }

    /**
     * Returns the classification color based on the classification level (high, moderate, low).
     */
    private fun getClassificationColor(classification: String): Int {
        return when (classification.lowercase()) {
            "high" -> ContextCompat.getColor(requireContext(), R.color.red)
            "moderate", "medium" -> ContextCompat.getColor(requireContext(), R.color.yellow)
            "low" -> ContextCompat.getColor(requireContext(), R.color.green)
            else -> ContextCompat.getColor(requireContext(), R.color.blue)
        }
    }
}
