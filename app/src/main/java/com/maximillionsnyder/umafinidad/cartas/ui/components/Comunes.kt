package com.maximillionsnyder.umafinidad.cartas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.cartas.domain.CartaApoyo
import com.maximillionsnyder.umafinidad.cartas.domain.CartaPersonaje
import com.maximillionsnyder.umafinidad.cartas.domain.Skill
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaKind
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaRareza
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipo
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipoApoyo
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.LocalIdioma
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.t
import java.text.Collator
import java.util.Locale

/* Las etiquetas de texto (tipos, kinds, apoyos, obtención) viven en
   domain/Etiquetas.kt: son texto puro y allí se prueban; se les pasa el idioma
   de los datos. Aquí queda el color, que sí es de Compose, y los textos propios
   de la interfaz, que salen de ui/i18n/TextosUi.kt. */

fun colorRareza(rarity: Int?): Color = when (rarity) {
    1 -> Color(0xFF9CA3AF)
    2 -> Color(0xFFD4A017)
    3, 5 -> Color(0xFFEC4899)
    4, 6 -> Color(0xFF8B5CF6)
    else -> Color(0xFF6B7280)
}

/* Fuentes de una skill con el color de cada tipo: los mismos colores que usan
   los chips de filtro, así se ve de un vistazo de dónde sale la skill. */
@Composable
fun ResumenFuentes(skill: Skill, modifier: Modifier = Modifier) {
    val partes = buildList {
        val f = skill.sources
        /* Los contadores de una sola fuente van en singular en inglés ("1 card",
           "1 hint"); las otras tres lenguas no distinguen número. */
        if (f.characterCards.isNotEmpty()) {
            val n = f.characterCards.size
            add(t(TX_UI, if (n == 1) "fuente_personaje_uno" else "fuente_personajes", n) to ColoresPersonaje)
        }
        if (f.characterEvents.isNotEmpty()) add(t(TX_UI, "fuente_eventos_personaje", f.characterEvents.size) to ColoresEventoPersonaje)
        if (f.supportHints.isNotEmpty()) {
            val n = f.supportHints.size
            add(t(TX_UI, if (n == 1) "fuente_hint_uno" else "fuente_hints", n) to ColoresHint)
        }
        if (f.supportEvents.isNotEmpty()) add(t(TX_UI, "fuente_eventos_apoyo", f.supportEvents.size) to ColoresEventoApoyo)
        if (f.scenarioEvents.isNotEmpty()) add(t(TX_UI, "fuente_escenarios", f.scenarioEvents.size) to ColoresEscenario)
    }
    if (partes.isEmpty()) {
        Text(
            t(TX_UI, "sin_fuentes"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    val separador = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val texto = buildAnnotatedString {
        partes.forEachIndexed { indice, (etiqueta, colores) ->
            if (indice > 0) withStyle(SpanStyle(color = separador)) { append(" · ") }
            withStyle(SpanStyle(color = colorFuente(colores))) { append(etiqueta) }
        }
    }
    Text(
        texto,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/* En tema oscuro conviene el tono claro de la paleta y en claro el oscuro,
   para que el texto se lea sobre la tarjeta. */
@Composable
private fun colorFuente(colores: List<Color>): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) colores.last() else colores.first()

@Composable
fun EtiquetaRareza(rarity: Int?) {
    Text(
        text = etiquetaRareza(rarity, LocalIdioma.current),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(colorRareza(rarity), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun Estrellas(rarity: Int?) {
    val cantidad = (rarity ?: 0).coerceIn(0, 3)
    if (cantidad > 0) {
        Text("★".repeat(cantidad), color = Color(0xFFD4A017), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SeccionTitulo(texto: String, contador: Int? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /* Barrita de color: separa las secciones de la ficha sin llenarlas de cajas. */
        Box(
            Modifier
                .padding(end = 8.dp)
                .size(width = 3.dp, height = 16.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(texto, style = MaterialTheme.typography.titleMedium)
        if (contador != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                contador.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EstadoVacio(texto: String, detalle: String? = null, icono: Int? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icono != null) {
            Icon(
                painterResource(icono),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(texto, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
        if (detalle != null) {
            Text(
                detalle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/* Estado vacío de las listas cuando nada coincide con la búsqueda o los filtros. */
@Composable
fun EstadoVacioBusqueda(detalle: String? = null) {
    EstadoVacio(
        t(TX_UI, "sin_resultados"),
        detalle ?: t(TX_UI, "sin_resultados_detalle"),
        R.drawable.ic_buscar,
    )
}

/* Colador en español para ordenar por nombre: "Á" va con la A y la "ñ" después
   de la "n", como espera cualquiera que lea en español. */
val COLADOR_ES: Collator = Collator.getInstance(Locale("es", "ES"))

/* Radio y borde comunes de las tarjetas: listas y fichas comparten el mismo estilo. */
val FORMA_TARJETA = RoundedCornerShape(16.dp)

@Composable
fun TarjetaApp(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val completo = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, FORMA_TARJETA)
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    if (onClick == null) {
        Card(modifier = completo, shape = FORMA_TARJETA, colors = colores, content = contenido)
    } else {
        Card(onClick = onClick, modifier = completo, shape = FORMA_TARJETA, colors = colores, content = contenido)
    }
}

@Composable
fun BarraDetalle(
    titulo: String,
    onVolver: () -> Unit,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_atras), contentDescription = t(TX_UI, "volver"))
        }
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        acciones()
    }
}

@Composable
fun Etiqueta(texto: String, color: Color = MaterialTheme.colorScheme.secondaryContainer, colorTexto: Color = MaterialTheme.colorScheme.onSecondaryContainer) {
    Text(
        texto,
        color = colorTexto,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/* Muestra de qué tipo es una carta de apoyo (Velocidad, Aguante, Wit…) con la
   paleta de ese tipo, para distinguirla de un vistazo en las listas. */
@Composable
fun EtiquetaTipoApoyo(type: String?) {
    val colores = coloresTipoApoyo(type)
    val forma = RoundedCornerShape(6.dp)
    Text(
        text = etiquetaTipoApoyo(type, LocalIdioma.current),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(forma)
            .background(Brush.horizontalGradient(colores))
            .border(1.dp, colores.first(), forma)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

val ColoresVelocidad = listOf(Color(0xFF1565C0), Color(0xFF00ACC1))
val ColoresAceleracion = listOf(Color(0xFFE65100), Color(0xFFFFB300))
val ColoresTodas = listOf(Color(0xFF37474F), Color(0xFF78909C))
val ColoresNormal = listOf(Color(0xFF616161), Color(0xFFBDBDBD))
val ColoresRara = listOf(Color(0xFFB8860B), Color(0xFFD4A017))
val ColoresUnica = listOf(Color(0xFFAD1457), Color(0xFFEC4899))
val ColoresEvolucion = listOf(Color(0xFF6A1B9A), Color(0xFF8B5CF6))
val ColoresDebuff = listOf(Color(0xFFB71C1C), Color(0xFFEF5350))
val ColoresPersonaje = listOf(Color(0xFF283593), Color(0xFF5C6BC0))
val ColoresEventoPersonaje = listOf(Color(0xFF4527A0), Color(0xFF9575CD))
val ColoresHint = listOf(Color(0xFF00838F), Color(0xFF4DD0E1))
val ColoresEventoApoyo = listOf(Color(0xFF4E342E), Color(0xFFA1887F))
val ColoresEscenario = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
val ColoresOcultar = listOf(Color(0xFF455A64), Color(0xFF90A4AE))
val ColoresOrden = listOf(Color(0xFF00695C), Color(0xFF4DB6AC))
val ColoresFavorito = listOf(Color(0xFF8D6E00), Color(0xFFE0B400))
val ColoresGacha = listOf(Color(0xFF4527A0), Color(0xFF7E57C2))

/* Efectos de una condición: verde cuando suma, rojo cuando resta. */
val ColoresEfectoSube = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
val ColoresEfectoBaja = listOf(Color(0xFFC62828), Color(0xFFEF9A9A))

val ColoresEstrella3 = listOf(Color(0xFFB8860B), Color(0xFFD4A017))
val ColoresEstrella2 = listOf(Color(0xFF78909C), Color(0xFFB0BEC5))
val ColoresEstrella1 = listOf(Color(0xFF8D6E63), Color(0xFFBCAAA4))

val ColoresApoyoSpeed = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
val ColoresApoyoStamina = listOf(Color(0xFFE65100), Color(0xFFFFA726))
val ColoresApoyoPower = listOf(Color(0xFFC62828), Color(0xFFEF5350))
val ColoresApoyoGuts = listOf(Color(0xFFAD1457), Color(0xFFF06292))
val ColoresApoyoIntelligence = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
val ColoresApoyoFriend = listOf(Color(0xFFF9A825), Color(0xFFFFD54F))
val ColoresApoyoGroup = listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))

/* Paleta por tipo de carta de apoyo. Debe declararse después de los colores
   para que estén inicializados al construir el mapa. */
val COLORES_TIPO_APOYO: Map<String, List<Color>> = mapOf(
    "speed" to ColoresApoyoSpeed,
    "stamina" to ColoresApoyoStamina,
    "power" to ColoresApoyoPower,
    "guts" to ColoresApoyoGuts,
    "intelligence" to ColoresApoyoIntelligence,
    "friend" to ColoresApoyoFriend,
    "group" to ColoresApoyoGroup,
)

fun coloresTipoApoyo(type: String?): List<Color> = COLORES_TIPO_APOYO[type] ?: ColoresTodas

@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    colores: List<Color>,
    onClick: () -> Unit,
) {
    val forma = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .shadow(if (seleccionado) 3.dp else 0.dp, forma)
            .clip(forma)
            .background(
                if (seleccionado) {
                    Brush.verticalGradient(colores)
                } else {
                    Brush.verticalGradient(colores.map { it.copy(alpha = 0.12f) })
                },
            )
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) colores.first() else colores.first().copy(alpha = 0.55f),
                shape = forma,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            texto,
            color = if (seleccionado) Color.White else colores.first(),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun FilaSkill(
    skill: Skill,
    idioma: String,
    compacta: Boolean = false,
    favorito: Boolean = false,
    onClick: () -> Unit,
) {
    /* En vista compacta el icono es más chico, hay menos relleno y se calla la
       línea en japonés: caben bastantes más filas en la misma pantalla. */
    val relleno = if (compacta) 6.dp else 10.dp
    TarjetaApp(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(relleno),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(skill.urlIcono, null, Modifier.size(if (compacta) 32.dp else 44.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    skill.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val japones = skill.name["ja"]
                if (!compacta && idioma != "ja" && !japones.isNullOrBlank()) {
                    Text(
                        japones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ResumenFuentes(skill)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (favorito) {
                    Icon(
                        painterResource(R.drawable.ic_estrella),
                        contentDescription = t(TX_UI, "favorita"),
                        tint = Color(0xFFD4A017),
                        modifier = Modifier.size(16.dp),
                    )
                }
                EtiquetaRareza(skill.rarity)
                if ("dbf" in skill.types) {
                    Etiqueta(etiquetaTipo("dbf", idioma), ColoresDebuff.first(), Color.White)
                }
                if ("en" in skill.unreleased) {
                    Etiqueta(
                        t(TX_UI, "sin_en"),
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun FilaPersonaje(
    carta: CartaPersonaje,
    idioma: String,
    kinds: List<String>?,
    compacta: Boolean = false,
    numeroSkills: Int? = null,
    onClick: () -> Unit,
) {
    val relleno = if (compacta) 6.dp else 10.dp
    TarjetaApp(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(relleno),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(carta.image, null, Modifier.size(if (compacta) 34.dp else 48.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    carta.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val titulo = carta.titulo(idioma)
                if (!compacta && titulo.isNotBlank()) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!kinds.isNullOrEmpty()) {
                    Text(
                        kinds.joinToString(" · ") { etiquetaKind(it, idioma) },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                val cuantas = numeroSkills
                if (cuantas != null) {
                    Text(
                        if (cuantas == 1) t(TX_UI, "skills_uno") else t(TX_UI, "skills_varios", cuantas),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Estrellas(carta.rarity)
        }
    }
}

@Composable
fun FilaApoyo(
    carta: CartaApoyo,
    idioma: String,
    insignia: String? = null,
    compacta: Boolean = false,
    numeroSkills: Int? = null,
    onClick: () -> Unit,
) {
    val relleno = if (compacta) 6.dp else 10.dp
    TarjetaApp(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(relleno),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(carta.image, null, Modifier.size(if (compacta) 34.dp else 48.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    carta.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val titulo = carta.titulo(idioma)
                if (!compacta && titulo.isNotBlank()) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EtiquetaTipoApoyo(carta.type)
                    if (!insignia.isNullOrBlank()) Etiqueta(insignia)
                    val cuantas = numeroSkills
                    if (cuantas != null) {
                        Text(
                            if (cuantas == 1) t(TX_UI, "skills_uno") else t(TX_UI, "skills_varios", cuantas),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Estrellas(carta.rarity)
        }
    }
}
