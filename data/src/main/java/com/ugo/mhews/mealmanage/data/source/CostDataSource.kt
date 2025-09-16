package com.ugo.mhews.mealmanage.data.source

import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId

interface CostDataSource {
    suspend fun addCost(userId: UserId?, item: CostItem)
    suspend fun totalCost(startMs: Long, endMs: Long, userId: UserId? = null): Double
    suspend fun totalsByUser(startMs: Long, endMs: Long): Map<UserId, Double>
    suspend fun costsForUser(userId: UserId, startMs: Long, endMs: Long): List<CostItem>
}
