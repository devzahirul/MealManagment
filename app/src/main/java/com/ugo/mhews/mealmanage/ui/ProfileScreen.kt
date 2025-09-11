package com.ugo.mhews.mealmanage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ugo.mhews.mealmanage.data.UserProfile
import com.ugo.mhews.mealmanage.data.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    repository: UserRepository = UserRepository(),
    onSignedOut: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    fun load() {
        loading = true
        repository.getCurrentProfile { profile, err ->
            loading = false
            if (profile != null) {
                name = profile.name
                email = profile.email
            } else {
                scope.launch { snackbar.showSnackbar(err?.localizedMessage ?: "Failed to load profile") }
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium)
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Email: $email", style = MaterialTheme.typography.bodyMedium)

                Button(onClick = {
                    saving = true
                    repository.updateCurrentName(name.trim()) { ok, err ->
                        saving = false
                        scope.launch {
                            if (ok) snackbar.showSnackbar("Saved") else snackbar.showSnackbar("Error: ${err?.localizedMessage}")
                        }
                    }
                }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                    if (saving) CircularProgressIndicator()
                    else Text("Save")
                }

                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    scope.launch { snackbar.showSnackbar("Signed out") }
                    onSignedOut()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign Out")
                }
            }
        }
    }
}
