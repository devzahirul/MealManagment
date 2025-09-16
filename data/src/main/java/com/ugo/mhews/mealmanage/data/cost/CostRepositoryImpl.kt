package com.ugo.mhews.mealmanage.data.cost

import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.CostDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CostRepositoryImpl @Inject constructor(
    private val auth: AuthDataSource,
    private val costs: CostDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CostRepository {
    override suspend fun addCost(entry: CostItem): Result<Unit> = withContext(ioDispatcher) {
        try {
            costs.addCost(auth.currentUserId(), entry)
            Result.Success(Unit)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: UserId?): Result<Double> = withContext(ioDispatcher) {
        try {
            Result.Success(costs.totalCost(startMs, endMs, uid))
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long): Result<Map<UserId, Double>> = withContext(ioDispatcher) {
        try {
            Result.Success(costs.totalsByUser(startMs, endMs))
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getCostsForUserRange(uid: UserId, startMs: Long, endMs: Long): Result<List<CostItem>> = withContext(ioDispatcher) {
        try {
            Result.Success(costs.costsForUser(uid, startMs, endMs))
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }
}
