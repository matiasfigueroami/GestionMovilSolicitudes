package com.example.gestionmovilsolicitudes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionmovilsolicitudes.session.SessionManager
import com.example.gestionmovilsolicitudes.ui.login.LoginScreen
import com.example.gestionmovilsolicitudes.ui.menu.MenuScreen
import com.example.gestionmovilsolicitudes.ui.solicitud.ListaSolicitudesScreen
import com.example.gestionmovilsolicitudes.ui.solicitud.SolicitudFormScreen

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "lista"
    const val FORM = "form?id={id}"

    fun form(id: Long? = null) = if (id == null) "form" else "form?id=$id"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val usuario by SessionManager.usuario.collectAsState()

    // Si cambia la sesión: sin usuario -> login; con usuario estando en login -> menú
    LaunchedEffect(usuario) {
        val ruta = nav.currentBackStackEntry?.destination?.route
        if (usuario == null && ruta != Rutas.LOGIN) {
            nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
        } else if (usuario != null && ruta == Rutas.LOGIN) {
            nav.navigate(Rutas.MENU) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    NavHost(navController = nav, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) { LoginScreen() }

        composable(Rutas.MENU) {
            MenuScreen(
                onSolicitudes = { nav.navigate(Rutas.LISTA) },
                onCerrarSesion = { SessionManager.cerrar() }
            )
        }

        composable(Rutas.LISTA) {
            ListaSolicitudesScreen(
                onNueva = { nav.navigate(Rutas.form()) },
                onAbrir = { id -> nav.navigate(Rutas.form(id)) },
                onVolver = { nav.popBackStack() }
            )
        }

        composable(
            route = Rutas.FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: -1L
            SolicitudFormScreen(
                solicitudId = id.takeIf { it != -1L },
                onVolver = { nav.popBackStack() }
            )
        }
    }
}