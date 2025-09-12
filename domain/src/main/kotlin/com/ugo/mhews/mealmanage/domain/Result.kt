package com.ugo.mhews.mealmanage.domain

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error(val error: DomainError) : Result<Nothing>()
}

sealed class DomainError(open val message: String? = null, open val cause: Throwable? = null) {
    data class Auth(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
    data class PermissionDenied(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
    data class NotFound(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
    data class Network(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
    data class IndexRequired(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
    data class Unknown(override val message: String? = null, override val cause: Throwable? = null) : DomainError(message, cause)
}

