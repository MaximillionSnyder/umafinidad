/* Ventana de bienvenida de accesibilidad: propone tamaño de texto,
   negrita y alto contraste según señales del sistema, con vista previa en
   vivo. Porte de ui/componentes/BienvenidaAccesibilidad.kt. */

import { useMemo, useState } from 'react'
import { store, useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { tamanoSegunFontScale, TamanoTexto, ThemeMode } from '../data/prefs'
import { Dialogo } from './Modal'

const ETIQUETA_TAMANO: Record<TamanoTexto, string> = {
  [TamanoTexto.NORMAL]: 'tamano_normal',
  [TamanoTexto.GRANDE]: 'tamano_grande',
  [TamanoTexto.MUY_GRANDE]: 'tamano_muy_grande',
}

/* Señales del navegador: escala de fuente efectiva (rem base) y
   preferencia de contraste del sistema. */
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

export function BienvenidaAccesibilidad() {
  const { tema, tamanoTexto, textoNegrita } = useAppStore()
  const { t } = useI18n()

  const detectado = useMemo(() => tamanoSegunFontScale(escalaFuenteSistema()), [])
  const altoContraste = useMemo(() => contrasteSistema(), [])
  const temaInicial = useMemo(() => tema, [tema])

  const [tamanoSel, setTamanoSel] = useState<TamanoTexto>(
    detectado > tamanoTexto ? detectado : tamanoTexto,
  )
  const [negritaSel, setNegritaSel] = useState(textoNegrita)
  const [temaSel, setTemaSel] = useState<ThemeMode>(
    altoContraste && tema !== ThemeMode.ALTO_CONTRASTE ? ThemeMode.ALTO_CONTRASTE : tema,
  )

  const esContraste = temaSel === ThemeMode.ALTO_CONTRASTE

  return (
    <Dialogo
      open
      bloqueante
      titulo={t('bienvenida_titulo')}
      onClose={() => {}}
    >
      <div className="bienvenida">
        <p className="secundario">{t('bienvenida_desc')}</p>

        <h3>{t('bienvenida_tamano')}</h3>
        <div role="radiogroup" aria-label={t('bienvenida_tamano')} className="bienvenida-grupo">
          {[TamanoTexto.NORMAL, TamanoTexto.GRANDE, TamanoTexto.MUY_GRANDE].map((opcion) => (
            <button
              key={opcion}
              type="button"
              role="radio"
              aria-checked={tamanoSel === opcion}
              className={tamanoSel === opcion ? 'opcion seleccionada' : 'opcion'}
              onClick={() => {
                setTamanoSel(opcion)
                store.setTamanoTexto(opcion)
              }}
            >
              <span className={tamanoSel === opcion ? 'radio activo' : 'radio'} aria-hidden="true" />
              <span className="opcion-titulo">{t(ETIQUETA_TAMANO[opcion])}</span>
            </button>
          ))}
        </div>

        <FilaToggle
          titulo={t('negrita_titulo')}
          descripcion={t('negrita_desc')}
          activado={negritaSel}
          onCambio={(valor) => {
            setNegritaSel(valor)
            store.setTextoNegrita(valor)
          }}
        />
        <FilaToggle
          titulo={t('tema_contraste')}
          descripcion={t('tema_contraste_desc')}
          activado={esContraste}
          onCambio={(activo) => {
            const nuevo = activo ? ThemeMode.ALTO_CONTRASTE : temaInicial
            setTemaSel(nuevo)
            store.setTema(nuevo)
          }}
        />
      </div>

      <button type="button" className="boton-texto" onClick={() => store.confirmarBienvenida()}>
        {t('guardar')}
      </button>
      <button type="button" className="boton-texto" onClick={() => store.omitirBienvenida()}>
        {t('omitir')}
      </button>
    </Dialogo>
  )
}

function FilaToggle({
  titulo,
  descripcion,
  activado,
  onCambio,
}: {
  titulo: string
  descripcion: string
  activado: boolean
  onCambio: (valor: boolean) => void
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={activado}
      className="fila-interruptor"
      onClick={() => onCambio(!activado)}
    >
      <span className="fila-interruptor-texto">
        <span className="fila-interruptor-titulo">{titulo}</span>
        <span className="secundario">{descripcion}</span>
      </span>
      <span className={activado ? 'switch activo' : 'switch'} aria-hidden="true">
        <span className="switch-bola" />
      </span>
    </button>
  )
}
