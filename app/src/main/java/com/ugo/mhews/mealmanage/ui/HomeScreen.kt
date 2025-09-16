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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val state by viewModel.state.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    var showUtilityDialog by remember { mutableStateOf(false) }
    var utilityName by remember { mutableStateOf("") }
    var utilityCostText by remember { mutableStateOf("") }
    var utilityPersonsText by remember { mutableStateOf(state.utilityPersons.toString()) }

    LaunchedEffect(state.utilityPersons) {
        utilityPersonsText = state.utilityPersons.toString()
    }

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
                text = monthFormatter.format(state.selectedMonth),
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = { showMonthPicker = true }) { Text("Select Month") }
        }

        StatCard(
            title = "This Month Total",
            subtitle = monthFormatter.format(state.selectedMonth),
            amount = state.monthTotal,
            loading = state.monthLoading,
            error = state.monthErr,
            currency = currency,
            onRefresh = { viewModel.refreshAll() },
            content = {
                val meals = state.monthMealsTotal
                if (!state.monthLoading && state.monthErr == null) {
                    Text("Total Meals: ${meals ?: 0}")
                    val cost = state.monthTotal ?: 0.0
                    val rate = if ((meals ?: 0) > 0) cost / (meals ?: 1) else null
                    Text(
                        text = "Meal Rate: " + (rate?.let { currency.format(it) } ?: "N/A"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Utility Card", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showUtilityDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add utility")
                    }
                }

                if (state.utilities.isEmpty()) {
                    Text("No utilities added yet", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.utilities.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.name)
                                Text(currency.format(entry.cost))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = utilityPersonsText,
                    onValueChange = { text ->
                        val filtered = text.filter { it.isDigit() }
                        utilityPersonsText = filtered
                        viewModel.updateUtilityPersons(filtered.toIntOrNull() ?: 0)
                    },
                    label = { Text("Total persons") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )

                Text("Total utility: ${currency.format(state.utilityTotal)}", style = MaterialTheme.typography.bodyMedium)
                Text("Per person: ${currency.format(state.utilityPerPerson)}", style = MaterialTheme.typography.bodyMedium)
            }
        }

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
                    state.todayMealsLoading -> Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                    state.todayMealsErr != null -> Text("Error: ${state.todayMealsErr}", color = MaterialTheme.colorScheme.error)
                    else -> {
                        Text("Total: ${state.todayMealsTotal}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        state.todayMealsTop.forEach { (name, cnt) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name.ifBlank { "Unknown" })
                                Text(cnt.toString())
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.loadTodayMeals() }) { Text("Refresh") }
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
                    state.byUserLoading -> Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                    state.byUserErr != null -> Text("Error: ${state.byUserErr}", color = MaterialTheme.colorScheme.error)
                    state.userTotals.isEmpty() -> Text("No data", style = MaterialTheme.typography.bodyMedium)
                    else -> {
                        state.userTotals.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectUser(item)
                                        viewModel.loadCostsForSelectedUser()
                                    }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.name.ifBlank { "Unknown" })
                                    Text(currency.format(item.total))
                                }
                                val userMeals = state.byUserMeals[item.uid] ?: 0
                                val totalMeals = state.monthMealsTotal ?: 0
                                val rate = if (!state.monthLoading && state.monthErr == null && totalMeals > 0) (state.monthTotal ?: 0.0) / totalMeals else null
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
                                if (index < state.userTotals.size - 1) {
                                    Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.loadByUser() }) { Text("Refresh") }
                }
            }
        }

        if (showMonthPicker) {
            var tempYear by remember { mutableStateOf(state.selectedMonth.year) }
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
                                        viewModel.setMonth(YearMonth.of(tempYear, m))
                                        showMonthPicker = false
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
        if (state.selectedUser != null) {
            val user = state.selectedUser!!
            AlertDialog(
                onDismissRequest = { viewModel.selectUser(null) },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectUser(null) }) { Text("Close") }
                },
                title = { Text("${user.name} - ${monthFormatter.format(state.selectedMonth)}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.userCostsLoading) {
                            Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                        } else if (state.userCostsErr != null) {
                            Text("Error: ${state.userCostsErr}", color = MaterialTheme.colorScheme.error)
                        } else if (state.userCosts.isEmpty()) {
                            Text("No entries")
                        } else {
                            val dtFmt = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
                            LazyColumn {
                                items(state.userCosts) { e ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(e.name, style = MaterialTheme.typography.bodyLarge)
                                            val dt = Instant.ofEpochMilli(e.timestampMillis).atZone(state.zone).toLocalDateTime()
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

        if (showUtilityDialog) {
            AlertDialog(
                onDismissRequest = { showUtilityDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val cost = utilityCostText.toDoubleOrNull()
                        if (utilityName.isNotBlank() && cost != null && cost > 0) {
                            viewModel.addUtility(utilityName.trim(), cost)
                            utilityName = ""
                            utilityCostText = ""
                            showUtilityDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showUtilityDialog = false
                        utilityName = ""
                        utilityCostText = ""
                    }) { Text("Cancel") }
                },
                title = { Text("Add Utility") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = utilityName,
                            onValueChange = { utilityName = it },
                            label = { Text("Utility name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = utilityCostText,
                            onValueChange = { utilityCostText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Amount") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                        )
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
    content: (@Composable () -> Unit)? = null
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
            if (content != null) {
                Spacer(Modifier.height(4.dp))
                content()
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
        }
    }
}
