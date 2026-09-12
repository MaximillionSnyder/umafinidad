package com.maximillionsnyder.umafinidad

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.data.EstiloAvatar
import com.maximillionsnyder.umafinidad.data.Idioma
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AjustesSemanticaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val modelo by lazy { AffinityRepository(contexto).modelo }

    private fun componerAjustes() {
        composeRule.setContent {
            SettingsScreen(
                modoGrilla = ModoGrilla.TARJETAS,
                onModoGrilla = {},
                estiloAvatar = EstiloAvatar.COLOR,
                onEstiloAvatar = {},
                tema = ThemeMode.SISTEMA,
                onTema = {},
                idioma = Idioma.SISTEMA,
                onIdioma = {},
                modelo = modelo,
                japones = false,
                arboles = emptyList(),
                onAbrirArbol = {},
                onEliminarArbol = {},
                onAbrirGrupos = {},
                onAbrirRanking = {},
                onAbrirRankingPadres = {},
                onAbrirElenco = {},
                tamanoTexto = TamanoTexto.NORMAL,
                onTamanoTexto = {},
                textoNegrita = false,
                onTextoNegrita = {},
                onAbrirBienvenida = {},
            )
        }
    }

    @Test
    fun filaInterruptorTieneUnSoloFocoConRolSwitch() {
        componerAjustes()
        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch),
        ).assertCountEquals(1)
    }

    @Test
    fun opcionesTienenRolRadio() {
        componerAjustes()
        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
        ).assertCountEquals(16)
    }

    @Test
    fun acordeonAnunciaExpandido() {
        componerAjustes()
        val titulo = contexto.getString(R.string.accesibilidad_titulo)
        val expandido = contexto.getString(R.string.expandido)

        composeRule.onNodeWithText(titulo).performScrollTo().performClick()

        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expandido),
        ).assertExists()
    }
}
