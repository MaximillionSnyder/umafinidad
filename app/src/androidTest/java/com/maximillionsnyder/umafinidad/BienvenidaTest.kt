package com.maximillionsnyder.umafinidad

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.maximillionsnyder.umafinidad.data.PrefsRepository
import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.componentes.BienvenidaAccesibilidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BienvenidaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = contexto.applicationContext as Application

    @Before
    fun limpiarEstado() {
        PrefsRepository(contexto).apply {
            bienvenidaAccesibilidadVista = false
            tamanoTexto = TamanoTexto.NORMAL
            textoNegrita = false
            tema = ThemeMode.SISTEMA
        }
    }

    @Test
    fun primerInicioMuestraLaVentana() {
        val vm = AppViewModel(app)
        assertTrue(vm.mostrarBienvenida.value)
    }

    @Test
    fun guardarPersisteYOcultaLaVentana() {
        val vm = AppViewModel(app)
        vm.setTamanoTexto(TamanoTexto.MUY_GRANDE)
        vm.confirmarBienvenida()

        assertFalse(vm.mostrarBienvenida.value)
        assertEquals(TamanoTexto.MUY_GRANDE, vm.tamanoTexto.value)
        assertTrue(PrefsRepository(contexto).bienvenidaAccesibilidadVista)
    }

    @Test
    fun omitirRestauraElEstadoPrevio() {
        PrefsRepository(contexto).bienvenidaAccesibilidadVista = true
        val vm = AppViewModel(app)
        assertFalse(vm.mostrarBienvenida.value)

        vm.abrirBienvenida()
        assertTrue(vm.mostrarBienvenida.value)

        vm.setTamanoTexto(TamanoTexto.MUY_GRANDE)
        vm.setTema(ThemeMode.OSCURO)
        vm.omitirBienvenida()

        assertFalse(vm.mostrarBienvenida.value)
        assertEquals(TamanoTexto.NORMAL, vm.tamanoTexto.value)
        assertEquals(ThemeMode.SISTEMA, vm.tema.value)
        assertTrue(PrefsRepository(contexto).bienvenidaAccesibilidadVista)
    }

    @Test
    fun reabrirDesdeAjustesMuestraLaVentana() {
        PrefsRepository(contexto).bienvenidaAccesibilidadVista = true
        val vm = AppViewModel(app)
        assertFalse(vm.mostrarBienvenida.value)

        vm.abrirBienvenida()

        assertTrue(vm.mostrarBienvenida.value)
    }

    @Test
    fun controlesDeLaVentanaTienenUnSoloFoco() {
        composeRule.setContent {
            BienvenidaAccesibilidad(
                tema = ThemeMode.SISTEMA,
                onTema = {},
                tamanoTexto = TamanoTexto.NORMAL,
                onTamanoTexto = {},
                textoNegrita = false,
                onTextoNegrita = {},
                onGuardar = {},
                onOmitir = {},
            )
        }

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
        ).assertCountEquals(2)

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
        ).assertCountEquals(3)
    }
}
