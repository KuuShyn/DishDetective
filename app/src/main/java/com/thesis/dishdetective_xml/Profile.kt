package com.thesis.dishdetective_xml

data class Profile(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val heightFeet: Int = 0,
    val heightInches: Int = 0,
    val weight: Double = 0.0,
    val isAnemiaChecked: Boolean = false,
    val isCardioChecked: Boolean = false
)
