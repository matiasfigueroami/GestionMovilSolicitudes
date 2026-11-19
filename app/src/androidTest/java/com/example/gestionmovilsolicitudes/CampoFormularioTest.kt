package com.example.gestionmovilsolicitudes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionmovilsolicitudes.ui.componentes.CampoFormulario
import com.example.gestionmovilsolicitudes.ui.theme.GestionMovilSolicitudesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba de la vista: el campo del formulario muestra su etiqueta y el mensaje de error. */
@RunWith(AndroidJUnit4::class)
class CampoFormularioTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraLaEtiquetaYElMensajeDeError() {
        composeRule.setContent {
            GestionMovilSolicitudesTheme {
                CampoFormulario(
                    value = "",
                    onValueChange = {},
                    label = "Título",
                    icono = Icons.Default.Create,
                    error = "El título es obligatorio"
                )
            }
        }

        composeRule.onNodeWithText("Título").assertIsDisplayed()
        composeRule.onNodeWithText("El título es obligatorio").assertIsDisplayed()
    }
}
