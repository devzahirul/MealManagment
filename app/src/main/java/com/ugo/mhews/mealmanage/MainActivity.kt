package com.ugo.mhews.mealmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ugo.mhews.mealmanage.ui.theme.MealManageTheme
import com.ugo.mhews.mealmanage.ui.CostAddScreen
import com.ugo.mhews.mealmanage.ui.HomeScreen
import com.ugo.mhews.mealmanage.ui.LoginScreen
import com.ugo.mhews.mealmanage.ui.ProfileScreen
import com.ugo.mhews.mealmanage.ui.MealScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealManageTheme {
                var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

                if (!isLoggedIn) {
                    LoginScreen(onLoggedIn = { isLoggedIn = true })
                } else {
                    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == BottomTab.Home,
                                    onClick = { selectedTab = BottomTab.Home },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                    label = { androidx.compose.material3.Text("Home") }
                                )
                            NavigationBarItem(
                                selected = selectedTab == BottomTab.Cost,
                                onClick = { selectedTab = BottomTab.Cost },
                                icon = { Icon(Icons.Filled.AttachMoney, contentDescription = "Cost") },
                                label = { androidx.compose.material3.Text("Cost") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == BottomTab.Meal,
                                onClick = { selectedTab = BottomTab.Meal },
                                icon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Meal") },
                                label = { androidx.compose.material3.Text("Meal") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == BottomTab.Profile,
                                onClick = { selectedTab = BottomTab.Profile },
                                icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                                label = { androidx.compose.material3.Text("Profile") }
                            )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)) {
                            when (selectedTab) {
                                BottomTab.Home -> HomeScreen(modifier = Modifier.fillMaxSize())
                                BottomTab.Cost -> CostAddScreen(modifier = Modifier.fillMaxSize())
                                BottomTab.Meal -> MealScreen(modifier = Modifier.fillMaxSize())
                                BottomTab.Profile -> ProfileScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onSignedOut = {
                                        selectedTab = BottomTab.Home
                                        isLoggedIn = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MealManageTheme {
        LoginScreen(onLoggedIn = {})
    }
}

private enum class BottomTab { Home, Cost, Meal, Profile }
