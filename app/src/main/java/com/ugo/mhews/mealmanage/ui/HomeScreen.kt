package com.ugo.mhews.mealmanage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ugo.mhews.mealmanage.data.CostRepository
import com.ugo.mhews.mealmanage.data.UserRepository
import com.ugo.mhews.mealmanage.data.MealRepository
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    repository: CostRepository = CostRepository()
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()

    val userRepo = remember { UserRepository() }
    val mealRepo = remember { MealRepository() }

    var monthTotal by remember { mutableStateOf<Double?>(null) }
    var monthLoading by remember { mutableStateOf(true) }
    var monthErr by remember { mutableStateOf<String?>(null) }
    var monthMealsTotal by remember { mutableStateOf<Int?>(null) }

    data class UserTotal(val uid: String, val name: String, val total: Double)
    var userTotals by remember { mutableStateOf<List<UserTotal>>(emptyList()) }
    var byUserLoading by remember { mutableStateOf(true) }
    var byUserErr by remember { mutableStateOf<String?>(null) }

    var selectedUser: UserTotal? by remember { mutableStateOf(null) }
    var userCosts by remember { mutableStateOf<List<com.ugo.mhews.mealmanage.data.CostEntry>>(emptyList()) }
    var userCostsLoading by remember { mutableStateOf(false) }
    var userCostsErr by remember { mutableStateOf<String?>(null) }
    var byUserMeals by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Today's meals (all users)
    var todayMealsLoading by remember { mutableStateOf(true) }
    var todayMealsErr by remember { mutableStateOf<String?>(null) }
    var todayMealsTotal by remember { mutableStateOf(0) }
    var todayMealsTop by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    fun loadMonth() {
        monthLoading = true
        monthErr = null
        monthMealsTotal = null
        val ym = selectedMonth
        val startMs = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val startDate = ym.atDay(1)
        val endDate = ym.plusMonths(1).atDay(1)

        repository.getTotalCostForRange(startMs, endMs, null) { sum, err ->
            if (err != null) {
                monthErr = err.localizedMessage ?: "Unknown error"
                monthTotal = null
            } else monthTotal = sum

            // After cost loads, load meals
            mealRepo.getTotalMealsForRange(startDate, endDate) { totalMeals, mErr ->
                if (mErr != null) {
                    monthErr = monthErr ?: mErr.localizedMessage ?: "Unknown error"
                    monthMealsTotal = null
                } else monthMealsTotal = totalMeals
                monthLoading = false
            }
        }
    }

    fun refreshAll() { loadMonth() }

    fun loadTodayMeals() {
        todayMealsLoading = true
        todayMealsErr = null
        val date = LocalDate.now()
        mealRepo.getAllMealsForDate(date) { list, err ->
            if (err != null) {
                todayMealsLoading = false
                todayMealsTotal = 0
                todayMealsTop = emptyList()
                todayMealsErr = err.localizedMessage ?: "Unknown error"
            } else {
                val total = list.sumOf { it.count }
                val uids = list.map { it.uid }.toSet()
                userRepo.getNames(uids) { names, nErr ->
                    todayMealsLoading = false
                    if (nErr != null) todayMealsErr = nErr.localizedMessage
                    val top = list.map { m ->
                        val name = names?.get(m.uid).orEmpty().ifBlank { "User ${m.uid.take(6)}" }
                        name to m.count
                    }.sortedByDescending { it.second }
                    todayMealsTotal = total
                    todayMealsTop = top
                }
            }
        }
    }

    fun loadByUser() {
        byUserLoading = true
        byUserErr = null
        val ym = selectedMonth
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        repository.getTotalsByUserForRange(start, end) { sums, err ->
            if (err != null) {
                byUserLoading = false
                userTotals = emptyList()
                byUserErr = err.localizedMessage ?: "Unknown error"
            } else {
                val uids = sums.keys
                var remaining = 2
                fun done() { remaining -= 1; if (remaining == 0) byUserLoading = false }

                userRepo.getNames(uids.toSet()) { names, nErr ->
                    if (nErr != null) {
                        byUserErr = nErr.localizedMessage ?: "Unknown error"
                    }
                    val list = sums.entries.map { (uid, total) ->
                        val name = names?.get(uid).orEmpty()
                            .ifBlank { "User ${uid.take(6)}" }
                        UserTotal(uid, name, total)
                    }.sortedByDescending { it.total }
                    userTotals = list
                    done()
                }

                val startDate = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
                val endDate = Instant.ofEpochMilli(end).atZone(zone).toLocalDate()
                mealRepo.getTotalMealsForRange(startDate, endDate) { _, _ -> /* ensure index warm-up */ }
                mealRepo.getTotalMealsForRange(startDate, endDate) { _, _ -> /* noop */ }
                mealRepo.getTotalMealsForRange(startDate, endDate) { _, _ -> /* noop */ }
                // fetch meals by user
                mealRepo.getMealsByUserForRange(startDate, endDate) { mealsMap, mErr ->
                    if (mErr != null) {
                        byUserErr = byUserErr ?: mErr.localizedMessage ?: "Unknown error"
                        byUserMeals = emptyMap()
                    } else byUserMeals = mealsMap
                    done()
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshAll(); loadByUser(); loadTodayMeals() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with current/selected month and selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = monthFormatter.format(selectedMonth),
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = { showMonthPicker = true }) { Text("Select Month") }
        }

        StatCard(
            title = "This Month Total",
            subtitle = monthFormatter.format(selectedMonth),
            amount = monthTotal,
            loading = monthLoading,
            error = monthErr,
            currency = currency,
            onRefresh = { loadMonth() },
            extraContent = {
                val meals = monthMealsTotal
                if (!monthLoading && monthErr == null) {
                    Text("Total Meals: ${meals ?: 0}")
                    val cost = monthTotal ?: 0.0
                    val rate = if ((meals ?: 0) > 0) cost / (meals ?: 1) else null
                    Text(
                        text = "Meal Rate: " + (rate?.let { currency.format(it) } ?: "N/A"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )

        // Today's Meals card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Today's Meals", style = MaterialTheme.typography.titleMedium)
                when {
                    todayMealsLoading -> Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                    todayMealsErr != null -> Text("Error: $todayMealsErr", color = MaterialTheme.colorScheme.error)
                    else -> {
                        Text("Total: $todayMealsTotal", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        todayMealsTop.forEach { (name, cnt) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name.ifBlank { "Unknown" })
                                Text(cnt.toString())
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { loadTodayMeals() }) { Text("Refresh") }
                }
            }
        }

        // By User card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("This Month by User", style = MaterialTheme.typography.titleMedium)
                when {
                    byUserLoading -> Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                    byUserErr != null -> Text("Error: $byUserErr", color = MaterialTheme.colorScheme.error)
                    userTotals.isEmpty() -> Text("No data", style = MaterialTheme.typography.bodyMedium)
                    else -> {
                        userTotals.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUser = item
                                        val ym = selectedMonth
                                        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                                        val end = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                                        userCostsLoading = true
                                        userCostsErr = null
                                        repository.getCostsForUserRange(item.uid, start, end) { list, err ->
                                            userCostsLoading = false
                                            if (err != null) {
                                                userCosts = emptyList()
                                                userCostsErr = err.localizedMessage ?: "Unknown error"
                                            } else {
                                                userCosts = list
                                            }
                                        }
                                    }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.name.ifBlank { "Unknown" })
                                    Text(currency.format(item.total))
                                }
                                val userMeals = byUserMeals[item.uid] ?: 0
                                val totalMeals = monthMealsTotal ?: 0
                                val rate = if (!monthLoading && monthErr == null && totalMeals > 0) (monthTotal ?: 0.0) / totalMeals else null
                                val expected = rate?.times(userMeals)
                                val balance = expected?.let { item.total - it }
                                Text(
                                    text = if (rate != null) {
                                        "Meals: $userMeals × Rate ${currency.format(rate)} = ${currency.format(expected ?: 0.0)}"
                                    } else {
                                        "Meals: $userMeals"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (balance != null) {
                                    val balColor = when {
                                        balance > 0.0 -> Color(0xFF2E7D32) // green
                                        balance < 0.0 -> Color(0xFFC62828) // red
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Text(
                                        text = "Balance: ${currency.format(balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = balColor
                                    )
                                }
                                if (index < userTotals.size - 1) {
                                    Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        // reload
                        loadByUser()
                    }) { Text("Refresh") }
                }
            }
        }

        if (showMonthPicker) {
            var tempYear by remember { mutableStateOf(selectedMonth.year) }
            AlertDialog(
                onDismissRequest = { showMonthPicker = false },
                confirmButton = {
                    TextButton(onClick = { showMonthPicker = false }) { Text("Close") }
                },
                title = { Text("Select Month & Year") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            IconButton(onClick = { tempYear -= 1 }) { Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev Year") }
                            Text(text = tempYear.toString(), style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { tempYear += 1 }) { Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Year") }
                        }

                        val months = listOf(
                            Month.JANUARY, Month.FEBRUARY, Month.MARCH,
                            Month.APRIL, Month.MAY, Month.JUNE,
                            Month.JULY, Month.AUGUST, Month.SEPTEMBER,
                            Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER
                        )
                        for (row in 0 until 4) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    val m = months[index]
                                    TextButton(onClick = {
                                        selectedMonth = YearMonth.of(tempYear, m)
                                        showMonthPicker = false
                                        loadMonth(); loadByUser()
                                    }, modifier = Modifier.weight(1f)) {
                                        Text(m.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // Detail dialog for selected user's costs in the month
        if (selectedUser != null) {
            val user = selectedUser!!
            AlertDialog(
                onDismissRequest = { selectedUser = null },
                confirmButton = {
                    TextButton(onClick = { selectedUser = null }) { Text("Close") }
                },
                title = { Text("${user.name} - ${monthFormatter.format(selectedMonth)}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (userCostsLoading) {
                            Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                        } else if (userCostsErr != null) {
                            Text("Error: $userCostsErr", color = MaterialTheme.colorScheme.error)
                        } else if (userCosts.isEmpty()) {
                            Text("No entries")
                        } else {
                            val dtFmt = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
                            LazyColumn {
                                items(userCosts) { e ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(e.name, style = MaterialTheme.typography.bodyLarge)
                                            val dt = Instant.ofEpochMilli(e.timestampMillis).atZone(zone).toLocalDateTime()
                                            Text(dt.format(dtFmt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(currency.format(e.cost))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun StatCard(
    title: String,
    subtitle: String? = null,
    amount: Double?,
    loading: Boolean,
    error: String?,
    currency: NumberFormat,
    onRefresh: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when {
                loading -> Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                else -> Text(
                    text = currency.format(amount ?: 0.0),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (extraContent != null) {
                Spacer(Modifier.height(4.dp))
                extraContent()
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
        }
    }
}
