package com.ugo.mhews.mealmanage.data.cost;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ugo.mhews.mealmanage.core.di.IoDispatcher;
import com.ugo.mhews.mealmanage.domain.Result;
import com.ugo.mhews.mealmanage.domain.model.CostItem;
import com.ugo.mhews.mealmanage.domain.model.UserId;
import com.ugo.mhews.mealmanage.domain.repository.CostRepository;
import kotlinx.coroutines.CoroutineDispatcher;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0002\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u001f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\rH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eJ9\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00100\n2\n\u0010\u0011\u001a\u00060\u0012j\u0002`\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0015H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0017J7\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\n2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\u000e\u0010\u0011\u001a\n\u0018\u00010\u0012j\u0004\u0018\u0001`\u0013H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aJ7\u0010\u001b\u001a\u0018\u0012\u0014\u0012\u0012\u0012\b\u0012\u00060\u0012j\u0002`\u0013\u0012\u0004\u0012\u00020\u00190\u001c0\n2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0015H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001dR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001e"}, d2 = {"Lcom/ugo/mhews/mealmanage/data/cost/CostRepositoryImpl;", "Lcom/ugo/mhews/mealmanage/domain/repository/CostRepository;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "ioDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "(Lcom/google/firebase/firestore/FirebaseFirestore;Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineDispatcher;)V", "addCost", "Lcom/ugo/mhews/mealmanage/domain/Result;", "", "entry", "Lcom/ugo/mhews/mealmanage/domain/model/CostItem;", "(Lcom/ugo/mhews/mealmanage/domain/model/CostItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCostsForUserRange", "", "uid", "", "Lcom/ugo/mhews/mealmanage/domain/model/UserId;", "startMs", "", "endMs", "(Ljava/lang/String;JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalCostForRange", "", "(JJLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalsByUserForRange", "", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class CostRepositoryImpl implements com.ugo.mhews.mealmanage.domain.repository.CostRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.firestore.FirebaseFirestore db = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineDispatcher ioDispatcher = null;
    
    @javax.inject.Inject
    public CostRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore db, @org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth, @com.ugo.mhews.mealmanage.core.di.IoDispatcher
    @org.jetbrains.annotations.NotNull
    kotlinx.coroutines.CoroutineDispatcher ioDispatcher) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object addCost(@org.jetbrains.annotations.NotNull
    com.ugo.mhews.mealmanage.domain.model.CostItem entry, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<kotlin.Unit>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getTotalCostForRange(long startMs, long endMs, @org.jetbrains.annotations.Nullable
    java.lang.String uid, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<java.lang.Double>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getTotalsByUserForRange(long startMs, long endMs, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<? extends java.util.Map<java.lang.String, java.lang.Double>>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getCostsForUserRange(@org.jetbrains.annotations.NotNull
    java.lang.String uid, long startMs, long endMs, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<? extends java.util.List<com.ugo.mhews.mealmanage.domain.model.CostItem>>> $completion) {
        return null;
    }
}