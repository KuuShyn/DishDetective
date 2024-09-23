package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.thesis.dishdetective_xml.FoodRepository
import com.thesis.dishdetective_xml.R

class RecipeAnalyzerFragment : Fragment() {

    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var addIngredientButton: MaterialButton
    private lateinit var checkNutritionButton: Button
    private lateinit var dishNameInput: TextInputEditText
    private lateinit var servingsInput: TextInputEditText
    private lateinit var predefinedIngredientInput: MaterialAutoCompleteTextView
    private lateinit var predefinedQuantityInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_analyzer, container, false)

        // Get references to predefined fields
        dishNameInput =
            view.findViewById<TextInputLayout>(R.id.editDishNameInputLayout).editText as TextInputEditText
        servingsInput =
            view.findViewById<TextInputLayout>(R.id.editServingsInputLayout).editText as TextInputEditText
        predefinedIngredientInput =
            view.findViewById<TextInputLayout>(R.id.ingredientLayout).editText as MaterialAutoCompleteTextView
        predefinedQuantityInput =
            view.findViewById<TextInputLayout>(R.id.quantityLayout).editText as TextInputEditText

        val ingredientList =
            FoodRepository.foodList.map { it.Food_Name_and_Description.toString() } // Retrieve the list of ingredients
        val ingredientAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ingredientList
        )
        predefinedIngredientInput.setAdapter(ingredientAdapter)

        // Reference to dynamic container and buttons
        ingredientsContainer = view.findViewById(R.id.addIngredientsContainer)
        addIngredientButton = view.findViewById(R.id.addIngredientButton)
        checkNutritionButton = view.findViewById(R.id.checkNutritionButton)

        // Add more ingredient fields when the button is clicked
        addIngredientButton.setOnClickListener {
            addIngredientFields()
        }

        // Collect inputs and pass to RecipeResultFragment when the button is clicked
        checkNutritionButton.setOnClickListener {
            if (validateInputs()) {
                switchToRecipeResultFragment()
            }
        }

        return view
    }

    private fun switchToRecipeResultFragment() {
        val dishName = dishNameInput.text.toString()
        val servings = servingsInput.text.toString()

        // Collect predefined ingredient and quantity
        val predefinedIngredient = predefinedIngredientInput.text.toString()
        val predefinedQuantity = predefinedQuantityInput.text.toString()

        // Collect dynamic ingredient and quantity data
        val ingredientsData = mutableListOf<String>()
        val quantitiesData = mutableListOf<String>()
        if (predefinedIngredient.isNotBlank() && predefinedQuantity.isNotBlank()) {
            ingredientsData.add(predefinedIngredient)
            quantitiesData.add(predefinedQuantity) // Add predefined quantity
        }

        // Collect dynamically added ingredient/quantity pairs
        for (i in 0 until ingredientsContainer.childCount) {
            val ingredientLayout = ingredientsContainer.getChildAt(i) as LinearLayout
            val ingredientInputLayout = ingredientLayout.getChildAt(0) as TextInputLayout
            val quantityInputLayout = ingredientLayout.getChildAt(1) as TextInputLayout

            val ingredient = ingredientInputLayout.editText?.text.toString()
            val quantity = quantityInputLayout.editText?.text.toString()

            if (ingredient.isNotBlank() && quantity.isNotBlank()) {
                ingredientsData.add(ingredient)
                quantitiesData.add(quantity)
            }
        }

        // Create a bundle to pass the data to the next fragment
        val bundle = Bundle()
        bundle.putString("dishName", dishName)
        bundle.putString("servings", servings)
        bundle.putStringArrayList("ingredients", ArrayList(ingredientsData))
        bundle.putStringArrayList("quantities", ArrayList(quantitiesData))

        // Switch to the RecipeResultFragment and pass the data
        val fragment = RecipeResultFragment()
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (dishNameInput.text.isNullOrBlank()) {
            dishNameInput.error = "Dish name cannot be blank"
            isValid = false
        }

        if (servingsInput.text.isNullOrBlank() || servingsInput.text.toString().toIntOrNull() == null) {
            servingsInput.error = "Enter a valid number of servings"
            isValid = false
        }

        return isValid
    }


    private fun addIngredientFields() {
        val newIngredientLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 0)
            }
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
        }

        // Ingredient TextInputLayout
        val ingredientLayout = TextInputLayout(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputOuterFieldStyle
            )
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                133.dpToPx(requireContext()),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(0, 0, 8, 0)
            hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
        }

        // Ingredient AutoCompleteTextView
        val ingredientAutoCompleteTextView = MaterialAutoCompleteTextView(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputInnerFieldStyle
            )
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48.dpToPx(requireContext())
            )
            setPadding(18.dpToPx(requireContext()), 0, 0, 0)
            hint = "e.g., Tomatoes"

        }

        val ingredientList = FoodRepository.foodList.map { it.Food_Name_and_Description.toString() }
        val ingredientAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ingredientList
        )
        ingredientAutoCompleteTextView.setAdapter(ingredientAdapter)
        ingredientAutoCompleteTextView.threshold = 1

        ingredientLayout.addView(ingredientAutoCompleteTextView)

        // Quantity TextInputEditText
        val quantityLayout = TextInputLayout(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputOuterFieldStyle
            )
        ).apply {
            layoutParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 8.dpToPx(requireContext())
                }
            hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
        }

        val quantityInput = TextInputEditText(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputInnerFieldStyle
            )
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g., 25"
        }

        quantityLayout.addView(quantityInput)

        newIngredientLayout.addView(ingredientLayout)
        newIngredientLayout.addView(quantityLayout)

        ingredientsContainer.addView(newIngredientLayout)
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
