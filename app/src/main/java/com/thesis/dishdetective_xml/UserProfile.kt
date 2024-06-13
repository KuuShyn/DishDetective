package com.thesis.dishdetective_xml

data class UserProfile(
    val allergies: List<String>,
    val diabeticSafeFoods: List<String>
)

object UserProfileManager {
    private var currentUserProfile: UserProfile? = null

    fun setUserProfile(profile: UserProfile) {
        currentUserProfile = profile
    }

    fun getUserProfile(): UserProfile? {
        return currentUserProfile
    }
}