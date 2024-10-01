package com.thesis.dishdetective_xml

import android.util.Log
import kotlin.math.round

object RecommendationSystem {

    internal val recommendationsList = mutableListOf<Map<String, Any>>()

    // Converts the Any type to a Double safely, defaulting to 0.0 if it's not parsable.
    private fun toDouble(value: Any): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    // Rounds a double value to 2 decimal places
    private fun roundToTwoDecimal(value: Double): Double {
        return round(value * 100) / 100
    }

    private fun calorieContentAssessment(dailyCalorieNeeds: Int, dishCalories: Double): String {
        val lowCalorieThreshold = dailyCalorieNeeds * 0.15
        val highCalorieThreshold = dailyCalorieNeeds * 0.25

        return when {
            dishCalories < lowCalorieThreshold -> "low"
            dishCalories > highCalorieThreshold -> "high"
            else -> "moderate"
        }
    }

    private fun nutrientContentAssessment(recommendedIntake: Double, dishNutrientContent: Double): String {
        val percentage = (dishNutrientContent / recommendedIntake) * 100
        return when {
            percentage <= 5 -> "low"
            percentage < 20 -> "medium"
            else -> "high"
        }
    }

    private fun contentBasedRecommendation(profile: ProfileResults, dishes: List<Food>): List<Map<String, Any>> {
        val recommendations = mutableListOf<Map<String, Any>>()

        for (dish in dishes) {
            val dishCalories = toDouble(dish.Energy_calculated)
            val caloriePercentage = roundToTwoDecimal((dishCalories / profile.ter) * 100)
            val calorieCategory = calorieContentAssessment(profile.ter, dishCalories)

            if (profile.recommendation == "anemia") {
                val ironContent = toDouble(dish.Iron_Fe)
                val ironPercentage = roundToTwoDecimal((ironContent / profile.recommendedIronIntake.toDouble()) * 100)
                val ironClassification = nutrientContentAssessment(profile.recommendedIronIntake.toDouble(), ironContent)

                val sortingScore = if (profile.weightChangeMode == "lose") {
                    ironPercentage / caloriePercentage
                } else {
                    (ironPercentage / caloriePercentage) + caloriePercentage
                }

                recommendations.add(
                    mapOf(
                        "dish" to dish.Food_Name_and_Description,
                        "iron_content" to ironContent,
                        "percentage_of_daily_value_iron" to ironPercentage,
                        "iron_classification" to ironClassification,
                        "calories" to dishCalories,
                        "percentage_of_daily_goal_calories" to caloriePercentage,
                        "calorie_category" to calorieCategory,
                        "sorting_score" to roundToTwoDecimal(sortingScore)
                    )
                )
            } else if (profile.recommendation == "cardiovascular") {
                val sodiumContent = toDouble(dish.Sodium_Na)
                val sodiumPercentage = roundToTwoDecimal((sodiumContent / profile.recommendedSodiumIntake.toDouble()) * 100)
                val sodiumClassification = nutrientContentAssessment(profile.recommendedSodiumIntake.toDouble(), sodiumContent)

                val cholesterolContent = toDouble(dish.Cholesterol)
                val cholesterolPercentage = roundToTwoDecimal((cholesterolContent / profile.recommendedCholesterolIntake.toDouble()) * 100)
                val cholesterolClassification = nutrientContentAssessment(profile.recommendedCholesterolIntake.toDouble(), cholesterolContent)

                // Combined Sodium + Cholesterol Calculation
                val combinedRiskNumber = roundToTwoDecimal(sodiumContent + cholesterolContent)
                val combinedRiskPercentage = roundToTwoDecimal(sodiumPercentage + cholesterolPercentage)

                val sortingScore = if (profile.weightChangeMode == "lose") {
                    sodiumPercentage + cholesterolPercentage + (2 * caloriePercentage)
                } else {
                    (0.5 * caloriePercentage) - (0.7 * (sodiumPercentage + cholesterolPercentage))
                }

                recommendations.add(
                    mapOf(
                        "dish" to dish.Food_Name_and_Description,
                        "sodium_content" to sodiumContent,
                        "percentage_of_daily_value_sodium" to sodiumPercentage,
                        "sodium_classification" to sodiumClassification,
                        "cholesterol_content" to cholesterolContent,
                        "percentage_of_daily_value_cholesterol" to cholesterolPercentage,
                        "cholesterol_classification" to cholesterolClassification,
                        "combined_risk_number" to combinedRiskNumber,
                        "combined_risk_percentage" to combinedRiskPercentage,
                        "calories" to dishCalories,
                        "percentage_of_daily_goal_calories" to caloriePercentage,
                        "calorie_category" to calorieCategory,
                        "sorting_score" to roundToTwoDecimal(sortingScore)
                    )
                )
            } else {
                // Default case for calorie-based recommendation
                recommendations.add(
                    mapOf(
                        "dish" to dish.Food_Name_and_Description,
                        "calories" to dishCalories,
                        "percentage_of_daily_goal_calories" to caloriePercentage,
                        "calorie_category" to calorieCategory
                    )
                )
            }
        }

        // Sort recommendations based on sorting score or calories
        recommendations.sortBy { it["sorting_score"] as? Double ?: Double.MAX_VALUE }

        return recommendations
    }

    // Function to return recommendations as a list
     fun getRecommendations(): List<Map<String, Any>> {
        val profile = ProfileRec.profileDetail.firstOrNull()

        if (profile == null) {
            Log.e("RecommendationSystem", "No profile details available.")
            return emptyList()
        }

        // Fetch the dish list
        val dishes = FoodRepository.dishList
        if (dishes.isEmpty()) {
            Log.e("RecommendationSystem", "No dishes available.")
            return emptyList()
        }

        // Generate and save recommendations
        recommendationsList.clear()
        recommendationsList.addAll(contentBasedRecommendation(profile, dishes))

        return recommendationsList
    }

    // Log recommendations for debugging or output
    fun logRecommendations() {
        val recommendations = getRecommendations()

        // Log the results
        Log.i("RecommendationSystem", "Recommended dishes based on your nutrient needs and calorie goals:")
        recommendations.forEach { recommendation ->
            Log.i("Recommendation System", "Dish: ${recommendation["dish"]} | Sorting Score: ${recommendation["sorting_score"]}")
            if (recommendation.containsKey("sodium_content")) {
                Log.i("Recommendation System", "Sodium Content: ${recommendation["sodium_content"]} mg, Percentage of Daily Value: ${recommendation["percentage_of_daily_value_sodium"]}%, Classification: ${recommendation["sodium_classification"]}")
                Log.i("Recommendation System", "Cholesterol Content: ${recommendation["cholesterol_content"]} mg, Percentage of Daily Value: ${recommendation["percentage_of_daily_value_cholesterol"]}%, Classification: ${recommendation["cholesterol_classification"]}")
                Log.i("Recommendation System", "Sodium + Cholesterol Content: ${recommendation["combined_risk_number"]} mg, Combined Percentage of Daily Value: ${recommendation["combined_risk_percentage"]}%")
            }
            Log.i("Recommendation System", "Calories: ${recommendation["calories"]} kcal, Percentage of Daily Goal: ${recommendation["percentage_of_daily_goal_calories"]}%, Category: ${recommendation["calorie_category"]}")
        }
    }
}
