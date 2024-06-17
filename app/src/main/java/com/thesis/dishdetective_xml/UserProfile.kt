package com.thesis.dishdetective_xml

import kotlinx.serialization.Serializable

class UserProfile {
    @Serializable
    data class UProfile(
        val id: Int,
        val age: String,
        val weight: String,
        val height: String,
        val gender: String,
        val weightGoal: String,
        val weightChangePerWeek: String
    )

    @Serializable
    data class UConcerns(
        val id: Int,
        val allergy: Boolean,
        val allergies: String,
        val anemia: String,
        val osteoporosis: String,
        val highProtein: String,
        val lowSugar: String
    )
}

data class UserPro(
    val allergies: List<String>,
    val diabeticSafeFoods: List<String>
)

object UserProfileManager {
    private var currentUserProfile: UserPro? = null

    fun setUserProfile(profile: UserPro) {
        currentUserProfile = profile
    }

    fun getUserProfile(): UserPro? {
        return currentUserProfile
    }
}

