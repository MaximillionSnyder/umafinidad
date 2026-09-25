package com.maximillionsnyder.umafinidad.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.maximillionsnyder.umafinidad.BuildConfig
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.EstiloAvatar
import com.maximillionsnyder.umafinidad.data.Idioma
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.data.TamanoBurbuja
import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.data.descripcionRes
import com.maximillionsnyder.umafinidad.data.tituloRes
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.componentes.ClavesTransicion
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoBounds
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoElemento
import com.maximillionsnyder.umafinidad.ui.componentes.headingSemantica

/* Apartado de ajustes: por ahora, el modo de grilla de personajes. */
@Composable
fun SettingsScreen(
    modoGrilla: ModoGrilla,
    onModoGrilla: (ModoGrilla) -> Unit,
    estiloAvatar: EstiloAvatar,
    onEstiloAvatar: (EstiloAvatar) -> Unit,
    tema: ThemeMode,
    onTema: (ThemeMode) -> Unit,
    idioma: Idioma,
    onIdioma: (Idioma) -> Unit,
    burbujaActiva: Boolean,
    burbujaPermiso: Boolean,
    onBurbuja: (Boolean) -> Unit,
    panelTranslucido: Boolean,
    onPanelTranslucido: (Boolean) -> Unit,
    dosColumnas: Boolean = false,
    onDosColumnas: (Boolean) -> Unit = {},
    tamanoBurbuja: TamanoBurbuja,
    onTamanoBurbuja: (TamanoBurbuja) -> Unit,
    onRestablecerTamanos: () -> Unit,
    modelo: AffinityModel,
    japones: Boolean,
    arboles: List<ArbolGuardado>,
    onAbrirArbol: (ArbolGuardado) -> Unit,
    onEliminarArbol: (Long) -> Unit,
    onAbrirGrupos: () -> Unit,
    onAbrirRanking: () -> Unit,
    onAbrirRankingPadres: () -> Unit,
    onAbrirElenco: () -> Unit,
    esPro: Boolean = false,
    onAbrirPro: () -> Unit = {},
    tamanoTexto: TamanoTexto,
    onTamanoTexto: (TamanoTexto) -> Unit,
    textoNegrita: Boolean,
    onTextoNegrita: (Boolean) -> Unit,
    onAbrirBienvenida: () -> Unit,
) {
    var seccionAbierta by rememberSaveable { mutableStateOf<String?>(null) }

    /* Acordeón: una sola sección abierta; abrir otra cierra la anterior. */
    fun estaAbierta(clave: String) = seccionAbierta == clave
    fun alternar(clave: String) {
        seccionAbierta = if (seccionAbierta == clave) null else clave
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBar(titulo = stringResource(R.string.tab_mas))

        Text(
            stringResource(R.string.tab_ajustes),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).headingSemantica(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        /* ===== Uma Afinidad Pro (licencia local) ===== */
        Card(
            modifier = Modifier.fillMaxWidth()
                .compartidoBounds(ClavesTransicion.OVERLAY_PRO)
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.pro_titulo),
                    onClick = onAbrirPro,
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_pro),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.compartidoElemento(
                        ClavesTransicion.icono(ClavesTransicion.OVERLAY_PRO),
                    ),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            stringResource(R.string.pro_titulo),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.compartidoBounds(
                                ClavesTransicion.titulo(ClavesTransicion.OVERLAY_PRO),
                            ),
                        )
                        if (esPro) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    stringResource(R.string.pro_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(if (esPro) R.string.pro_estado_activo else R.string.pro_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.burbuja_titulo),
            subtitulo = stringResource(R.string.burbuja_desc),
            abierto = estaAbierta("burbuja"),
            onToggle = { alternar("burbuja") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilaInterruptor(
                    titulo = stringResource(R.string.burbuja_switch),
                    descripcion = stringResource(R.string.burbuja_switch_desc),
                    activado = burbujaActiva,
                    onCambio = onBurbuja,
                )
                FilaInterruptor(
                    titulo = stringResource(R.string.burbuja_panel_translucido),
                    descripcion = stringResource(R.string.burbuja_panel_translucido_desc),
                    activado = panelTranslucido,
                    onCambio = onPanelTranslucido,
                )
                FilaInterruptor(
                    titulo = stringResource(R.string.burbuja_dos_columnas),
                    descripcion = stringResource(R.string.burbuja_dos_columnas_desc),
                    activado = dosColumnas,
                    onCambio = onDosColumnas,
                )
                Text(
                    stringResource(R.string.burbuja_tamano_titulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.burbuja_tamano_chico),
                    descripcion = stringResource(R.string.burbuja_tamano_chico_desc),
                    seleccionado = tamanoBurbuja == TamanoBurbuja.CHICO,
                    onClick = { onTamanoBurbuja(TamanoBurbuja.CHICO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.burbuja_tamano_normal),
                    descripcion = stringResource(R.string.burbuja_tamano_normal_desc),
                    seleccionado = tamanoBurbuja == TamanoBurbuja.NORMAL,
                    onClick = { onTamanoBurbuja(TamanoBurbuja.NORMAL) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.burbuja_tamano_grande),
                    descripcion = stringResource(R.string.burbuja_tamano_grande_desc),
                    seleccionado = tamanoBurbuja == TamanoBurbuja.GRANDE,
                    onClick = { onTamanoBurbuja(TamanoBurbuja.GRANDE) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.burbuja_tamano_muy_grande),
                    descripcion = stringResource(R.string.burbuja_tamano_muy_grande_desc),
                    seleccionado = tamanoBurbuja == TamanoBurbuja.MUY_GRANDE,
                    onClick = { onTamanoBurbuja(TamanoBurbuja.MUY_GRANDE) },
                )
                TextButton(onClick = onRestablecerTamanos) {
                    Text(stringResource(R.string.burbuja_restablecer))
                }
                if (burbujaActiva && !burbujaPermiso) {
                    Text(
                        stringResource(R.string.burbuja_sin_permiso),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.ajustes_apariencia),
            abierto = estaAbierta("apariencia"),
            onToggle = { alternar("apariencia") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.modo_grilla_pregunta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.modo_vertical),
                    descripcion = stringResource(R.string.modo_vertical_desc),
                    seleccionado = modoGrilla == ModoGrilla.TARJETAS,
                    onClick = { onModoGrilla(ModoGrilla.TARJETAS) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.modo_lista),
                    descripcion = stringResource(R.string.modo_lista_desc),
                    seleccionado = modoGrilla == ModoGrilla.LISTA,
                    onClick = { onModoGrilla(ModoGrilla.LISTA) },
                )
                Text(
                    stringResource(R.string.avatares_pregunta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OpcionGrilla(
                    titulo = stringResource(EstiloAvatar.COLOR.tituloRes()),
                    descripcion = stringResource(EstiloAvatar.COLOR.descripcionRes()),
                    seleccionado = estiloAvatar == EstiloAvatar.COLOR,
                    onClick = { onEstiloAvatar(EstiloAvatar.COLOR) },
                )
                OpcionGrilla(
                    titulo = stringResource(EstiloAvatar.GRISES.tituloRes()),
                    descripcion = stringResource(EstiloAvatar.GRISES.descripcionRes()),
                    seleccionado = estiloAvatar == EstiloAvatar.GRISES,
                    onClick = { onEstiloAvatar(EstiloAvatar.GRISES) },
                )
                OpcionGrilla(
                    titulo = stringResource(EstiloAvatar.MONOCROMO.tituloRes()),
                    descripcion = stringResource(EstiloAvatar.MONOCROMO.descripcionRes()),
                    seleccionado = estiloAvatar == EstiloAvatar.MONOCROMO,
                    onClick = { onEstiloAvatar(EstiloAvatar.MONOCROMO) },
                )
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.accesibilidad_titulo),
            subtitulo = stringResource(R.string.accesibilidad_desc),
            abierto = estaAbierta("accesibilidad"),
            onToggle = { alternar("accesibilidad") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.tamano_texto_titulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tamano_normal),
                    descripcion = stringResource(R.string.tamano_normal_desc),
                    seleccionado = tamanoTexto == TamanoTexto.NORMAL,
                    onClick = { onTamanoTexto(TamanoTexto.NORMAL) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tamano_grande),
                    descripcion = stringResource(R.string.tamano_grande_desc),
                    seleccionado = tamanoTexto == TamanoTexto.GRANDE,
                    onClick = { onTamanoTexto(TamanoTexto.GRANDE) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tamano_muy_grande),
                    descripcion = stringResource(R.string.tamano_muy_grande_desc),
                    seleccionado = tamanoTexto == TamanoTexto.MUY_GRANDE,
                    onClick = { onTamanoTexto(TamanoTexto.MUY_GRANDE) },
                )
                FilaInterruptor(
                    titulo = stringResource(R.string.negrita_titulo),
                    descripcion = stringResource(R.string.negrita_desc),
                    activado = textoNegrita,
                    onCambio = onTextoNegrita,
                )
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.tema_titulo),
            subtitulo = stringResource(R.string.tema_desc),
            abierto = estaAbierta("tema"),
            onToggle = { alternar("tema") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_sistema),
                    descripcion = stringResource(R.string.tema_sistema_desc),
                    seleccionado = tema == ThemeMode.SISTEMA,
                    onClick = { onTema(ThemeMode.SISTEMA) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_claro),
                    descripcion = stringResource(R.string.tema_claro_desc),
                    seleccionado = tema == ThemeMode.CLARO,
                    onClick = { onTema(ThemeMode.CLARO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_oscuro),
                    descripcion = stringResource(R.string.tema_oscuro_desc),
                    seleccionado = tema == ThemeMode.OSCURO,
                    onClick = { onTema(ThemeMode.OSCURO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_contraste),
                    descripcion = stringResource(R.string.tema_contraste_desc),
                    seleccionado = tema == ThemeMode.ALTO_CONTRASTE,
                    onClick = { onTema(ThemeMode.ALTO_CONTRASTE) },
                )
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.idioma_titulo),
            subtitulo = stringResource(R.string.idioma_desc),
            abierto = estaAbierta("idioma"),
            onToggle = { alternar("idioma") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_sistema),
                    descripcion = stringResource(R.string.idioma_sistema_desc),
                    seleccionado = idioma == Idioma.SISTEMA,
                    onClick = { onIdioma(Idioma.SISTEMA) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_espanol),
                    descripcion = stringResource(R.string.idioma_espanol_desc),
                    seleccionado = idioma == Idioma.ESPANOL,
                    onClick = { onIdioma(Idioma.ESPANOL) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_ingles),
                    descripcion = stringResource(R.string.idioma_ingles_desc),
                    seleccionado = idioma == Idioma.INGLES,
                    onClick = { onIdioma(Idioma.INGLES) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_japones),
                    descripcion = stringResource(R.string.idioma_japones_desc),
                    seleccionado = idioma == Idioma.JAPONES,
                    onClick = { onIdioma(Idioma.JAPONES) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_chino_simplificado),
                    descripcion = stringResource(R.string.idioma_chino_simplificado_desc),
                    seleccionado = idioma == Idioma.CHINO_SIMPLIFICADO,
                    onClick = { onIdioma(Idioma.CHINO_SIMPLIFICADO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_chino_tradicional),
                    descripcion = stringResource(R.string.idioma_chino_tradicional_desc),
                    seleccionado = idioma == Idioma.CHINO_TRADICIONAL,
                    onClick = { onIdioma(Idioma.CHINO_TRADICIONAL) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_coreano),
                    descripcion = stringResource(R.string.idioma_coreano_desc),
                    seleccionado = idioma == Idioma.COREANO,
                    onClick = { onIdioma(Idioma.COREANO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_indonesio),
                    descripcion = stringResource(R.string.idioma_indonesio_desc),
                    seleccionado = idioma == Idioma.INDONESIO,
                    onClick = { onIdioma(Idioma.INDONESIO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_tailandes),
                    descripcion = stringResource(R.string.idioma_tailandes_desc),
                    seleccionado = idioma == Idioma.TAILANDES,
                    onClick = { onIdioma(Idioma.TAILANDES) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.idioma_vietnamita),
                    descripcion = stringResource(R.string.idioma_vietnamita_desc),
                    seleccionado = idioma == Idioma.VIETNAMITA,
                    onClick = { onIdioma(Idioma.VIETNAMITA) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        /* ===== Grupos (referencia, archivada de la barra inferior) ===== */
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth()
                .compartidoBounds(ClavesTransicion.OVERLAY_GRUPOS)
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.abrir_grupos),
                    onClick = onAbrirGrupos,
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_groups),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.compartidoElemento(ClavesTransicion.icono(ClavesTransicion.OVERLAY_GRUPOS)),
                )
                Column {
                    Text(
                        stringResource(R.string.tab_groups),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.compartidoBounds(ClavesTransicion.titulo(ClavesTransicion.OVERLAY_GRUPOS)),
                    )
                    Text(
                        stringResource(R.string.grupos_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Ranking (referencia, archivado de la barra inferior) ===== */
        Card(
            modifier = Modifier.fillMaxWidth()
                .compartidoBounds(ClavesTransicion.OVERLAY_RANKING)
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.abrir_ranking),
                    onClick = onAbrirRanking,
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_ranking),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.compartidoElemento(ClavesTransicion.icono(ClavesTransicion.OVERLAY_RANKING)),
                )
                Column {
                    Text(
                        stringResource(R.string.tab_ranking),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.compartidoBounds(ClavesTransicion.titulo(ClavesTransicion.OVERLAY_RANKING)),
                    )
                    Text(
                        stringResource(R.string.ranking_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Mejores padres (función Pro) ===== */
        Card(
            modifier = Modifier.fillMaxWidth()
                .compartidoBounds(ClavesTransicion.OVERLAY_RANKING_PADRES)
                .clickable(
                    role = Role.Button,
                    onClick = { if (esPro) onAbrirRankingPadres() else onAbrirPro() },
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(if (esPro) R.drawable.ic_tab_ranking else R.drawable.ic_candado),
                    contentDescription = if (esPro) null else stringResource(R.string.pro_bloqueada),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.compartidoElemento(ClavesTransicion.icono(ClavesTransicion.OVERLAY_RANKING_PADRES)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.ranking_padres_titulo),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.compartidoBounds(ClavesTransicion.titulo(ClavesTransicion.OVERLAY_RANKING_PADRES)),
                    )
                    Text(
                        stringResource(R.string.ranking_padres_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!esPro) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            stringResource(R.string.pro_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }

        /* ===== Mis Umas (referencia, archivado de la barra inferior) ===== */
        Card(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onAbrirElenco),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_elenco),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.tab_elenco), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.elenco_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Árboles guardados (global) ===== */
        if (arboles.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.arboles_ajustes),
                modifier = Modifier.headingSemantica(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            arboles.forEach { a ->
                val nombreHijo = modelo.porId(a.hijoId)?.displayName(japones) ?: "#${'$'}{a.hijoId}"
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = { onAbrirArbol(a) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                nombreHijo + " · ◎ " + a.total,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onEliminarArbol(a.id) }) {
                            Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.cancelar))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        /* ===== Revisar accesibilidad ===== */
        Card(
            modifier = Modifier.fillMaxWidth().clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.revisar_accesibilidad),
                onClick = onAbrirBienvenida,
            ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_ajustes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.revisar_accesibilidad), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.revisar_accesibilidad_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "Uma Afinidad v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilaInterruptor(titulo: String, descripcion: String, activado: Boolean, onCambio: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = activado, role = Role.Switch, onValueChange = onCambio)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = activado, onCheckedChange = null)
        }
    }
}

@Composable
private fun OpcionGrilla(titulo: String, descripcion: String, seleccionado: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (seleccionado) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = seleccionado, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = seleccionado, onClick = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SeccionDesplegable(
    titulo: String,
    abierto: Boolean,
    onToggle: () -> Unit,
    subtitulo: String? = null,
    contenido: @Composable () -> Unit,
) {
    val estadoTxt = stringResource(if (abierto) R.string.expandido else R.string.contraido)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = abierto, role = Role.Button, onValueChange = { onToggle() })
                    .semantics { stateDescription = estadoTxt }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(titulo, modifier = Modifier.headingSemantica(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (subtitulo != null) {
                        Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    if (abierto) "∧" else "∨",
                    modifier = Modifier.clearAndSetSemantics {},
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AnimatedVisibility(
                visible = abierto,
                enter = expandVertically(tween(200)) + fadeIn(tween(150)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(120)),
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    contenido()
                }
            }
        }
    }
}
