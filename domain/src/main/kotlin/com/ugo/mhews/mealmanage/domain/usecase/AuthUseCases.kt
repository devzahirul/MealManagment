package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository

class SignIn(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserId> =
        authRepository.signIn(email, password)
}

class SignUp(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserId> =
        authRepository.signUp(email, password)
}

class SignOut(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.signOut()
    }
}

class GetCurrentUserId(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): UserId? = authRepository.currentUser()
}
