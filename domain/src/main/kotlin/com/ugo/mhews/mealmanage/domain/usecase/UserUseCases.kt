package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import com.ugo.mhews.mealmanage.domain.repository.UserRepository

class GetCurrentProfile(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserProfile> = userRepository.getCurrentProfile()
}

class UpdateCurrentName(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): Result<Unit> = userRepository.updateCurrentName(name)
}

class GetUserNames(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uids: Set<UserId>): Result<Map<UserId, String>> =
        userRepository.getNames(uids)
}
