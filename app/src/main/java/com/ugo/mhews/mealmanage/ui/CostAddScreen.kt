package com.ugo.mhews.mealmanage.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostAddScreen(
    modifier: Modifier = Modifier,
    viewModel: CostAddViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute,
        is24Hour = true
    )
    var showTimePicker by remember { mutableStateOf(false) }

    val vmState by viewModel.state.collectAsState()

    val formattedDateTime by derivedStateOf {
        val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
        val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val time = LocalTime.of(timeState.hour, timeState.minute)
        val dt = LocalDateTime.of(date, time)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Add Cost") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = costText,
                onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Cost") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(12.dp))

            Text(text = "Date & Time: $formattedDateTime", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { showDatePicker = true }) { Text("Select Date") }
                Button(onClick = { showTimePicker = true }) { Text("Select Time") }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val cost = costText.toDoubleOrNull()
                    val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = LocalTime.of(timeState.hour, timeState.minute)
                    val dt = LocalDateTime.of(date, time)
                    val epochMs = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    if (name.isBlank() || cost == null) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.submit(name.trim(), cost, epochMs) {
                        name = ""
                        costText = ""
                    }
                },
                enabled = !vmState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vmState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text("Submit")
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                title = { Text("Select Time") },
                text = {
                    TimePicker(state = timeState)
                }
            )
        }
    }

    // Snackbar from ViewModel
    LaunchedEffect(vmState.snackbar) {
        vmState.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }
}
