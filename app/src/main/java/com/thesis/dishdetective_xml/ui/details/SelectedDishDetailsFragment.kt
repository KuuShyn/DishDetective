package com.thesis.dishdetective_xml.ui.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.thesis.dishdetective_xml.FoodRepository
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.ui.recipe_analyzer.EditRecipeFragment
import com.thesis.dishdetective_xml.util.FirebaseUtil
import java.util.Locale

class SelectedDishDetailsFragment : Fragment() {

    private lateinit var dishNameTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var proteinsValue: TextView
    private lateinit var fatValue: TextView
    private lateinit var carbsValue: TextView
    private lateinit var fiberValue: TextView
    private lateinit var ingredientContainer: LinearLayout
    private lateinit var proteinProgressBar: LinearProgressIndicator
    private lateinit var fatProgressBar: LinearProgressIndicator
    private lateinit var carbsProgressBar: LinearProgressIndicator
    private lateinit var fiberProgressBar: LinearProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_result, container, false)
        // Inflate the layout for this fragment
        dishNameTextView = view.findViewById(R.id.dishNameTextView)
        caloriesTextView = view.findViewById(R.id.caloriesTextView)
        proteinsValue = view.findViewById(R.id.proteinValue)
        fatValue = view.findViewById(R.id.fatValue)
        carbsValue = view.findViewById(R.id.carbsValue)
        fiberValue = view.findViewById(R.id.fiberValue)
        proteinProgressBar = view.findViewById(R.id.proteinProgress)
        fatProgressBar = view.findViewById(R.id.fatProgress)
        carbsProgressBar = view.findViewById(R.id.carbsProgress)
        fiberProgressBar = view.findViewById(R.id.fiberProgress)
        ingredientContainer = view.findViewById(R.id.IngredientContainer)

        // Get the data from the Bundle
        val dishName = arguments?.getString("dishName")
        val servings = arguments?.getString("servings")?.toIntOrNull() ?: 1
        val ingredients = arguments?.getStringArrayList("ingredients") ?: arrayListOf()
        val quantities =
            arguments?.getStringArrayList("quantities")?.map { it.toDoubleOrNull() ?: 100.0 }
                ?: List(ingredients.size) { 100.0 }
        val documentId = arguments?.getString("documentId")
        // Log received data
        Log.d("RecipeResultFragment", "Dish Name: $dishName")
        Log.d("RecipeResultFragment", "Servings: $servings")
        Log.d("RecipeResultFragment", "Ingredients: $ingredients")
        Log.d("RecipeResultFragment", "Quantities: $quantities")

        // Calculate total nutrition
        val totalNutrition = calculateTotalNutrition(ingredients, servings, quantities)

        // Log calculated nutrition
        Log.d("RecipeResultFragment", "Total Nutrition: $totalNutrition")

        // Display total nutrition
        dishNameTextView.text = "Dish: $dishName"
        caloriesTextView.text = "${totalNutrition["Calories"]} cal"
        proteinsValue.text = "${totalNutrition["Protein"]} g"
        fatValue.text = "${totalNutrition["Total_Fat"]} g"
        carbsValue.text = "${totalNutrition["Carbohydrate_total"]} g"
        fiberValue.text = "${totalNutrition["Fiber_total_dietary"]} g"

        proteinProgressBar.progress = totalNutrition["Protein"]?.toInt() ?: 0
        fatProgressBar.progress = totalNutrition["Total_Fat"]?.toInt() ?: 0
        carbsProgressBar.progress = totalNutrition["Carbohydrate_total"]?.toInt() ?: 0
        fiberProgressBar.progress = totalNutrition["Fiber_total_dietary"]?.toInt() ?: 0

        // Display ingredients and quantities
        ingredients.forEachIndexed { index, ingredient ->
            val quantity = quantities.getOrNull(index) ?: 100.0
            val ingredientTextView = TextView(context).apply {
                text = "$ingredient: $quantity g"
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            ingredientContainer.addView(ingredientTextView)
        }

        return view
    }

    private fun calculateTotalNutrition(
        ingredients: List<String>,
        servings: Int,
        quantities: List<Double>
    ): Map<String, Double> {
        val totalNutrition = mutableMapOf(
            "Calories" to 0.0,
            "Protein" to 0.0,
            "Total_Fat" to 0.0,
            "Carbohydrate_total" to 0.0,
            "Fiber_total_dietary" to 0.0
        )

        val foodList = FoodRepository.foodList

        // Step 1: Calculate the total weight of the recipe
        val totalWeight = quantities.sum() // Add up all the ingredient weights

        ingredients.forEachIndexed { index, ingredient ->
            val food = foodList.find {
                it.Food_Name_and_Description.toString().equals(ingredient, ignoreCase = true)
            }
            val quantity =
                quantities.getOrNull(index) ?: 100.0 // Default to 100g if no quantity provided

            food?.let {
                // Nutrient values are for 100g, adjust for actual quantity
                val quantityRatio = quantity / 100.0

                totalNutrition["Calories"] = (totalNutrition["Calories"] ?: 0.0) +
                        (parseToDouble(it.Energy_calculated) * quantityRatio)
                totalNutrition["Protein"] = (totalNutrition["Protein"] ?: 0.0) +
                        (parseToDouble(it.Protein) * quantityRatio)
                totalNutrition["Total_Fat"] = (totalNutrition["Total_Fat"] ?: 0.0) +
                        (parseToDouble(it.Total_Fat) * quantityRatio)
                totalNutrition["Carbohydrate_total"] =
                    (totalNutrition["Carbohydrate_total"] ?: 0.0) +
                            (parseToDouble(it.Carbohydrate_total) * quantityRatio)
                totalNutrition["Fiber_total_dietary"] =
                    (totalNutrition["Fiber_total_dietary"] ?: 0.0) +
                            (parseToDouble(it.Fiber_total_dietary) * quantityRatio)
            }
        }

        // Step 2: Adjust values per serving
        totalNutrition.keys.forEach { key ->
            totalNutrition[key] = (totalNutrition[key] ?: 0.0) / servings
        }
        totalNutrition.keys.forEach { key ->
            totalNutrition[key] = String.format(Locale.US, "%.2f", totalNutrition[key]).toDouble()
        }

        Log.d(
            "RecipeResultFragment",
            "Total Weight of com.thesis.dishdetective_xml.ui.recipe_analyzer.Recipe: $totalWeight g"
        )

        return totalNutrition
    }

    private fun parseToDouble(value: Any): Double {
        return when (value) {
            is String -> {
                value.toDoubleOrNull() ?: 0.0
            }

            is Number -> {
                value.toDouble()
            }

            else -> 0.0
        }
    }


}