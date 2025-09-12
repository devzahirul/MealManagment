package com.ugo.mhews.mealmanage.domain.model

import java.time.LocalDate

typealias UserId = String

data class Meal(val date: LocalDate, val count: Int)

data class UserMeal(val uid: UserId, val count: Int)

data class CostItem(
    val name: String,
    val cost: Double,
    val timestampMillis: Long
)

data class UserProfile(val uid: UserId, val name: String, val email: String)

