package com.ugo.mhews.mealmanage.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugo.mhews.mealmanage.data.MealRepository
import com.ugo.mhews.mealmanage.data.UserRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch

private const val TAG = "MealScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(
    modifier: Modifier = Modifier,
    repository: MealRepository = MealRepository()
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val firstDow = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val monthLabelFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    var showEditor by remember { mutableStateOf<LocalDate?>(null) }
    var showDetails by remember { mutableStateOf<LocalDate?>(null) }
    var count by remember { mutableStateOf(0) }
    var loadingCount by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    fun canEdit(date: LocalDate) = !date.isBefore(today)

    val userRepo = remember { UserRepository() }

    data class DetailItem(val uid: String, val name: String, val count: Int)
    var detailsLoading by remember { mutableStateOf(false) }
    var detailsErr by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf<List<DetailItem>>(emptyList()) }

    // Meals of current user for the visible month
    var monthMeals by remember { mutableStateOf<Map<LocalDate, Int>>(emptyMap()) }

    fun loadMonthMeals(target: YearMonth = month) {
        val start = target.atDay(1)
        val end = target.plusMonths(1).atDay(1)
        repository.getMealsForUserRange(start, end) { map, _ ->
            monthMeals = map
        }
    }

    fun openEditor(date: LocalDate) {
        if (!canEdit(date)) {
            // For past dates, show details for all users on that day
            showDetails = date
            detailsLoading = true
            detailsErr = null
            repository.getAllMealsForDate(date) { list, err ->
                if (err != null) {
                    Log.e(TAG, "getAllMealsForDate failed for $date", err)
                    detailsLoading = false
                    details = emptyList()
                    detailsErr = err.localizedMessage ?: "Failed to load"
                } else {
                    val uids = list.map { it.uid }.toSet()
                    userRepo.getNames(uids) { names, nErr ->
                        detailsLoading = false
                        if (nErr != null) {
                            Log.e(TAG, "getNames failed for $date", nErr)
                            detailsErr = nErr.localizedMessage
                        }
                        details = list.map { m ->
                            DetailItem(m.uid, names?.get(m.uid).orEmpty().ifBlank { "User ${m.uid.take(6)}" }, m.count)
                        }.sortedByDescending { it.count }
                    }
                }
            }
            return
        }
        showEditor = date
        loadingCount = true
        repository.getMealForDate(date) { entry, err ->
            loadingCount = false
            count = entry?.count ?: 0
            err?.let {
                Log.e(TAG, "getMealForDate failed for $date", it)
                scope.launch { snackbar.showSnackbar(it.localizedMessage ?: "Load failed") }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Meals") },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newMonth = month.minusMonths(1)
                            month = newMonth
                            loadMonthMeals(newMonth)
                        }) {
                            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev month")
                        }
                        Text(month.format(monthLabelFmt), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = {
                            val newMonth = month.plusMonths(1)
                            month = newMonth
                            loadMonthMeals(newMonth)
                        }) {
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }
                }
            )
        }
    ) { inner ->
        // Ensure we always fetch month data immediately on first render and when month changes
        LaunchedEffect(month) { loadMonthMeals(month) }

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Weekday headers
            val weekdays = dayHeaders(firstDow)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekdays.forEach { d ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(d, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Divider()

            // Calendar grid
            val firstOfMonth = month.atDay(1)
            val startGrid = firstOfMonth.with(TemporalAdjusters.previousOrSame(firstDow))
            val days = (0 until 42).map { startGrid.plusDays(it.toLong()) }
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    week.forEach { date ->
                        val inMonth = date.month == month.month
                        val isPast = date.isBefore(today)
                        val clickable = true
                        val textColor = if (inMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        val count = monthMeals[date] ?: 0
                        val hasMeal = count > 0
                        val bg = when {
                            hasMeal && !isPast -> Color(0xFFFFF59D) // Yellow 200 for current/future with meals
                            hasMeal && isPast -> Color(0xFFC8E6C9)  // Green 100 for past with meals
                            date == today -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .height(44.dp)
                                .background(bg, shape = CircleShape)
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                                .then(if (clickable) Modifier.clickable { openEditor(date) } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(date.dayOfMonth.toString(), color = textColor)
                            if (hasMeal) {
                                val badgeBg = MaterialTheme.colorScheme.secondary
                                val badgeFg = MaterialTheme.colorScheme.onSecondary
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(badgeBg, CircleShape)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = badgeFg,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditor != null) {
            val date = showEditor!!
            AlertDialog(
                onDismissRequest = { if (!saving) showEditor = null },
                title = { Text("Set meals for ${date}") },
                text = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (!loadingCount && count > 0) count -= 1 }) { Text("-") }
                        Text(if (loadingCount) "…" else count.toString(), style = MaterialTheme.typography.headlineSmall)
                        OutlinedButton(onClick = { if (!loadingCount) count += 1 }) { Text("+") }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        saving = true
                        repository.setMealForDate(date, count) { ok, err ->
                            saving = false
                            if (ok) {
                                Log.i(TAG, "Saved meal for $date = $count")
                                showEditor = null
                                scope.launch { snackbar.showSnackbar("Saved") }
                                // refresh month meals to update calendar colors
                                loadMonthMeals()
                            } else {
                                Log.e(TAG, "setMealForDate failed for $date (count=$count)", err)
                                scope.launch { snackbar.showSnackbar(err?.localizedMessage ?: "Save failed") }
                            }
                        }
                    }, enabled = !saving) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { if (!saving) showEditor = null }) { Text("Cancel") }
                }
            )
        }

        if (showDetails != null) {
            val date = showDetails!!
            AlertDialog(
                onDismissRequest = { showDetails = null },
                confirmButton = { TextButton(onClick = { showDetails = null }) { Text("Close") } },
                title = { Text("Meals on $date") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            detailsLoading -> Text("Loading…")
                            detailsErr != null -> Text("Error: $detailsErr", color = MaterialTheme.colorScheme.error)
                            details.isEmpty() -> Text("No meals recorded")
                            else -> LazyColumn {
                                items(details) { item ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.name)
                                        Text(item.count.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
    // Real-time listener for month meals; updates calendar instantly on changes
    DisposableEffect(month) {
        val start = month.atDay(1)
        val end = month.plusMonths(1).atDay(1)
        val reg = repository.observeMealsForUserRange(start, end) { map, err ->
            if (err != null) {
                // Log and fall back to a one-shot fetch so UI still updates
                Log.e(TAG, "observeMealsForUserRange failed for $start..$end", err)
                repository.getMealsForUserRange(start, end) { fallback, _ ->
                    monthMeals = fallback
                }
                // Let user know realtime updates are temporarily unavailable
                scope.launch {
                    snackbar.showSnackbar(err.localizedMessage ?: "Live updates unavailable; loaded once")
                }
            } else if (map != null) {
                monthMeals = map
            }
        }
        onDispose { reg?.remove() }
    }
}

private fun dayHeaders(first: DayOfWeek): List<String> {
    val order = (0..6).map { first.plus(it.toLong()) }
    return order.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
}
