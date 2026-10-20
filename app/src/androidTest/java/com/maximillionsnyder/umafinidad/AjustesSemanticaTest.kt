package com.maximillionsnyder.umafinidad

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.EstiloAvatar
import com.maximillionsnyder.umafinidad.data.Idioma
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.data.TamanoBurbuja
import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.ui.settings.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AjustesSemanticaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val modelo by lazy { AffinityRepository(contexto).modelo }

    /* Mismos testTag que pone SeccionDesplegable en la cabecera de cada
       sección; el acordeón arranca cerrado (seccionAbierta = null). */
    private companion object {
        const val SECCION_BURBUJA = "seccion_burbuja"
        const val SECCION_APARIENCIA = "seccion_apariencia"
        const val SECCION_ACCESIBILIDAD = "seccion_accesibilidad"
        const val SECCION_TEMA = "seccion_tema"
        const val SECCION_IDIOMA = "seccion_idioma"
    }

    private fun componerAjustes(arboles: List<ArbolGuardado> = emptyList()) {
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
                burbujaActiva = false,
                burbujaPermiso = false,
                onBurbuja = {},
                panelTranslucido = false,
                onPanelTranslucido = {},
                tamanoBurbuja = TamanoBurbuja.NORMAL,
                onTamanoBurbuja = {},
                onRestablecerTamanos = {},
                modelo = modelo,
                japones = false,
                arboles = arboles,
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

    /* Cuenta nodos del árbol semántico fusionado (el que anuncia el lector
       de pantalla), no del árbol crudo. */
    private fun nodosConRol(rol: Role): Int =
        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, rol),
        ).fetchSemanticsNodes().size

    private fun assertNodosConRol(rol: Role, esperado: Int, donde: String) {
        assertEquals("Nodos con rol $rol en $donde", esperado, nodosConRol(rol))
    }

    /* AnimatedVisibility(visible = false) no compone su contenido, así que
       primero hay que expandir la sección. Se toca la cabecera por testTag
       porque "Floating bubble" es también el título del primer interruptor. */
    private fun abrirSeccion(etiqueta: String) {
        composeRule.onNodeWithTag(etiqueta).performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(etiqueta).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                contexto.getString(R.string.expandido),
            ),
        )
    }

    @Test
    fun acordeonCerradoNoComponeControles() {
        componerAjustes()

        // Estado inicial: ninguna sección abierta, nada que anunciar todavía.
        assertNodosConRol(Role.Switch, 0, "el acordeón cerrado")
        assertNodosConRol(Role.RadioButton, 0, "el acordeón cerrado")
    }

    @Test
    fun filaInterruptorTieneUnSoloFocoConRolSwitch() {
        componerAjustes()

        abrirSeccion(SECCION_BURBUJA)
        // 3 FilaInterruptor (burbuja, panel translúcido, dos columnas). El
        // Switch interno (onCheckedChange = null) no aporta un segundo foco
        // con rol: si lo hiciera, acá habría 6 nodos.
        assertNodosConRol(Role.Switch, 3, "la sección Burbuja flotante")

        abrirSeccion(SECCION_ACCESIBILIDAD)
        // Al expandir accesibilidad se cierra burbuja: queda 1 interruptor
        // (texto en negrita), también con un único foco.
        assertNodosConRol(Role.Switch, 1, "la sección Accesibilidad")
    }

    @Test
    fun opcionesTienenRolRadio() {
        componerAjustes()

        // Una opción por valor del enum en cada sección. El acordeón deja una
        // sola abierta, así que se cuenta sección por sección.
        // La suma da 26 radios (4+5+3+4+10); el 16 del test viejo correspondía
        // a una UI sin burbuja y con 4 idiomas.
        abrirSeccion(SECCION_BURBUJA)
        assertNodosConRol(Role.RadioButton, TamanoBurbuja.entries.size, "Burbuja (tamaños)")

        abrirSeccion(SECCION_APARIENCIA)
        assertNodosConRol(
            Role.RadioButton,
            2 + EstiloAvatar.entries.size,
            "Apariencia (modo de grilla + avatares)",
        )

        abrirSeccion(SECCION_ACCESIBILIDAD)
        assertNodosConRol(Role.RadioButton, TamanoTexto.entries.size, "Accesibilidad (tamaños de texto)")

        abrirSeccion(SECCION_TEMA)
        assertNodosConRol(Role.RadioButton, ThemeMode.entries.size, "Tema")

        abrirSeccion(SECCION_IDIOMA)
        assertNodosConRol(Role.RadioButton, Idioma.entries.size, "Idioma")
    }

    @Test
    fun acordeonAnunciaExpandido() {
        componerAjustes()
        val expandido = contexto.getString(R.string.expandido)
        val contraido = contexto.getString(R.string.contraido)

        composeRule.onNodeWithTag(SECCION_ACCESIBILIDAD)
            .performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, contraido))

        abrirSeccion(SECCION_ACCESIBILIDAD)

        composeRule.onNodeWithTag(SECCION_ACCESIBILIDAD)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expandido))
        // El acordeón es excluyente: solo una sección anuncia "expandido".
        composeRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expandido),
        ).assertCountEquals(1)
    }

    /* Regresión del bug de interpolación: con un hijo que ya no está en el
       modelo se mostraba el template literal "#${a.hijoId}" (el "$" estaba
       escapado) en vez del id. */
    @Test
    fun arbolConHijoFueraDelModeloMuestraElId() {
        val idInexistente = modelo.personajes.maxOf { it.charId } + 1
        componerAjustes(
            arboles = listOf(
                ArbolGuardado(
                    id = 1L,
                    hijoId = idInexistente,
                    nombre = "Árbol de prueba",
                    seleccion = List<Int?>(7) { null },
                    total = 42,
                    creadoEn = 0L,
                ),
            ),
        )

        // La tarjeta muestra "nombreHijo · ◎ total".
        composeRule.onNodeWithText("#$idInexistente", substring = true).assertExists()

        // Si el fallback vuelve a escapar el dólar, la UI dice esto literal.
        composeRule.onAllNodesWithText("#${'$'}{a.hijoId}", substring = true)
            .assertCountEquals(0)
    }
}
