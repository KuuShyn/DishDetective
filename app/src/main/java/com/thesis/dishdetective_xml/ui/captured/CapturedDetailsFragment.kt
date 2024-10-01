package com.thesis.dishdetective_xml.ui.captured

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesis.dishdetective_xml.ProfileRec
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.RecommendationSystem
import com.thesis.dishdetective_xml.databinding.FragmentCapturedDetailsBinding

data class Dish(val name: String, val rank: Int)

data class Nutrients(val name: String, val value: Int, val unit: String)

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

        val context = requireContext()

// Get the profile detail and recommendations
        val profileDetail = ProfileRec.profileDetail.firstOrNull()
        val boundingBoxNames =
            arguments?.getStringArrayList(ARG_BOUNDING_BOX_NAMES) ?: listOf<String>()
        val recommendedDishes = RecommendationSystem.getRecommendations().filter { dish ->
            boundingBoxNames.contains(dish["dish"] as String)
        }

        if (profileDetail != null && recommendedDishes.isNotEmpty()) {
            // Main layout to hold dynamically created views
            val mainLayout = binding.root.findViewById<LinearLayout>(R.id.dynamicContentLayout)

            // 1. Dynamically add goals section based on profile recommendation
            mainLayout.addView(createGoalsSection(context, profileDetail.recommendation))

            // 2. Dynamically add recommended dishes section
            mainLayout.addView(createRecommendedDishesSection(context, recommendedDishes))

            // 3. Dynamically add total nutrients section based on profile recommendation
            mainLayout.addView(
                createTotalNutrientsSection(
                    context,
                    recommendedDishes,
                    profileDetail.recommendation
                )
            )

            // 4. Dynamically add nutrients considered section based on profile recommendation
            mainLayout.addView(
                createNutrientsConsideredSection(
                    context,
                    profileDetail.recommendation
                )
            )

            // 5. Dynamically add recommendations section based on profile recommendation
            mainLayout.addView(createRecommendationsSection(context, profileDetail.recommendation))


        } else {
            // Show a message if no dishes are detected
            val emptyMessage = TextView(context).apply {
                text = "No dishes detected from the bounding boxes."
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                gravity = Gravity.CENTER
            }
            binding.root.findViewById<LinearLayout>(R.id.dynamicContentLayout).addView(emptyMessage)
        }
    }

    private fun createRecommendedDishesSection(
        context: Context,
        dishes: List<Map<String, Any>>
    ): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            strokeColor = Color.TRANSPARENT
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = dpToPx(10).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val title = TextView(context).apply {
                text = "Order of recommended dishes based on goals:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Adding each dish from recommendations
            for (i in dishes.indices) {
                val dish = dishes[i]
                val dishLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 0)
                }

                // Determine drawable based on the position in the list
                val dishDrawable = when (i) {
                    0 -> R.drawable.circle_green   // First dish is green
                    dishes.size - 1 -> R.drawable.circle_red  // Last dish is red
                    else -> R.drawable.circle_yellow  // All dishes in between are yellow
                }

                val dishRank = TextView(context).apply {
                    text = (i + 1).toString()  // Rank by position
                    textSize = 16f
                    setPadding(16, 16, 16, 16)  // Padding to create enough space for the circle
                    background = ContextCompat.getDrawable(context, dishDrawable)  // Apply drawable
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                val dishName = TextView(context).apply {
                    text = dish["dish"] as String
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setPadding(16, 0, 0, 0)
                }

                dishLayout.addView(dishRank)
                dishLayout.addView(dishName)
                layout.addView(dishLayout)
            }

            addView(layout)
        }
        return cardView
    }


    private fun createTotalNutrientsSection(
        context: Context,
        recommendedDishes: List<Map<String, Any>>,
        profileRecommendation: String
    ): MaterialCardView {
        var totalSodium = 0
        var totalCholesterol = 0
        var totalIron = 0

        // Sum the nutrient values across all dishes
        for (dish in recommendedDishes) {
            if (profileRecommendation == "cardiovascular") {
                totalSodium += (dish["sodium_content"] as? Double)?.toInt() ?: 0
                totalCholesterol += (dish["cholesterol_content"] as? Double)?.toInt() ?: 0
            } else if (profileRecommendation == "anemia") {
                totalIron += (dish["iron_content"] as? Double)?.toInt() ?: 0
            }
        }

        // Adjust maxProgress dynamically based on the total values
        fun adjustMaxProgress(total: Int, recommended: Int): Int {
            var maxProgress = recommended
            while (total > maxProgress) {
                maxProgress += 100  // Increase the max by 100 if the total exceeds the current max
            }
            return maxProgress
        }

        // Calculate dynamic max progress
        val sodiumMaxProgress =
            adjustMaxProgress(totalSodium, 2000)   // Recommended intake 2000mg for sodium
        val cholesterolMaxProgress =
            adjustMaxProgress(totalCholesterol, 300) // Recommended intake 300mg for cholesterol
        val ironMaxProgress = adjustMaxProgress(totalIron, 18)  // Recommended intake 18mg for iron

        // Create the card view to display the total nutrients
        val cardView = MaterialCardView(context).apply {
            strokeColor = Color.TRANSPARENT
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = dpToPx(10).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val title = TextView(context).apply {
                text = "Total nutrients:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Display the total nutrients with progress bars based on the profile recommendation
            if (profileRecommendation == "cardiovascular") {
                // Sodium (Red)
                val sodiumLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 8, 0, 0)
                }
                val sodiumName = TextView(context).apply {
                    text = "Sodium"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val sodiumProgressBar =
                    ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = sodiumMaxProgress
                        progress = totalSodium  // Progress reflects the total sodium value
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                .apply {
                                    setMargins(8, 0, 8, 0)
                                }
                        progressDrawable.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            ), android.graphics.PorterDuff.Mode.SRC_IN
                        )  // Set red color
                    }
                val sodiumValue = TextView(context).apply {
                    text = "${totalSodium} mg"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                sodiumLayout.addView(sodiumName)
                sodiumLayout.addView(sodiumProgressBar)
                sodiumLayout.addView(sodiumValue)
                layout.addView(sodiumLayout)

                // Cholesterol (Yellow)
                val cholesterolLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 8, 0, 0)
                }
                val cholesterolName = TextView(context).apply {
                    text = "Cholesterol"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val cholesterolProgressBar =
                    ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = cholesterolMaxProgress
                        progress =
                            totalCholesterol  // Progress reflects the total cholesterol value
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                .apply {
                                    setMargins(8, 0, 8, 0)
                                }
                        progressDrawable.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.yellow
                            ), android.graphics.PorterDuff.Mode.SRC_IN
                        )  // Set yellow color
                    }
                val cholesterolValue = TextView(context).apply {
                    text = "${totalCholesterol} mg"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                cholesterolLayout.addView(cholesterolName)
                cholesterolLayout.addView(cholesterolProgressBar)
                cholesterolLayout.addView(cholesterolValue)
                layout.addView(cholesterolLayout)
            } else if (profileRecommendation == "anemia") {
                // Iron (Red)
                val ironLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 8, 0, 0)
                }
                val ironName = TextView(context).apply {
                    text = "Iron"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val ironProgressBar =
                    ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = ironMaxProgress
                        progress = totalIron  // Progress reflects the total iron value
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                .apply {
                                    setMargins(8, 0, 8, 0)
                                }
                        progressDrawable.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            ), android.graphics.PorterDuff.Mode.SRC_IN
                        )  // Set red color
                    }
                val ironValue = TextView(context).apply {
                    text = "${totalIron} mg"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                ironLayout.addView(ironName)
                ironLayout.addView(ironProgressBar)
                ironLayout.addView(ironValue)
                layout.addView(ironLayout)
            }

            addView(layout)
        }

        return cardView
    }

    private fun createGoalsSection(
        context: Context,
        profileRecommendation: String
    ): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            strokeColor = Color.TRANSPARENT
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = dpToPx(10).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            // Add title for the section
            val title = TextView(context).apply {
                text = "Your Goals:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Add Goal 1: Weight loss
            val goal1Title = TextView(context).apply {
                text = "Goal 1: Weight loss"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }
            val goal1Description = TextView(context).apply {
                text =
                    "To lose weight, you need to consume fewer calories than your body burns. This forces your body to use stored fat for energy."
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }
            layout.addView(goal1Title)
            layout.addView(goal1Description)

            // Add Goal 2 based on profile recommendation
            val goal2Title = TextView(context).apply {
                text = when (profileRecommendation) {
                    "cardiovascular" -> "Goal 2: Cardiovascular Risk Management"
                    "anemia" -> "Goal 2: Anemia Management"
                    else -> "Goal 2: General Health Management"
                }
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }

            val goal2Description = TextView(context).apply {
                text = when (profileRecommendation) {
                    "cardiovascular" -> "Calories, sodium, and cholesterol are key contributors to heart disease. Balancing its intake helps manage overall cardiovascular risk."
                    "anemia" -> "Anemia often results from low iron levels. Consuming enough iron can alleviate symptoms and improve overall health."
                    else -> "Maintaining a balanced intake of nutrients is essential for overall health."
                }
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }

            layout.addView(goal2Title)
            layout.addView(goal2Description)

            addView(layout)
        }
        return cardView
    }

    private fun createNutrientsConsideredSection(
        context: Context,
        profileRecommendation: String
    ): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            strokeColor = Color.TRANSPARENT
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = dpToPx(10).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val title = TextView(context).apply {
                text = "Nutrients considered:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Define the nutrients based on the recommendation
            val nutrients = when (profileRecommendation) {
                "cardiovascular" -> listOf(
                    "Calories" to "Calories are a measure of energy provided by food. The balance between calories consumed and calories expended determines your body weight.",
                    "Sodium" to "Sodium plays a key role in regulating blood pressure, excessive intake can contribute to hypertension, a major risk factor for heart disease.",
                    "Cholesterol" to "Cholesterol in excessive levels contributes to hyperlipidemia, a major risk factor for heart disease by promoting plaque buildup in arteries."
                )

                "anemia" -> listOf(
                    "Calories" to "Calories are a measure of energy provided by food. The balance between calories consumed and calories expended determines your body weight.",
                    "Iron" to "Iron is a vital nutrient for producing hemoglobin, which carries oxygen in your blood."
                )

                else -> listOf()
            }

            // Dynamically create views for each nutrient
            for ((index, nutrient) in nutrients.withIndex()) {
                val nutrientLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 16, 0, 0)
                    gravity = Gravity.CENTER_VERTICAL
                }

                // Create a numbered circle for the nutrient
                val numberCircle = TextView(context).apply {
                    text = (index + 1).toString()
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.circle_blue
                    )  // Use a blue circle drawable
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                    gravity = Gravity.CENTER
                }

                // Create the text for the nutrient name and description
                val nutrientTextLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 0, 0, 0)
                }

                val nutrientName = TextView(context).apply {
                    text = nutrient.first  // Nutrient name (e.g., Calories)
                    textSize = 16f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                val nutrientDescription = TextView(context).apply {
                    text = nutrient.second  // Nutrient description (e.g., explanation of Calories)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                nutrientTextLayout.addView(nutrientName)
                nutrientTextLayout.addView(nutrientDescription)

                // Add the numbered circle and the nutrient text to the layout
                nutrientLayout.addView(numberCircle)
                nutrientLayout.addView(nutrientTextLayout)

                // Add the nutrient layout to the main layout
                layout.addView(nutrientLayout)
            }

            addView(layout)
        }

        return cardView
    }

    private fun createRecommendationsSection(context: Context, profileRecommendation: String): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            strokeColor = Color.TRANSPARENT
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = dpToPx(10).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)  // Add padding around the entire recommendations section
            }

            val title = TextView(context).apply {
                text = "How we make our recommendations:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Add first recommendation: Prioritizing lower calorie content
            layout.addView(createRecommendationItem(
                context,
                R.drawable.ic_down_arrow_red,  // Your custom drawable for the down arrow (red)
                "Prioritizing lower calorie content.",
                "We prioritize dishes that are low to moderate in calories based on your daily allowance. This helps you achieve a calorie deficit while keeping within safe and healthy limits."
            ))

            // Add second recommendation based on profile recommendation
            when (profileRecommendation) {
                "cardiovascular" -> {
                    layout.addView(createRecommendationItem(
                        context,
                        R.drawable.ic_down_arrow_red,  // Your custom drawable for the down arrow (red)
                        "Prioritizing lower sodium & cholesterol content.",
                        "We prioritize dishes that are high in calcium based on your daily recommended intake."
                    ))

                    // Add the final conclusion block for cardiovascular
                    val conclusionCard = createConclusionCard(context, "Recommendations consider the ratio of sodium, cholesterol and calories. This ensures you get the least cardiovascular strain with the least caloric impact, supporting both your cardiovascular risk management and weight goals.")
                    layout.addView(conclusionCard)
                }
                "anemia" -> {
                    layout.addView(createRecommendationItem(
                        context,
                        R.drawable.ic_up_arrow_green,  // Your custom drawable for the up arrow (green)
                        "Prioritizing high iron content.",
                        "We prioritize dishes that are high in iron based on your daily recommended intake."
                    ))

                    // Add the final conclusion block for anemia
                    val conclusionCard = createConclusionCard(context, "Both factors consider the ratio of iron content to calories. This ensures you get the most nutritional benefit (iron) with the least caloric impact, supporting both your anemia management and weight goals.")
                    layout.addView(conclusionCard)
                }
            }

            addView(layout)
        }

        return cardView
    }

    private fun createRecommendationItem(context: Context, iconResId: Int, title: String, description: String): LinearLayout {
        val recommendationLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)  // Add padding to each recommendation item
            gravity = Gravity.CENTER_VERTICAL
        }

        // Icon for the recommendation
        val icon = ImageView(context).apply {
            setImageResource(iconResId)
            layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                marginEnd = 16
            }
        }

        // Text layout for title and description
        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Title (bold)
        val recommendationTitle = TextView(context).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Description
        val recommendationDescription = TextView(context).apply {
            text = description
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        textLayout.addView(recommendationTitle)
        textLayout.addView(recommendationDescription)

        recommendationLayout.addView(icon)
        recommendationLayout.addView(textLayout)

        return recommendationLayout
    }

    private fun createConclusionCard(context: Context, conclusionText: String): LinearLayout {
        val conclusionLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)  // Add padding to the conclusion card
            background = ContextCompat.getDrawable(context, R.drawable.conclusion_background)  // Customize background
        }

        val conclusionTextView = TextView(context).apply {
            text = conclusionText
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
        }

        conclusionLayout.addView(conclusionTextView)
        return conclusionLayout
    }



    override fun onStart() {
        super.onStart()

        // Set the BottomSheet to have a full expandable height
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight =
                dpToPx(200)  // Automatically adjust to available screen height
            behavior.state =
                BottomSheetBehavior.STATE_EXPANDED  // Allow the bottom sheet to expand fully
        }
    }


    // Convert dp to pixels
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val ARG_BOUNDING_BOX_NAMES = "bounding_box_names"

        fun newInstance(boundingBoxNames: List<String>): CapturedDetailsFragment {
            val fragment = CapturedDetailsFragment()
            val args = Bundle().apply {
                putStringArrayList(ARG_BOUNDING_BOX_NAMES, ArrayList(boundingBoxNames))
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
