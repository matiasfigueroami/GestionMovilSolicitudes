package com.example.gestionmovilsolicitudes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.session.SessionManager
import com.example.gestionmovilsolicitudes.ui.HomeTemporal
import com.example.gestionmovilsolicitudes.ui.login.LoginScreen

object Rutas {
    const val LOGIN = "login"
    const val CLIENTE = "cliente"
    const val ADMIN = "admin"
    const val TECNICO = "tecnico"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val usuario by SessionManager.usuario.collectAsState()

    // Cada vez que cambia la sesión, se va a la pantalla que corresponde al perfil
    LaunchedEffect(usuario) {
        val destino = when (usuario?.perfil) {
            null -> Rutas.LOGIN
            Perfil.CLIENTE -> Rutas.CLIENTE
            Perfil.ADMINISTRADOR -> Rutas.ADMIN
            Perfil.TECNICO -> Rutas.TECNICO
        }
        nav.navigate(destino) {
            popUpTo(nav.graph.id) { inclusive = true } // borra el historial: "atrás" no vuelve al login
        }
    }

    NavHost(navController = nav, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) { LoginScreen() }
        composable(Rutas.CLIENTE) { HomeTemporal("Panel cliente") }
        composable(Rutas.ADMIN) { HomeTemporal("Panel administrador") }
        composable(Rutas.TECNICO) { HomeTemporal("Panel técnico") }
    }
}