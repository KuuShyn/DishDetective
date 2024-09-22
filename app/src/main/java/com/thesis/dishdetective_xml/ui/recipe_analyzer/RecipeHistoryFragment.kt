package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentRecipeHistoryBinding

class RecipeHistoryFragment : Fragment() {

    private lateinit var binding: FragmentRecipeHistoryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recipeAdapter: RecipeAdapter
    private val recipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        recipeAdapter = RecipeAdapter(recipes) { recipe, position ->
            // Handle item click
            Toast.makeText(context, "Clicked on ${recipe.dishName}", Toast.LENGTH_SHORT).show()
            recipeAdapter.setSelectedPosition(position)
            navigateToRecipeResult(recipe)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }

        fetchRecipes()
    }

    private fun fetchRecipes() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("recipes")
                .get()
                .addOnSuccessListener { documents ->
                    recipes.clear() // Clear the existing list to avoid duplicates
                    for (document in documents) {
                        val recipe = document.toObject(Recipe::class.java)
                        recipe.documentId = document.id
                        recipeAdapter.addRecipe(recipe)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    Toast.makeText(context, "Error fetching recipes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToRecipeResult(recipe: Recipe) {
        val recipeResultFragment = RecipeResultFragment().apply {
            arguments = Bundle().apply {
                putString("dishName", recipe.dishName)
                putString("servings", recipe.servings.toString())
                putStringArrayList("ingredients", ArrayList(recipe.ingredients))
                putStringArrayList("quantities", ArrayList(recipe.quantities.map { it.toString() }))
                putString("documentId", recipe.documentId)

            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeResultFragment)
            .addToBackStack(null)
            .commit()
    }
}