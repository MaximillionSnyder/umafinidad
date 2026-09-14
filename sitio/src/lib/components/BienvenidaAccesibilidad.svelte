<script lang="ts">
/* Ventana de bienvenida de accesibilidad: propone tamaño de texto,
   negrita y alto contraste según señales del sistema, con vista previa en
   vivo. */

import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'
import { ESCALA_TAMANO, tamanoSegunFontScale, TamanoTexto, ThemeMode } from '../data/prefs'
import Dialogo from './Dialogo.svelte'

const ETIQUETA_TAMANO: Record<TamanoTexto, string> = {
  [TamanoTexto.NORMAL]: 'tamano_normal',
  [TamanoTexto.GRANDE]: 'tamano_grande',
  [TamanoTexto.MUY_GRANDE]: 'tamano_muy_grande',
}

function escalaFuenteSistema(): number {
  if (typeof window === 'undefined') return 1
  const tamano = parseFloat(getComputedStyle(document.documentElement).fontSize)
  return Number.isFinite(tamano) ? tamano / 16 : 1
}

function contrasteSistema(): boolean {
  return typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-contrast: more)').matches
    : false
}

const i18n = $derived(crearI18n(store.idioma))
const detectado = tamanoSegunFontScale(escalaFuenteSistema())
const altoContraste = contrasteSistema()
const temaInicial = store.tema

let tamanoSel = $state<TamanoTexto>(
  ESCALA_TAMANO[detectado] > ESCALA_TAMANO[store.tamanoTexto] ? detectado : store.tamanoTexto,
)
let negritaSel = $state(store.textoNegrita)
let temaSel = $state<ThemeMode>(
  altoContraste && store.tema !== ThemeMode.ALTO_CONTRASTE ? ThemeMode.ALTO_CONTRASTE : store.tema,
)

function cambiarTamano(opcion: TamanoTexto): void {
  tamanoSel = opcion
  store.setTamanoTexto(opcion)
}

function cambiarNegrita(valor: boolean): void {
  negritaSel = valor
  store.setTextoNegrita(valor)
}

function cambiarContraste(activo: boolean): void {
  const nuevo = activo ? ThemeMode.ALTO_CONTRASTE : temaInicial
  temaSel = nuevo
  store.setTema(nuevo)
}
</script>

<Dialogo
  open={true}
  bloqueante={true}
  titulo={i18n.t('bienvenida_titulo')}
  onClose={() => {}}
>
  <div class="bienvenida">
    <p class="secundario">{i18n.t('bienvenida_desc')}</p>

    <h3>{i18n.t('bienvenida_tamano')}</h3>
    <div role="radiogroup" aria-label={i18n.t('bienvenida_tamano')} class="bienvenida-grupo">
      {#each [TamanoTexto.NORMAL, TamanoTexto.GRANDE, TamanoTexto.MUY_GRANDE] as opcion (opcion)}
        <button
          type="button"
          role="radio"
          aria-checked={tamanoSel === opcion}
          class={tamanoSel === opcion ? 'opcion seleccionada' : 'opcion'}
          onclick={() => cambiarTamano(opcion)}
        >
          <span class={tamanoSel === opcion ? 'radio activo' : 'radio'} aria-hidden="true"></span>
          <span class="opcion-titulo">{i18n.t(ETIQUETA_TAMANO[opcion])}</span>
        </button>
      {/each}
    </div>

    <button
      type="button"
      role="switch"
      aria-checked={negritaSel}
      class="fila-interruptor"
      onclick={() => cambiarNegrita(!negritaSel)}
    >
      <span class="fila-interruptor-texto">
        <span class="fila-interruptor-titulo">{i18n.t('negrita_titulo')}</span>
        <span class="secundario">{i18n.t('negrita_desc')}</span>
      </span>
      <span class={negritaSel ? 'switch activo' : 'switch'} aria-hidden="true">
        <span class="switch-bola"></span>
      </span>
    </button>

    <button
      type="button"
      role="switch"
      aria-checked={temaSel === ThemeMode.ALTO_CONTRASTE}
      class="fila-interruptor"
      onclick={() => cambiarContraste(temaSel !== ThemeMode.ALTO_CONTRASTE)}
    >
      <span class="fila-interruptor-texto">
        <span class="fila-interruptor-titulo">{i18n.t('tema_contraste')}</span>
        <span class="secundario">{i18n.t('tema_contraste_desc')}</span>
      </span>
      <span class={temaSel === ThemeMode.ALTO_CONTRASTE ? 'switch activo' : 'switch'} aria-hidden="true">
        <span class="switch-bola"></span>
      </span>
    </button>
  </div>

  <button type="button" class="boton-texto" onclick={() => store.confirmarBienvenida()}>
    {i18n.t('guardar')}
  </button>
  <button type="button" class="boton-texto" onclick={() => store.omitirBienvenida()}>
    {i18n.t('omitir')}
  </button>
</Dialogo>
