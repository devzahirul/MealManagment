package com.ugo.mhews.mealmanage.data.meal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ugo.mhews.mealmanage.domain.DomainError;
import com.ugo.mhews.mealmanage.domain.Result;
import com.ugo.mhews.mealmanage.domain.model.Meal;
import com.ugo.mhews.mealmanage.domain.model.UserId;
import com.ugo.mhews.mealmanage.domain.model.UserMeal;
import com.ugo.mhews.mealmanage.domain.repository.MealRepository;
import kotlinx.coroutines.flow.Flow;
import java.time.LocalDate;
import javax.inject.Inject;
import com.ugo.mhews.mealmanage.core.di.IoDispatcher;
import kotlinx.coroutines.CoroutineDispatcher;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J%\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J\u001f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J7\u0010\u0016\u001a\u0018\u0012\u0014\u0012\u0012\u0012\b\u0012\u00060\fj\u0002`\u0018\u0012\u0004\u0012\u00020\u00190\u00170\u00102\u0006\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u000eH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001cJ\'\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00190\u00102\u0006\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u000eH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001cJ0\u0010\u001e\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00190\u00170\u00100\u001f2\u0006\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u000eH\u0016J\'\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u00102\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\"\u001a\u00020\u0019H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010#R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006$"}, d2 = {"Lcom/ugo/mhews/mealmanage/data/meal/MealRepositoryImpl;", "Lcom/ugo/mhews/mealmanage/domain/repository/MealRepository;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "ioDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "(Lcom/google/firebase/firestore/FirebaseFirestore;Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineDispatcher;)V", "dayDoc", "Lcom/google/firebase/firestore/DocumentReference;", "uid", "", "date", "Ljava/time/LocalDate;", "getAllMealsForDate", "Lcom/ugo/mhews/mealmanage/domain/Result;", "", "Lcom/ugo/mhews/mealmanage/domain/model/UserMeal;", "(Ljava/time/LocalDate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMealForDate", "Lcom/ugo/mhews/mealmanage/domain/model/Meal;", "getMealsByUserForRange", "", "Lcom/ugo/mhews/mealmanage/domain/model/UserId;", "", "start", "end", "(Ljava/time/LocalDate;Ljava/time/LocalDate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalMealsForRange", "observeMealsForUserRange", "Lkotlinx/coroutines/flow/Flow;", "setMealForDate", "", "count", "(Ljava/time/LocalDate;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class MealRepositoryImpl implements com.ugo.mhews.mealmanage.domain.repository.MealRepository {
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.firestore.FirebaseFirestore db = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineDispatcher ioDispatcher = null;
    
    @javax.inject.Inject
    public MealRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.google.firebase.firestore.FirebaseFirestore db, @org.jetbrains.annotations.NotNull
    com.google.firebase.auth.FirebaseAuth auth, @com.ugo.mhews.mealmanage.core.di.IoDispatcher
    @org.jetbrains.annotations.NotNull
    kotlinx.coroutines.CoroutineDispatcher ioDispatcher) {
        super();
    }
    
    private final com.google.firebase.firestore.DocumentReference dayDoc(java.lang.String uid, java.time.LocalDate date) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getMealForDate(@org.jetbrains.annotations.NotNull
    java.time.LocalDate date, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<com.ugo.mhews.mealmanage.domain.model.Meal>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object setMealForDate(@org.jetbrains.annotations.NotNull
    java.time.LocalDate date, int count, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<kotlin.Unit>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<com.ugo.mhews.mealmanage.domain.Result<java.util.Map<java.time.LocalDate, java.lang.Integer>>> observeMealsForUserRange(@org.jetbrains.annotations.NotNull
    java.time.LocalDate start, @org.jetbrains.annotations.NotNull
    java.time.LocalDate end) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAllMealsForDate(@org.jetbrains.annotations.NotNull
    java.time.LocalDate date, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<? extends java.util.List<com.ugo.mhews.mealmanage.domain.model.UserMeal>>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getTotalMealsForRange(@org.jetbrains.annotations.NotNull
    java.time.LocalDate start, @org.jetbrains.annotations.NotNull
    java.time.LocalDate end, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<java.lang.Integer>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getMealsByUserForRange(@org.jetbrains.annotations.NotNull
    java.time.LocalDate start, @org.jetbrains.annotations.NotNull
    java.time.LocalDate end, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.ugo.mhews.mealmanage.domain.Result<? extends java.util.Map<java.lang.String, java.lang.Integer>>> $completion) {
        return null;
    }
}