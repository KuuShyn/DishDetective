package com.thesis.dishdetective_xml

data class UserPro (
    val age: Int,
    val weight: Float,
    val heightFeet: Int,
    val heightInches: Int,
    val gender: String,
    val weightGoal: String,
    val weightChangePerWeek: Float,

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

