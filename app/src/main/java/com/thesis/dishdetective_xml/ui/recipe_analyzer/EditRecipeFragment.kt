package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.thesis.dishdetective_xml.FoodRepository
import com.thesis.dishdetective_xml.R

class EditRecipeFragment : Fragment() {

    private lateinit var dishNameText: TextInputEditText
    private lateinit var servingsText: TextInputEditText
    private lateinit var editIngredientsContainer: LinearLayout
    private lateinit var addIngredientButton: Button
    private lateinit var updateButton: Button

    private val ingredientFields = mutableListOf<Pair<TextInputLayout, TextInputLayout>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_recipe, container, false)

        dishNameText = view.findViewById(R.id.editDishNameText)
        servingsText = view.findViewById(R.id.editServingsText)
        editIngredientsContainer = view.findViewById(R.id.addIngredientsContainer)
        addIngredientButton = view.findViewById(R.id.addIngredientButton)
        updateButton = view.findViewById(R.id.checkNutritionButton)

        // Get passed data (if available)
        val dishName = arguments?.getString("dishName")
        val servings = arguments?.getString("servings")?.toIntOrNull()
        val ingredients = arguments?.getStringArrayList("ingredients") ?: arrayListOf()
        val quantities = arguments?.getStringArrayList("quantities")?.map { it.toDoubleOrNull() ?: 100.0 }
        val documentId = arguments?.getString("documentId")

        // Set initial values
        dishNameText.setText(dishName)
        servingsText.setText(servings?.toString() ?: "1")

        // Add existing ingredients and quantities
        ingredients.forEachIndexed { index, ingredient ->
            addIngredientField(ingredient, quantities?.getOrNull(index) ?: 100.0)
        }

        // Add a new ingredient field when button is clicked
        addIngredientButton.setOnClickListener {
            addIngredientField("", 100.0)
        }

        // Save button functionality
        updateButton.setOnClickListener {
            if (documentId != null) {
                updateRecipe(documentId)
            }
        }

        return view
    }

    private fun addIngredientField(ingredient: String, quantity: Double) {
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

        val ingredientLayout = TextInputLayout(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputOuterFieldStyle
            )
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                129.dpToPx(requireContext()),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(0, 0, 8, 0)
            hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
        }

        val ingredientAutoCompleteTextView = MaterialAutoCompleteTextView(
            ContextThemeWrapper(
                requireContext(),
                R.style.LoginTextInputInnerFieldStyle
            )
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55.dpToPx(requireContext())
            )
            setPadding(18.dpToPx(requireContext()), 0, 0, 0)
            hint = "e.g., Tomatoes"
            setText(ingredient)
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
            setText(quantity.toString())
        }


        quantityLayout.addView(quantityInput)

        newIngredientLayout.addView(ingredientLayout)
        newIngredientLayout.addView(quantityLayout)

        // Add EditTexts to container
        editIngredientsContainer.addView(newIngredientLayout)
//        editIngredientsContainer.addView(quantityLayout)

        // Keep track of fields
        ingredientFields.add(Pair(ingredientLayout, quantityLayout))
    }

    private fun updateRecipe(documentId: String) {
        val dishName = dishNameText.text.toString()
        val servings = servingsText.text.toString().toIntOrNull() ?: 1

        val ingredients = ingredientFields.map {  it.first.editText?.text.toString() }
        val quantities = ingredientFields.map { it.second.editText?.text.toString().toDoubleOrNull() ?: 100.0 }

        // You can now pass this data back to the RecipeResultFragment or save it to a database
        val bundle = Bundle().apply {
            putString("dishName", dishName)
            putString("servings", servings.toString())
            putStringArrayList("ingredients", ArrayList(ingredients))
            putStringArrayList("quantities", ArrayList(quantities.map { it.toString() }))
            putString("documentId", documentId)
        }

        val recipeResultFragment = RecipeResultFragment().apply {
            arguments = bundle
        }

        // Navigate back to RecipeResultFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeResultFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
