package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.CostRepository

class AddCost(
    private val repo: CostRepository
) {
    suspend operator fun invoke(item: CostItem): Result<Unit> = repo.addCost(item)
}

class GetTotalCostForRange(
    private val repo: CostRepository
) {
    suspend operator fun invoke(startMs: Long, endMs: Long, uid: UserId? = null) =
        repo.getTotalCostForRange(startMs, endMs, uid)
}

class GetTotalsByUserForRange(
    private val repo: CostRepository
) {
    suspend operator fun invoke(startMs: Long, endMs: Long) =
        repo.getTotalsByUserForRange(startMs, endMs)
}

class GetCostsForUserRange(
    private val repo: CostRepository
) {
    suspend operator fun invoke(uid: UserId, startMs: Long, endMs: Long) =
        repo.getCostsForUserRange(uid, startMs, endMs)
}

