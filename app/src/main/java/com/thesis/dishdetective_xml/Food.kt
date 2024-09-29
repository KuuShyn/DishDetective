package com.thesis.dishdetective_xml

data class Food(
    val Food_ID: Any = "",
    val Food_Name_and_Description: Any = "",
    val Scientific_Name: Any = "",
    val Alternate_Common_Name: Any = "",
    val Edible_Portion: Any = "",
    val Water: Any = "",  // Use Any to handle potential non-numeric values
    val Energy_calculated: Any = "",
    val Protein: Any = "",
    val Total_Fat: Any = "",
    val Carbohydrate_total: Any = "",
    val Ash_total: Any = "",
    val Fiber_total_dietary: Any = "",
    val Sugars_total: Any = "",
    val Calcium_Ca: Any = "",
    val Phosphorus_P: Any = "",
    val Iron_Fe: Any = "",
    val Sodium_Na: Any = "",
    val Retinol_Vitamin_A: Any = "",
    val Beta_Carotene: Any = "",
    val Retinol_Activity_Equivalent_RAE: Any = "",
    val Thiamin_Vitamin_B1: Any = "",
    val Riboflavin_Vitamin_B2: Any = "",
    val Niacin: Any = "",
    val Ascorbic_Acid_Vitamin_C: Any = "",
    val Fatty_acids_saturated_total: Any = "",
    val Fatty_acids_monounsaturated_total: Any = "",
    val Cholesterol: Any = ""
)