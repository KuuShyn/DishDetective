package com.thesis.dishdetective_xml.ui.captured

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentCapturedDetailsBinding

data class Dish(val name: String, val rank: Int)

data class Nutrients(val name: String, val value: Int, val unit: String)

class CapturedDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCapturedDetailsBinding? = null
    private val binding get() = _binding!!

    // Example data
    private val recommendedDishes = listOf(
        Dish(name = "Pork Sinigang", rank = 1),
        Dish(name = "Pork Adobo", rank = 2),
        Dish(name = "Chicken Adobo", rank = 3)
    )

    private val nutrients = listOf(
        Nutrients(name = "Iron", value = 47, unit = "g"),
        Nutrients(name = "Sodium", value = 0, unit = "mg"),
        Nutrients(name = "Cholesterol", value = 200, unit = "mg")
    )

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

        // Main layout to hold dynamically created views
        val mainLayout = binding.root.findViewById<LinearLayout>(R.id.dynamicContentLayout)

        // 1. Dynamically add recommended dishes section
        mainLayout.addView(createRecommendedDishesSection(context, recommendedDishes))

        // 2. Dynamically add total nutrients section
        mainLayout.addView(createTotalNutrientsSection(context, nutrients))

        // 3. Dynamically add goals section
        mainLayout.addView(createGoalsSection(context))

        // 4. Dynamically add nutrients considered section
        mainLayout.addView(createNutrientsConsideredSection(context))

        // 5. Dynamically add recommendations section
        mainLayout.addView(createRecommendationsSection(context))
    }

    private fun createRecommendedDishesSection(context: Context, dishes: List<Dish>): MaterialCardView {
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

            // Adding each dish
            for (dish in dishes) {
                val dishLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 0)
                }
                val dishRank = TextView(context).apply {
                    text = "${dish.rank}"
                    textSize = 16f
                    setPadding(8, 0, 8, 0)
                    background = ContextCompat.getDrawable(context, getCircleDrawable(dish.rank))
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                val dishName = TextView(context).apply {
                    text = dish.name
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                dishLayout.addView(dishRank)
                dishLayout.addView(dishName)
                layout.addView(dishLayout)
            }

            addView(layout)
        }
        return cardView
    }

    private fun getCircleDrawable(rank: Int): Int {
        return when (rank) {
            1 -> R.drawable.circle_green
            2 -> R.drawable.circle_yellow
            3 -> R.drawable.circle_red
            else -> R.drawable.circle_gray
        }
    }

    private fun createTotalNutrientsSection(context: Context, nutrients: List<Nutrients>): MaterialCardView {
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
                text = "Total Nutrients:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            layout.addView(title)

            // Dynamically add each nutrient if the value > 0
            for (nutrient in nutrients) {
                if (nutrient.value > 0) {
                    val nutrientLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 0)
                    }

                    val nutrientName = TextView(context).apply {
                        text = nutrient.name
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                    }

                    val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = 100
                        progress = nutrient.value // Example: Replace with actual progress
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(8, 0, 8, 0)
                        }
                    }

                    val nutrientValue = TextView(context).apply {
                        text = "${nutrient.value}${nutrient.unit}"
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                    }

                    nutrientLayout.addView(nutrientName)
                    nutrientLayout.addView(progressBar)
                    nutrientLayout.addView(nutrientValue)

                    layout.addView(nutrientLayout)
                }
            }

            addView(layout)
        }
        return cardView
    }

    private fun createGoalsSection(context: Context): MaterialCardView {
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
                text = "Your Goals:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val goal = TextView(context).apply {
                text = "Goal 1: Weight loss"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }

            val description = TextView(context).apply {
                text = "To lose weight, you need to consume fewer calories than your body burns, creating a calorie deficit. This forces your body to use stored fat for energy, leading to weight loss."
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }

            layout.addView(title)
            layout.addView(goal)
            layout.addView(description)

            addView(layout)
        }
        return cardView
    }

    private fun createNutrientsConsideredSection(context: Context): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = 12f
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
                text = "Nutrients Considered:"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            // Create row layout for icon and text
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
            }

            // Add icon for Calories
            val icon = ImageView(context).apply {
                setImageResource(R.drawable.circle_blue)  // Use your calorie icon resource
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    marginEnd = 16
                }
            }

            // Add text for Calories
            val caloriesText = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            val caloriesTitle = TextView(context).apply {
                text = "Calories"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            val description = TextView(context).apply {
                text = "Calories are a measure of energy provided by food. The balance between calories consumed and calories expended determines your body weight."
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            caloriesText.addView(caloriesTitle)
            caloriesText.addView(description)

            rowLayout.addView(icon)
            rowLayout.addView(caloriesText)

            layout.addView(title)
            layout.addView(rowLayout)

            addView(layout)
        }
        return cardView
    }


    private fun createRecommendationsSection(context: Context): LinearLayout {
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // Create Title for the section (outside the CardView)
        val title = TextView(context).apply {
            text = "How we make our recommendations:"
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }

        // Create the CardView for the content
        val cardView = MaterialCardView(context).apply {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_white))
            radius = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            // Create row layout for icon and text
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
            }

            // Add icon for lower calorie content
            val icon = ImageView(context).apply {
                setImageResource(R.drawable.ic_bad)  // Use your lower calorie content icon resource
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    marginEnd = 16
                }
            }

            // Add text for recommendation
            val recommendationText = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            val recommendationTitle = TextView(context).apply {
                text = "Prioritizing lower calorie content."
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            val description = TextView(context).apply {
                text = "We prioritize dishes that are low to moderate in calories based on your daily allowance. This helps you achieve a calorie deficit while keeping within safe and healthy limits."
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            recommendationText.addView(recommendationTitle)
            recommendationText.addView(description)

            rowLayout.addView(icon)
            rowLayout.addView(recommendationText)

            layout.addView(rowLayout)
            addView(layout)
        }

        // Add both the title and card to the main container
        containerLayout.addView(title)
        containerLayout.addView(cardView)

        return containerLayout
    }


    override fun onStart() {
        super.onStart()

        // Set the BottomSheet to have a peek height of 200dp
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = dpToPx(200)  // 200dp converted to pixels
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED  // Optional: Start collapsed
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
