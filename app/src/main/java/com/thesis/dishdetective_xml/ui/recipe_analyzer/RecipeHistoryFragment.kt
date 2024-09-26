package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.os.Bundle
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
import com.thesis.dishdetective_xml.util.Debounce.setDebounceClickListener

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

        // Initialize adapter with delete functionality
        recipeAdapter = RecipeAdapter(recipes,
            { recipe, position ->
                // Handle item click
                Toast.makeText(context, "Clicked on ${recipe.dishName}", Toast.LENGTH_SHORT).show()
                recipeAdapter.setSelectedPosition(position)
                navigateToRecipeResult(recipe)
            },
            { recipe, position ->
                // Handle delete button click
                deleteRecipe(recipe, position)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }

        binding.addRecipeButton.setDebounceClickListener {
            navigateToRecipeAnalyzer()
        }

        fetchRecipes()
    }

    private fun fetchRecipes() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("recipes")
                .get()
                .addOnSuccessListener { documents ->
                    val newRecipes = mutableListOf<Recipe>()
                    for (document in documents) {
                        val recipe = document.toObject(Recipe::class.java)
                        recipe.documentId = document.id
                        newRecipes.add(recipe)
                    }
                    updateRecipes(newRecipes)
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    Toast.makeText(context, "Error fetching recipes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateRecipes(newRecipes: List<Recipe>) {
        val oldSize = recipes.size
        recipes.clear()
        recipeAdapter.notifyItemRangeRemoved(0, oldSize)
        recipes.addAll(newRecipes)
        recipeAdapter.notifyItemRangeInserted(0, newRecipes.size)
        updateUI()
    }

    private fun updateUI() {
        if (recipes.isEmpty()) {
            binding.recyclerView.animate().alpha(0f).withEndAction {
                binding.recyclerView.visibility = View.GONE
                binding.emptyTextView.visibility = View.VISIBLE
                binding.addRecipeButton.visibility = View.VISIBLE
            }.start()
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerView.animate().alpha(1f).start()
            binding.emptyTextView.visibility = View.GONE
            binding.addRecipeButton.visibility = View.GONE
        }
    }

    private fun deleteRecipe(recipe: Recipe, position: Int) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("recipes")
                .document(recipe.documentId)
                .delete()
                .addOnSuccessListener {
                    recipeAdapter.removeRecipe(position)
                    Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error deleting recipe: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun navigateToRecipeAnalyzer() {
        val recipeAnalyzerFragment = RecipeAnalyzerFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, recipeAnalyzerFragment)
            .addToBackStack(null)
            .commit()
    }
}