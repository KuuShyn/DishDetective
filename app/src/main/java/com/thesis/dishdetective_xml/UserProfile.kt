package com.thesis.dishdetective_xml

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

