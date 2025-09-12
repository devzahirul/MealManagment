package com.ugo.mhews.mealmanage.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

interface DispatchersProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

object DefaultDispatchersProvider : DispatchersProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}

interface DateProvider {
    fun today(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate
}

class SystemDateProvider(private val clock: Clock = Clock.systemDefaultZone()) : DateProvider {
    override fun today(zoneId: ZoneId): LocalDate = LocalDate.now(clock.withZone(zoneId))
}

