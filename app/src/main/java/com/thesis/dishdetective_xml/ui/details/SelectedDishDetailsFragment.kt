package com.thesis.dishdetective_xml.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.thesis.dishdetective_xml.R

class SelectedDishDetailsFragment : Fragment() {

    companion object {
        private const val ARG_FOOD_NAME = "food_name"
        private const val ARG_CALORIES = "calories"
        private const val ARG_PROTEINS = "proteins"
        private const val ARG_FATS = "fats"
        private const val ARG_CARBS = "carbs"
        private const val ARG_FIBER = "fiber"
        private const val ARG_INGREDIENTS = "ingredients"

        fun newInstance(
            foodName: String,
            calories: String,
            proteins: String,
            fats: String,
            carbs: String,
            fiber: String,
            ingredients: List<String>
        ): SelectedDishDetailsFragment {
            val fragment = SelectedDishDetailsFragment()
            val args = Bundle().apply {
                putString(ARG_FOOD_NAME, foodName)
                putString(ARG_CALORIES, calories)
                putString(ARG_PROTEINS, proteins)
                putString(ARG_FATS, fats)
                putString(ARG_CARBS, carbs)
                putString(ARG_FIBER, fiber)
                putStringArrayList(ARG_INGREDIENTS, ArrayList(ingredients))
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var foodName: String? = null
    private var calories: String? = null
    private var proteins: String? = null
    private var fats: String? = null
    private var carbs: String? = null
    private var fiber: String? = null
    private var ingredients: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            foodName = it.getString(ARG_FOOD_NAME)
            calories = it.getString(ARG_CALORIES)
            proteins = it.getString(ARG_PROTEINS)
            fats = it.getString(ARG_FATS)
            carbs = it.getString(ARG_CARBS)
            fiber = it.getString(ARG_FIBER)
            ingredients = it.getStringArrayList(ARG_INGREDIENTS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected_dish_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dishNameTextView = view.findViewById<TextView>(R.id.dishNameTextView)
        val caloriesTextView = view.findViewById<TextView>(R.id.caloriesTextView)
        val proteinsTextView = view.findViewById<TextView>(R.id.proteinValue)
        val fatsTextView = view.findViewById<TextView>(R.id.fatValue)
        val carbsTextView = view.findViewById<TextView>(R.id.carbsValue)
        val fiberTextView = view.findViewById<TextView>(R.id.fiberValue)
        val ingredientContainer = view.findViewById<LinearLayout>(R.id.IngredientContainer)
        val backButton = view.findViewById<MaterialButton>(R.id.detailsButton)

        // Display the dish details
        dishNameTextView?.text = foodName
        caloriesTextView?.text = calories
        proteinsTextView?.text = proteins
        fatsTextView?.text = fats
        carbsTextView?.text = carbs
        fiberTextView?.text = fiber

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Add ingredients dynamically
        ingredients?.forEach { ingredient ->
            val ingredientTextView = TextView(context).apply {
                text = ingredient
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            ingredientContainer.addView(ingredientTextView)
        }
    }
}
