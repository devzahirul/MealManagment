package com.ugo.mhews.mealmanage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ugo.mhews.mealmanage.data.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository = AuthRepository(),
    onLoggedIn: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    fun doAuth() {
        val e = email.trim()
        val p = password
        if (!e.contains("@") || p.length < 6) {
            scope.launch { snackbar.showSnackbar("Enter valid email and 6+ char password") }
            return
        }
        isLoading = true
        val cb: (com.google.firebase.auth.FirebaseUser?, Throwable?) -> Unit = { user, err ->
            isLoading = false
            if (err != null || user == null) {
                scope.launch { snackbar.showSnackbar(err?.localizedMessage ?: "Auth failed") }
            } else {
                onLoggedIn()
            }
        }
        if (isSignUpMode) repository.signUp(e, p, cb) else repository.signIn(e, p, cb)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text(if (isSignUpMode) "Create Account" else "Login") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { doAuth() }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                if (isLoading) CircularProgressIndicator()
                else Text(if (isSignUpMode) "Create Account" else "Login")
            }

            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(if (isSignUpMode) "Have an account? Login" else "New here? Create account")
            }
        }
    }
}

