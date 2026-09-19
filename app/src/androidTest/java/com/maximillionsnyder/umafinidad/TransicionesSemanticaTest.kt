package com.maximillionsnyder.umafinidad

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
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
import com.maximillionsnyder.umafinidad.ui.componentes.ClavesTransicion
import com.maximillionsnyder.umafinidad.ui.componentes.LocalAnimatedVisibilityScope
import com.maximillionsnyder.umafinidad.ui.componentes.LocalSharedTransitionScope
import com.maximillionsnyder.umafinidad.ui.groups.GroupsScreen
import com.maximillionsnyder.umafinidad.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/* Verifica que el host de transiciones (mismo patrón que MainActivity)
   conserve la semántica al abrir y cerrar un overlay desde Ajustes. */
@RunWith(AndroidJUnit4::class)
class TransicionesSemanticaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val modelo by lazy { AffinityRepository(contexto).modelo }

    private fun componerHost() {
        composeRule.setContent {
            var destino by remember { mutableStateOf<String?>(null) }
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                val scopeCompartido = this
                AnimatedContent(
                    targetState = destino,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                    label = "destinoTest",
                ) { actual ->
                    val scopeVisibilidad = this
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides scopeCompartido,
                        LocalAnimatedVisibilityScope provides scopeVisibilidad,
                    ) {
                        if (actual == null) {
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
                                onAbrirGrupos = { destino = ClavesTransicion.OVERLAY_GRUPOS },
                                onAbrirRanking = {},
                                onAbrirRankingPadres = {},
                                onAbrirElenco = {},
                                tamanoTexto = TamanoTexto.NORMAL,
                                onTamanoTexto = {},
                                textoNegrita = false,
                                onTextoNegrita = {},
                                onAbrirBienvenida = {},
                            )
                        } else {
                            GroupsScreen(modelo = modelo, japones = false, onVolver = { destino = null })
                        }
                    }
                }
            }
        }
    }

    @Test
    fun abrirYCerrarGruposConservaSemantica() {
        componerHost()
        val tituloGrupos = contexto.getString(R.string.tab_groups)

        composeRule.onAllNodesWithText(tituloGrupos).onFirst().performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit),
        ).onFirst().assertExists()

        composeRule.onNodeWithContentDescription(contexto.getString(R.string.volver)).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(tituloGrupos).onFirst().assertIsDisplayed()
    }
}
