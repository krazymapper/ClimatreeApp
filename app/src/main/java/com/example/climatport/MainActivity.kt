package com.example.climatport

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.climatport.data.FirebaseService
import com.example.climatport.ui.screens.LoginScreen
import com.example.climatport.ui.screens.MainScreen
import com.example.climatport.ui.screens.SignUpScreen
import com.example.climatport.ui.theme.ClimatportTheme

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Permission granted
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only coarse location granted
            }
            else -> {
                // No location access granted
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request location permissions
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }

        setContent {
            ClimatportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val firebaseService = remember { FirebaseService() }

    when (currentScreen) {
        Screen.Login -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.Main },
            onSignUpClick = { currentScreen = Screen.SignUp }
        )
        Screen.SignUp -> SignUpScreen(
            onSignUpSuccess = { currentScreen = Screen.Main },
            onBackClick = { currentScreen = Screen.Login }
        )
        Screen.Main -> MainScreen(
            onLogout = {
                firebaseService.signOut()
                currentScreen = Screen.Login
            }
        )
    }
}

sealed class Screen {
    object Login : Screen()
    object SignUp : Screen()
    object Main : Screen()
}