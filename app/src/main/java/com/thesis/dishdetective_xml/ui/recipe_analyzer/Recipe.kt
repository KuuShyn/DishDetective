package com.thesis.dishdetective_xml.ui.recipe_analyzer

data class Recipe(
    val dishName: String = "",
    val servings: Int = 0,
    val ingredients: List<String> = emptyList(),
    val quantities: List<Double> = emptyList(),
    val totalNutrition: Map<String, Double> = emptyMap(),
    var documentId: String = ""
)