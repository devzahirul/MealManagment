package com.ugo.mhews.mealmanage.data.source

import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile

interface UserDataSource {
    suspend fun loadProfile(userId: UserId): UserProfile?
    suspend fun saveProfile(userId: UserId, profile: UserProfile)
    suspend fun loadNames(userIds: Set<UserId>): Map<UserId, String>
}
