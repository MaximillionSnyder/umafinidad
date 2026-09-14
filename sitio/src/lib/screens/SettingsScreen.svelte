<script lang="ts">
/* Ajustes: apariencia, accesibilidad, tema, idioma, datos (export/import),
   accesos a Grupos/Rankings/Mis Umas, árboles guardados y bienvenida. */

import { goto } from '$app/navigation'
import { resolve } from '$app/paths'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { displayName } from '$lib/domain/models'
import { exportarArboles, fusionarImportados, importarArboles } from '$lib/data/transferencia'
import {
  EstiloAvatar,
  Idioma,
  ModoGrilla,
  TamanoTexto,
  ThemeMode,
} from '$lib/data/prefs'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import Icono from '$lib/components/Icono.svelte'
import { useSnackbar } from '$lib/components/snackbar'

const i18n = $derived(crearI18n(store.idioma))
const { avisar } = useSnackbar()

let seccionAbierta = $state<string | null>(null)

function estaAbierta(clave: string): boolean {
  return seccionAbierta === clave
}

function alternar(clave: string): void {
  seccionAbierta = seccionAbierta === clave ? null : clave
}

function exportar(): void {
  if (store.arboles.length === 0) {
    avisar(i18n.t('exportar_vacio'))
    return
  }
  const texto = exportarArboles(store.arboles)
  const blob = new Blob([texto], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = `umafinidad-arboles-${new Date().toISOString().slice(0, 10)}.json`
  enlace.click()
  URL.revokeObjectURL(url)
  avisar(i18n.t('exportado_snack'))
}

function importarArchivo(archivo: File): void {
  void archivo
    .text()
    .then((texto) => {
      try {
        const importados = importarArboles(texto)
        store.reemplazarArboles(fusionarImportados(store.arboles, importados))
        avisar(i18n.t('importados_snack', importados.length))
      } catch (error) {
        avisar(error instanceof Error ? error.message : i18n.t('importar_error'))
      }
    })
    .catch(() => avisar(i18n.t('importar_error')))
}

function onArchivo(evento: Event): void {
  const input = evento.currentTarget as HTMLInputElement
  const archivo = input.files?.[0]
  if (archivo) importarArchivo(archivo)
  input.value = ''
}
</script>

<svelte:head>
  <title>{i18n.t('tab_mas')} · Uma Afinidad</title>
</svelte:head>

<div class="pantalla">
  <HeaderBar titulo={i18n.t('tab_mas')} />
  <h2 class="subtitulo-seccion">{i18n.t('tab_ajustes')}</h2>

  <div class="ajustes-scroll">
    <section class="card seccion-desplegable">
      <button
        type="button"
        class="seccion-cabecera"
        aria-expanded={estaAbierta('apariencia')}
        onclick={() => alternar('apariencia')}
      >
        <span class="seccion-titulos">
          <span class="seccion-titulo">{i18n.t('ajustes_apariencia')}</span>
        </span>
        <span class="seccion-flecha" aria-hidden="true">{estaAbierta('apariencia') ? '∧' : '∨'}</span>
      </button>
      {#if estaAbierta('apariencia')}
        <div class="seccion-contenido">
          <p class="secundario">{i18n.t('modo_grilla_pregunta')}</p>
          {@render opcion(
            i18n.t('modo_vertical'),
            i18n.t('modo_vertical_desc'),
            store.modoGrilla === ModoGrilla.TARJETAS,
            () => store.setModoGrilla(ModoGrilla.TARJETAS),
          )}
          {@render opcion(
            i18n.t('modo_lista'),
            i18n.t('modo_lista_desc'),
            store.modoGrilla === ModoGrilla.LISTA,
            () => store.setModoGrilla(ModoGrilla.LISTA),
          )}
          <p class="secundario">{i18n.t('avatares_pregunta')}</p>
          {@render opcion(
            i18n.t('avatar_color'),
            i18n.t('avatar_color_desc'),
            store.estiloAvatar === EstiloAvatar.COLOR,
            () => store.setEstiloAvatar(EstiloAvatar.COLOR),
          )}
          {@render opcion(
            i18n.t('avatar_grises'),
            i18n.t('avatar_grises_desc'),
            store.estiloAvatar === EstiloAvatar.GRISES,
            () => store.setEstiloAvatar(EstiloAvatar.GRISES),
          )}
          {@render opcion(
            i18n.t('avatar_monocromo'),
            i18n.t('avatar_monocromo_desc'),
            store.estiloAvatar === EstiloAvatar.MONOCROMO,
            () => store.setEstiloAvatar(EstiloAvatar.MONOCROMO),
          )}
        </div>
      {/if}
    </section>

    <section class="card seccion-desplegable">
      <button
        type="button"
        class="seccion-cabecera"
        aria-expanded={estaAbierta('accesibilidad')}
        onclick={() => alternar('accesibilidad')}
      >
        <span class="seccion-titulos">
          <span class="seccion-titulo">{i18n.t('accesibilidad_titulo')}</span>
          <span class="secundario seccion-subtitulo">{i18n.t('accesibilidad_desc')}</span>
        </span>
        <span class="seccion-flecha" aria-hidden="true">
          {estaAbierta('accesibilidad') ? '∧' : '∨'}
        </span>
      </button>
      {#if estaAbierta('accesibilidad')}
        <div class="seccion-contenido">
          <p class="secundario">{i18n.t('tamano_texto_titulo')}</p>
          {@render opcion(
            i18n.t('tamano_normal'),
            i18n.t('tamano_normal_desc'),
            store.tamanoTexto === TamanoTexto.NORMAL,
            () => store.setTamanoTexto(TamanoTexto.NORMAL),
          )}
          {@render opcion(
            i18n.t('tamano_grande'),
            i18n.t('tamano_grande_desc'),
            store.tamanoTexto === TamanoTexto.GRANDE,
            () => store.setTamanoTexto(TamanoTexto.GRANDE),
          )}
          {@render opcion(
            i18n.t('tamano_muy_grande'),
            i18n.t('tamano_muy_grande_desc'),
            store.tamanoTexto === TamanoTexto.MUY_GRANDE,
            () => store.setTamanoTexto(TamanoTexto.MUY_GRANDE),
          )}
          {@render interruptor(
            i18n.t('negrita_titulo'),
            i18n.t('negrita_desc'),
            store.textoNegrita,
            (valor) => store.setTextoNegrita(valor),
          )}
        </div>
      {/if}
    </section>

    <section class="card seccion-desplegable">
      <button
        type="button"
        class="seccion-cabecera"
        aria-expanded={estaAbierta('tema')}
        onclick={() => alternar('tema')}
      >
        <span class="seccion-titulos">
          <span class="seccion-titulo">{i18n.t('tema_titulo')}</span>
          <span class="secundario seccion-subtitulo">{i18n.t('tema_desc')}</span>
        </span>
        <span class="seccion-flecha" aria-hidden="true">{estaAbierta('tema') ? '∧' : '∨'}</span>
      </button>
      {#if estaAbierta('tema')}
        <div class="seccion-contenido">
          {@render opcion(
            i18n.t('tema_sistema'),
            i18n.t('tema_sistema_desc'),
            store.tema === ThemeMode.SISTEMA,
            () => store.setTema(ThemeMode.SISTEMA),
          )}
          {@render opcion(
            i18n.t('tema_claro'),
            i18n.t('tema_claro_desc'),
            store.tema === ThemeMode.CLARO,
            () => store.setTema(ThemeMode.CLARO),
          )}
          {@render opcion(
            i18n.t('tema_oscuro'),
            i18n.t('tema_oscuro_desc'),
            store.tema === ThemeMode.OSCURO,
            () => store.setTema(ThemeMode.OSCURO),
          )}
          {@render opcion(
            i18n.t('tema_contraste'),
            i18n.t('tema_contraste_desc'),
            store.tema === ThemeMode.ALTO_CONTRASTE,
            () => store.setTema(ThemeMode.ALTO_CONTRASTE),
          )}
        </div>
      {/if}
    </section>

    <section class="card seccion-desplegable">
      <button
        type="button"
        class="seccion-cabecera"
        aria-expanded={estaAbierta('idioma')}
        onclick={() => alternar('idioma')}
      >
        <span class="seccion-titulos">
          <span class="seccion-titulo">{i18n.t('idioma_titulo')}</span>
          <span class="secundario seccion-subtitulo">{i18n.t('idioma_desc')}</span>
        </span>
        <span class="seccion-flecha" aria-hidden="true">{estaAbierta('idioma') ? '∧' : '∨'}</span>
      </button>
      {#if estaAbierta('idioma')}
        <div class="seccion-contenido">
          {@render opcion(
            i18n.t('idioma_sistema'),
            i18n.t('idioma_sistema_desc'),
            store.idioma === Idioma.SISTEMA,
            () => store.setIdioma(Idioma.SISTEMA),
          )}
          {@render opcion(
            i18n.t('idioma_espanol'),
            i18n.t('idioma_espanol_desc'),
            store.idioma === Idioma.ESPANOL,
            () => store.setIdioma(Idioma.ESPANOL),
          )}
          {@render opcion(
            i18n.t('idioma_ingles'),
            i18n.t('idioma_ingles_desc'),
            store.idioma === Idioma.INGLES,
            () => store.setIdioma(Idioma.INGLES),
          )}
          {@render opcion(
            i18n.t('idioma_japones'),
            i18n.t('idioma_japones_desc'),
            store.idioma === Idioma.JAPONES,
            () => store.setIdioma(Idioma.JAPONES),
          )}
        </div>
      {/if}
    </section>

    <section class="card seccion-desplegable">
      <button
        type="button"
        class="seccion-cabecera"
        aria-expanded={estaAbierta('datos')}
        onclick={() => alternar('datos')}
      >
        <span class="seccion-titulos">
          <span class="seccion-titulo">{i18n.t('datos_titulo')}</span>
        </span>
        <span class="seccion-flecha" aria-hidden="true">{estaAbierta('datos') ? '∧' : '∨'}</span>
      </button>
      {#if estaAbierta('datos')}
        <div class="seccion-contenido">
          {@render acceso(
            'elenco',
            i18n.t('exportar_arboles'),
            i18n.t('exportar_arboles_desc'),
            exportar,
          )}
          <label class="card tarjeta-acceso" for="archivo-importar">
            <span class="tarjeta-acceso-icono" aria-hidden="true"><Icono nombre="ajustes" /></span>
            <span class="tarjeta-acceso-texto">
              <span class="opcion-titulo">{i18n.t('importar_arboles')}</span>
              <span class="secundario">{i18n.t('importar_arboles_desc')}</span>
            </span>
            <input
              id="archivo-importar"
              class="oculto-visualmente"
              type="file"
              accept="application/json,.json"
              onchange={onArchivo}
            />
          </label>
        </div>
      {/if}
    </section>

    <hr class="separador" />

    {@render acceso('grupos', i18n.t('tab_groups'), i18n.t('grupos_ajustes_desc'), () => goto(resolve('/grupos')))}
    {@render acceso('ranking', i18n.t('tab_ranking'), i18n.t('ranking_ajustes_desc'), () => goto(resolve('/ranking')))}
    {@render acceso(
      'ranking',
      i18n.t('ranking_padres_titulo'),
      i18n.t('ranking_padres_ajustes_desc'),
      () => goto(resolve('/ranking-padres')),
    )}
    {@render acceso('elenco', i18n.t('tab_elenco'), i18n.t('elenco_ajustes_desc'), () => goto(resolve('/elenco')))}

    {#if store.arboles.length > 0}
      <hr class="separador" />
      <h2 class="subtitulo-seccion">{i18n.t('arboles_ajustes')}</h2>
      <ul class="lista-guardadas">
        {#each store.arboles as a (a.id)}
          {@const personaje = store.modelo?.porId(a.hijoId)}
          <li>
            <article class="card tarjeta-guardada">
              <button
                type="button"
                class="tarjeta-guardada-abrir"
                onclick={() => {
                  store.abrirArbol(a)
                  void goto(resolve('/corredora'))
                }}
              >
                <span class="tarjeta-guardada-nombre">{a.nombre}</span>
                <span class="secundario tarjeta-guardada-meta">
                  {personaje ? displayName(personaje, i18n.japones) : `#${a.hijoId}`} · ◎ {a.total}
                </span>
              </button>
              <button
                type="button"
                class="icon-button"
                onclick={() => store.eliminarArbol(a.id)}
                aria-label={i18n.t('cancelar')}
              >
                <Icono nombre="cerrar" />
              </button>
            </article>
          </li>
        {/each}
      </ul>
    {/if}

    {@render acceso(
      'ajustes',
      i18n.t('revisar_accesibilidad'),
      i18n.t('revisar_accesibilidad_desc'),
      () => store.abrirBienvenida(),
    )}

    <hr class="separador" />
    <p class="version">Uma Afinidad v{__APP_VERSION__}</p>
  </div>
</div>

{#snippet opcion(titulo: string, descripcion: string, seleccionado: boolean, onClick: () => void)}
  <button
    type="button"
    role="radio"
    aria-checked={seleccionado}
    class={seleccionado ? 'opcion seleccionada' : 'opcion'}
    onclick={onClick}
  >
    <span class={seleccionado ? 'radio activo' : 'radio'} aria-hidden="true"></span>
    <span class="opcion-texto">
      <span class="opcion-titulo">{titulo}</span>
      <span class="secundario">{descripcion}</span>
    </span>
  </button>
{/snippet}

{#snippet interruptor(titulo: string, descripcion: string, activado: boolean, onCambio: (valor: boolean) => void)}
  <button type="button" role="switch" aria-checked={activado} class="fila-interruptor" onclick={() => onCambio(!activado)}>
    <span class="fila-interruptor-texto">
      <span class="fila-interruptor-titulo">{titulo}</span>
      <span class="secundario">{descripcion}</span>
    </span>
    <span class={activado ? 'switch activo' : 'switch'} aria-hidden="true">
      <span class="switch-bola"></span>
    </span>
  </button>
{/snippet}

{#snippet acceso(_icono: string, titulo: string, descripcion: string, onClick: () => void)}
  <button type="button" class="card tarjeta-acceso" onclick={onClick}>
    <span class="tarjeta-acceso-icono" aria-hidden="true"><Icono nombre={_icono} /></span>
    <span class="tarjeta-acceso-texto">
      <span class="opcion-titulo">{titulo}</span>
      <span class="secundario">{descripcion}</span>
    </span>
  </button>
{/snippet}
