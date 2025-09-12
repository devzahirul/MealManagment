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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

private const val TAG = "MealScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(
    modifier: Modifier = Modifier,
    viewModel: MealViewModel = hiltViewModel()
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()

    val month = state.month
    val today = state.today
    val firstDow = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val monthLabelFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    fun canEdit(date: LocalDate) = !date.isBefore(today)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Meals") },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.onPrevMonth() }) {
                            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev month")
                        }
                        Text(month.format(monthLabelFmt), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { viewModel.onNextMonth() }) {
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }
                }
            )
        }
    ) { inner ->
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
                        val count = state.monthMeals[date] ?: 0
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
                                .then(if (clickable) Modifier.clickable { viewModel.openDate(date) } else Modifier),
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

        if (state.showEditorDate != null) {
            val date = state.showEditorDate!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissEditor() },
                title = { Text("Set meals for ${date}") },
                text = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { viewModel.decCount() }) { Text("-") }
                        Text(if (state.loadingCount) "…" else state.count.toString(), style = MaterialTheme.typography.headlineSmall)
                        OutlinedButton(onClick = { viewModel.incCount() }) { Text("+") }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveCount() }, enabled = !state.saving) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissEditor() }) { Text("Cancel") }
                }
            )
        }

        if (state.showDetailsDate != null) {
            val date = state.showDetailsDate!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissDetails() },
                confirmButton = { TextButton(onClick = { viewModel.dismissDetails() }) { Text("Close") } },
                title = { Text("Meals on $date") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            state.detailsLoading -> Text("Loading…")
                            state.detailsError != null -> Text("Error: ${state.detailsError}", color = MaterialTheme.colorScheme.error)
                            state.details.isEmpty() -> Text("No meals recorded")
                            else -> LazyColumn {
                                items(state.details) { item ->
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
    // Snackbar from ViewModel messages
    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbar.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }
}

private fun dayHeaders(first: DayOfWeek): List<String> {
    val order = (0..6).map { first.plus(it.toLong()) }
    return order.map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
}
