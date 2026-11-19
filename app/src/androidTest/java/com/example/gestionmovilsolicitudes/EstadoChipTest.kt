package com.example.gestionmovilsolicitudes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.ui.solicitud.EstadoChip
import com.example.gestionmovilsolicitudes.ui.theme.GestionMovilSolicitudesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba de la vista: el chip muestra el nombre del estado de la solicitud. */
@RunWith(AndroidJUnit4::class)
class EstadoChipTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraElNombreDelEstado() {
        composeRule.setContent {
            GestionMovilSolicitudesTheme {
                EstadoChip(EstadoSolicitud.RECIBIDA)
            }
        }

        composeRule.onNodeWithText("Recibida").assertIsDisplayed()
    }
}
