package com.thesis.dishdetective_xml.ui.recipe_analyzer

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thesis.dishdetective_xml.databinding.ItemRecipeBinding

data class Recipe(
    val dishName: String = "",
    val servings: Int = 0,
    val ingredients: List<String> = emptyList(),
    val quantities: List<Double> = emptyList(),
    val totalNutrition: Map<String, Double> = emptyMap(),
    var documentId: String = ""
)

class RecipeAdapter(
    private val recipes: MutableList<Recipe>,
    private val onItemClick: (Recipe, Int) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private val handler = Handler(Looper.getMainLooper())

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe, isSelected: Boolean) {
            binding.dishName.text = recipe.dishName
            binding.servings.text = "Servings: ${recipe.servings}"
            // Bind other fields as needed

            // Set the click listener
            binding.root.setOnClickListener {
                onItemClick(recipe, adapterPosition)
            }

            // Update background color based on selection
            binding.root.setBackgroundColor(
                if (isSelected) Color.LTGRAY else Color.WHITE
            )
        }

        fun resetBackgroundColor() {
            binding.root.setBackgroundColor(Color.WHITE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = recipes.size

    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }

    fun removeRecipe(position: Int) {
        if (position < recipes.size) {
            recipes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)

        // Reset the background color after 1 second
        handler.postDelayed({
            selectedPosition = RecyclerView.NO_POSITION
            notifyItemChanged(position)
        }, 1000)
    }
}