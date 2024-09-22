package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.FoodRepository
import java.util.Locale


class RecipeResultFragment : Fragment() {

    private lateinit var dishNameTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var proteinsTextView: TextView
    private lateinit var fatTextView: TextView
    private lateinit var carbsTextView: TextView
    private lateinit var fibersTextView: TextView
    private lateinit var ingredientContainer: LinearLayout
    private lateinit var editRecipeButton: Button
    private lateinit var saveRecipeButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_result, container, false)

        dishNameTextView = view.findViewById(R.id.editDishNameTextView)
        caloriesTextView = view.findViewById(R.id.caloriesTextView)
        proteinsTextView = view.findViewById(R.id.proteinsTextView)
        fatTextView = view.findViewById(R.id.fatTextView)
        carbsTextView = view.findViewById(R.id.carbsTextView)
        fibersTextView = view.findViewById(R.id.fibersTextView)
        ingredientContainer = view.findViewById(R.id.IngredientContainer)
        editRecipeButton = view.findViewById(R.id.editRecipeButton)
        saveRecipeButton = view.findViewById(R.id.saveRecipeButton)

        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Get the data from the Bundle
        val dishName = arguments?.getString("dishName")
        val servings = arguments?.getString("servings")?.toIntOrNull() ?: 1
        val ingredients = arguments?.getStringArrayList("ingredients") ?: arrayListOf()
        val quantities = arguments?.getStringArrayList("quantities")?.map { it.toDoubleOrNull() ?: 100.0 } ?: List(ingredients.size) { 100.0 }
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
        caloriesTextView.text = "Calories: ${totalNutrition["Calories"]} cal"
        proteinsTextView.text = "Proteins: ${totalNutrition["Protein"]} g"
        fatTextView.text = "Total Fat: ${totalNutrition["Total_Fat"]} g"
        carbsTextView.text = "Carbs: ${totalNutrition["Carbohydrate_total"]} g"
        fibersTextView.text = "Fibers: ${totalNutrition["Fiber_total_dietary"]} g"

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
        // Inside RecipeResultFragment.kt
        editRecipeButton.setOnClickListener {
            val editRecipeFragment = EditRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString("dishName", dishNameTextView.text.toString().removePrefix("Dish: "))
                    putString("servings", servings.toString())
                    putStringArrayList("ingredients", ArrayList(ingredients))
                    putStringArrayList("quantities", ArrayList(quantities.map { it.toString() }))
                    putString("documentId", documentId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, editRecipeFragment)
                .addToBackStack(null)
                .commit()
        }

        saveRecipeButton.setOnClickListener {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                saveRecipeToFirestore(currentUser.uid, dishName, servings, ingredients, quantities, totalNutrition, documentId)
            } else {
                Log.w("RecipeResultFragment", "User not authenticated")
            }
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
            val food = foodList.find { it.Food_Name_and_Description.toString().equals(ingredient, ignoreCase = true) }
            val quantity = quantities.getOrNull(index) ?: 100.0 // Default to 100g if no quantity provided

            food?.let {
                // Nutrient values are for 100g, adjust for actual quantity
                val quantityRatio = quantity / 100.0

                totalNutrition["Calories"] = (totalNutrition["Calories"] ?: 0.0) +
                        (parseToDouble(it.Energy_calculated) * quantityRatio)
                totalNutrition["Protein"] = (totalNutrition["Protein"] ?: 0.0) +
                        (parseToDouble(it.Protein) * quantityRatio)
                totalNutrition["Total_Fat"] = (totalNutrition["Total_Fat"] ?: 0.0) +
                        (parseToDouble(it.Total_Fat) * quantityRatio)
                totalNutrition["Carbohydrate_total"] = (totalNutrition["Carbohydrate_total"] ?: 0.0) +
                        (parseToDouble(it.Carbohydrate_total) * quantityRatio)
                totalNutrition["Fiber_total_dietary"] = (totalNutrition["Fiber_total_dietary"] ?: 0.0) +
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

        Log.d("RecipeResultFragment", "Total Weight of com.thesis.dishdetective_xml.ui.recipe_analyzer.Recipe: $totalWeight g")

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

    private fun saveRecipeToFirestore(
        userId: String,
        dishName: String?,
        servings: Int,
        ingredients: List<String>,
        quantities: List<Double>,
        totalNutrition: Map<String, Double>,
        documentId: String? // New parameter for the document ID
    ) {

        val recipe = hashMapOf(
            "dishName" to dishName,
            "servings" to servings,
            "ingredients" to ingredients,
            "quantities" to quantities,
            "totalNutrition" to totalNutrition
        )

        if (documentId != null) {
            // Update existing recipe
            db.collection("users").document(userId).collection("recipes").document(documentId)
                .set(recipe)
                .addOnSuccessListener {
                    Log.d("RecipeResultFragment", "DocumentSnapshot successfully updated!")
                    Toast.makeText(context, "Recipe updated successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Navigate back to the previous fragment
                }
                .addOnFailureListener { e ->
                    Log.w("RecipeResultFragment", "Error updating document", e)
                    Toast.makeText(context, "Error updating recipe", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add a new recipe
            db.collection("users").document(userId).collection("recipes")
                .add(recipe)
                .addOnSuccessListener { documentReference ->
                    Log.d("RecipeResultFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(context, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Navigate back to the previous fragment
                }
                .addOnFailureListener { e ->
                    Log.w("RecipeResultFragment", "Error adding document", e)
                    Toast.makeText(context, "Error saving recipe", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
