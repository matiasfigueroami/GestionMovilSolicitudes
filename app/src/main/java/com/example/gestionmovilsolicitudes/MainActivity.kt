package com.example.gestionmovilsolicitudes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.gestionmovilsolicitudes.ui.navigation.AppNavigation
import com.example.gestionmovilsolicitudes.ui.theme.GestionMovilSolicitudesTheme
import androidx.compose.foundation.layout.consumeWindowInsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionMovilSolicitudesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    androidx.compose.foundation.layout.Box(Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}