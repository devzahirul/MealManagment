package com.ugo.mhews.mealmanage.data.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.ugo.mhews.mealmanage.core.di.IoDispatcher;
import com.ugo.mhews.mealmanage.domain.DomainError;
import com.ugo.mhews.mealmanage.domain.Result;
import com.ugo.mhews.mealmanage.domain.model.UserId;
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository;
import kotlinx.coroutines.CoroutineDispatcher;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0007\u001a\n\u0018\u00010\bj\u0004\u0018\u0001`\tH\u0016J+\u0010\n\u001a\f\u0012\b\u0012\u00060\bj\u0002`\t0\u000b2\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eJ\b\u0010\u000f\u001a\u00020\u0010H\u0016J+\u0010\u0011\u001a\f\u0012\b\u0012\u00060\bj\u0002`\t0\u000b2\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0012"}, d2 = {"Lcom/ugo/mhews/mealmanage/data/auth/AuthRepositoryImpl;", "Lcom/ugo/mhews/mealmanage/domain/repository/AuthRepository;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "ioDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "(Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineDispatcher;)V", "currentUser", "", "Lcom/ugo/mhews/mealmanage/domain/model/UserId;", "signIn", "Lcom/ugo/mhews/mealmanage/domain/Result;", "email", "password", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "signOut", "", "signUp", "data_debug"})
public final class AuthRepositoryImpl implements com.ugo.mhews.mealmanage.domain.repository.AuthRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineDispatcher ioDispatcher = null;
    
    @javax.inject.Inject
    public AuthRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth, @com.ugo.mhews.mealmanage.core.di.IoDispatcher
    @org.jetbrains.annotations.NotNull
    kotlinx.coroutines.CoroutineDispatcher ioDispatcher) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object signIn(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object signUp(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.String currentUser() {
        return null;
    }
    
    @java.lang.Override
    public void signOut() {
    }
}