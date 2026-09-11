package com.maximillionsnyder.umafinidad

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasToggleableState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.ui.QuitarResultado
import com.maximillionsnyder.umafinidad.ui.ToggleResultado
import com.maximillionsnyder.umafinidad.ui.compat.CompatScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccesibilidadSemanticaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val modelo by lazy { AffinityRepository(contexto).modelo }

    private fun componer(seleccion: List<Int?>) {
        composeRule.setContent {
            CompatScreen(
                modelo = modelo,
                seleccion = seleccion,
                resultado = null,
                modoGrilla = ModoGrilla.TARJETAS,
                japones = false,
                onToggle = { ToggleResultado.COLOCADO },
                onQuitarSlot = { QuitarResultado.OK },
                onConfirmarQuitarSoloHijo = {},
                onLimpiarTodo = {},
                avisar = {},
            )
        }
    }

    @Test
    fun tarjetaSeleccionadaExponeCheckboxConSlot() {
        composeRule.enableAccessibilityChecks()
        val personaje = modelo.personajes.first { it.playable == true && it.active == true }
        componer(List(7) { if (it == 0) personaje.charId else null })

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and
                hasToggleableState(ToggleableState.On),
        ).onFirst().assertExists()

        composeRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription),
        ).onFirst().assertExists()

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and
                hasToggleableState(ToggleableState.On),
        ).onFirst().tryPerformAccessibilityChecks()
    }

    @Test
    fun tarjetaSinSeleccionExponeCheckboxApagado() {
        composeRule.enableAccessibilityChecks()
        componer(List(7) { null })

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and
                hasToggleableState(ToggleableState.Off),
        ).onFirst().assertExists()

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and
                hasToggleableState(ToggleableState.Off),
        ).onFirst().tryPerformAccessibilityChecks()
    }
}
