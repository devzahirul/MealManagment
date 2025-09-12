package com.ugo.mhews.mealmanage.data.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ugo.mhews.mealmanage.domain.DomainError;
import com.ugo.mhews.mealmanage.domain.Result;
import com.ugo.mhews.mealmanage.domain.model.UserId;
import com.ugo.mhews.mealmanage.domain.model.UserProfile;
import com.ugo.mhews.mealmanage.domain.repository.UserRepository;
import javax.inject.Inject;
import com.ugo.mhews.mealmanage.core.di.IoDispatcher;
import kotlinx.coroutines.CoroutineDispatcher;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\fJ9\u0010\r\u001a\u0018\u0012\u0014\u0012\u0012\u0012\b\u0012\u00060\u000fj\u0002`\u0010\u0012\u0004\u0012\u00020\u000f0\u000e0\n2\u0010\u0010\u0011\u001a\f\u0012\b\u0012\u00060\u000fj\u0002`\u00100\u0012H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J\u001f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\n2\u0006\u0010\u0016\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0017J\b\u0010\u0018\u001a\u00020\u0019H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001a"}, d2 = {"Lcom/ugo/mhews/mealmanage/data/user/UserRepositoryImpl;", "Lcom/ugo/mhews/mealmanage/domain/repository/UserRepository;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "ioDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "(Lcom/google/firebase/firestore/FirebaseFirestore;Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineDispatcher;)V", "getCurrentProfile", "Lcom/ugo/mhews/mealmanage/domain/Result;", "Lcom/ugo/mhews/mealmanage/domain/model/UserProfile;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getNames", "", "", "Lcom/ugo/mhews/mealmanage/domain/model/UserId;", "uids", "", "(Ljava/util/Set;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCurrentName", "", "name", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "users", "Lcom/google/firebase/firestore/CollectionReference;", "data_debug"})
public final class UserRepositoryImpl implements com.ugo.mhews.mealmanage.domain.repository.UserRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.firestore.FirebaseFirestore db = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineDispatcher ioDispatcher = null;
    
    @javax.inject.Inject
    public UserRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore db, @org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth, @com.ugo.mhews.mealmanage.core.di.IoDispatcher
    @org.jetbrains.annotations.NotNull
    kotlinx.coroutines.CoroutineDispatcher ioDispatcher) {
        super();
    }
    
    private final com.google.firebase.firestore.CollectionReference users() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getCurrentProfile(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<com.ugo.mhews.mealmanage.domain.model.UserProfile>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object updateCurrentName(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<kotlin.Unit>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getNames(@org.jetbrains.annotations.NotNull
    java.util.Set<java.lang.String> uids, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<? extends java.util.Map<java.lang.String, java.lang.String>>> $completion) {
        return null;
    }
}