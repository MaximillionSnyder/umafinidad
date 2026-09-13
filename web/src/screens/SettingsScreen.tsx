/* Ajustes: apariencia, accesibilidad, tema, idioma, accesos archivados
   (Grupos/Rankings/Mis Umas), árboles guardados y bienvenida de
   accesibilidad. Porte de ui/settings/SettingsScreen.kt. */

import { useState, type ReactNode } from 'react'
import { store, useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { irA, irAOverlay } from '../state/navegacion'
import { displayName } from '../domain/models'
import {
  EstiloAvatar,
  Idioma,
  ModoGrilla,
  TamanoTexto,
  ThemeMode,
} from '../data/prefs'
import { HeaderBar } from '../components/HeaderBar'
import { IconAjustes, IconCerrar, IconElenco, IconGrupos, IconRanking } from '../components/Icons'

declare const __APP_VERSION__: string

export function SettingsScreen() {
  const {
    modoGrilla,
    estiloAvatar,
    tema,
    idioma,
    tamanoTexto,
    textoNegrita,
    modelo,
    arboles,
  } = useAppStore()
  const { t, japones } = useI18n()
  const [seccionAbierta, setSeccionAbierta] = useState<string | null>(null)

  const estaAbierta = (clave: string) => seccionAbierta === clave
  const alternar = (clave: string) => setSeccionAbierta((actual) => (actual === clave ? null : clave))

  return (
    <div className="pantalla">
      <HeaderBar titulo={t('tab_mas')} />
      <h2 className="subtitulo-seccion">{t('tab_ajustes')}</h2>

      <div className="ajustes-scroll">
        <SeccionDesplegable
          titulo={t('ajustes_apariencia')}
          abierto={estaAbierta('apariencia')}
          onToggle={() => alternar('apariencia')}
        >
          <p className="secundario">{t('modo_grilla_pregunta')}</p>
          <Opcion
            titulo={t('modo_vertical')}
            descripcion={t('modo_vertical_desc')}
            seleccionado={modoGrilla === ModoGrilla.TARJETAS}
            onClick={() => store.setModoGrilla(ModoGrilla.TARJETAS)}
          />
          <Opcion
            titulo={t('modo_lista')}
            descripcion={t('modo_lista_desc')}
            seleccionado={modoGrilla === ModoGrilla.LISTA}
            onClick={() => store.setModoGrilla(ModoGrilla.LISTA)}
          />
          <p className="secundario">{t('avatares_pregunta')}</p>
          <Opcion
            titulo={t('avatar_color')}
            descripcion={t('avatar_color_desc')}
            seleccionado={estiloAvatar === EstiloAvatar.COLOR}
            onClick={() => store.setEstiloAvatar(EstiloAvatar.COLOR)}
          />
          <Opcion
            titulo={t('avatar_grises')}
            descripcion={t('avatar_grises_desc')}
            seleccionado={estiloAvatar === EstiloAvatar.GRISES}
            onClick={() => store.setEstiloAvatar(EstiloAvatar.GRISES)}
          />
          <Opcion
            titulo={t('avatar_monocromo')}
            descripcion={t('avatar_monocromo_desc')}
            seleccionado={estiloAvatar === EstiloAvatar.MONOCROMO}
            onClick={() => store.setEstiloAvatar(EstiloAvatar.MONOCROMO)}
          />
        </SeccionDesplegable>

        <SeccionDesplegable
          titulo={t('accesibilidad_titulo')}
          subtitulo={t('accesibilidad_desc')}
          abierto={estaAbierta('accesibilidad')}
          onToggle={() => alternar('accesibilidad')}
        >
          <p className="secundario">{t('tamano_texto_titulo')}</p>
          <Opcion
            titulo={t('tamano_normal')}
            descripcion={t('tamano_normal_desc')}
            seleccionado={tamanoTexto === TamanoTexto.NORMAL}
            onClick={() => store.setTamanoTexto(TamanoTexto.NORMAL)}
          />
          <Opcion
            titulo={t('tamano_grande')}
            descripcion={t('tamano_grande_desc')}
            seleccionado={tamanoTexto === TamanoTexto.GRANDE}
            onClick={() => store.setTamanoTexto(TamanoTexto.GRANDE)}
          />
          <Opcion
            titulo={t('tamano_muy_grande')}
            descripcion={t('tamano_muy_grande_desc')}
            seleccionado={tamanoTexto === TamanoTexto.MUY_GRANDE}
            onClick={() => store.setTamanoTexto(TamanoTexto.MUY_GRANDE)}
          />
          <FilaInterruptor
            titulo={t('negrita_titulo')}
            descripcion={t('negrita_desc')}
            activado={textoNegrita}
            onCambio={(valor) => store.setTextoNegrita(valor)}
          />
        </SeccionDesplegable>

        <SeccionDesplegable
          titulo={t('tema_titulo')}
          subtitulo={t('tema_desc')}
          abierto={estaAbierta('tema')}
          onToggle={() => alternar('tema')}
        >
          <Opcion
            titulo={t('tema_sistema')}
            descripcion={t('tema_sistema_desc')}
            seleccionado={tema === ThemeMode.SISTEMA}
            onClick={() => store.setTema(ThemeMode.SISTEMA)}
          />
          <Opcion
            titulo={t('tema_claro')}
            descripcion={t('tema_claro_desc')}
            seleccionado={tema === ThemeMode.CLARO}
            onClick={() => store.setTema(ThemeMode.CLARO)}
          />
          <Opcion
            titulo={t('tema_oscuro')}
            descripcion={t('tema_oscuro_desc')}
            seleccionado={tema === ThemeMode.OSCURO}
            onClick={() => store.setTema(ThemeMode.OSCURO)}
          />
          <Opcion
            titulo={t('tema_contraste')}
            descripcion={t('tema_contraste_desc')}
            seleccionado={tema === ThemeMode.ALTO_CONTRASTE}
            onClick={() => store.setTema(ThemeMode.ALTO_CONTRASTE)}
          />
        </SeccionDesplegable>

        <SeccionDesplegable
          titulo={t('idioma_titulo')}
          subtitulo={t('idioma_desc')}
          abierto={estaAbierta('idioma')}
          onToggle={() => alternar('idioma')}
        >
          <Opcion
            titulo={t('idioma_sistema')}
            descripcion={t('idioma_sistema_desc')}
            seleccionado={idioma === Idioma.SISTEMA}
            onClick={() => store.setIdioma(Idioma.SISTEMA)}
          />
          <Opcion
            titulo={t('idioma_espanol')}
            descripcion={t('idioma_espanol_desc')}
            seleccionado={idioma === Idioma.ESPANOL}
            onClick={() => store.setIdioma(Idioma.ESPANOL)}
          />
          <Opcion
            titulo={t('idioma_ingles')}
            descripcion={t('idioma_ingles_desc')}
            seleccionado={idioma === Idioma.INGLES}
            onClick={() => store.setIdioma(Idioma.INGLES)}
          />
          <Opcion
            titulo={t('idioma_japones')}
            descripcion={t('idioma_japones_desc')}
            seleccionado={idioma === Idioma.JAPONES}
            onClick={() => store.setIdioma(Idioma.JAPONES)}
          />
        </SeccionDesplegable>

        <hr className="separador" />

        <TarjetaAcceso
          icono={<IconGrupos />}
          titulo={t('tab_groups')}
          descripcion={t('grupos_ajustes_desc')}
          onClick={() => irAOverlay('grupos')}
        />
        <TarjetaAcceso
          icono={<IconRanking />}
          titulo={t('tab_ranking')}
          descripcion={t('ranking_ajustes_desc')}
          onClick={() => irAOverlay('ranking')}
        />
        <TarjetaAcceso
          icono={<IconRanking />}
          titulo={t('ranking_padres_titulo')}
          descripcion={t('ranking_padres_ajustes_desc')}
          onClick={() => irAOverlay('ranking-padres')}
        />
        <TarjetaAcceso
          icono={<IconElenco />}
          titulo={t('tab_elenco')}
          descripcion={t('elenco_ajustes_desc')}
          onClick={() => irA(3)}
        />

        {arboles.length > 0 ? (
          <>
            <hr className="separador" />
            <h2 className="subtitulo-seccion">{t('arboles_ajustes')}</h2>
            <ul className="lista-guardadas">
              {arboles.map((a) => {
                const nombreHijo = modelo?.porId(a.hijoId)
                  ? displayName(modelo.porId(a.hijoId)!, japones)
                  : `#${a.hijoId}`
                return (
                  <li key={a.id}>
                    <article className="card tarjeta-guardada">
                      <button
                        type="button"
                        className="tarjeta-guardada-abrir"
                        onClick={() => {
                          store.abrirArbol(a)
                          irA(2)
                        }}
                      >
                        <span className="tarjeta-guardada-nombre">{a.nombre}</span>
                        <span className="secundario tarjeta-guardada-meta">
                          {nombreHijo} · ◎ {a.total}
                        </span>
                      </button>
                      <button
                        type="button"
                        className="icon-button"
                        onClick={() => store.eliminarArbol(a.id)}
                        aria-label={t('cancelar')}
                      >
                        <IconCerrar />
                      </button>
                    </article>
                  </li>
                )
              })}
            </ul>
          </>
        ) : null}

        <TarjetaAcceso
          icono={<IconAjustes />}
          titulo={t('revisar_accesibilidad')}
          descripcion={t('revisar_accesibilidad_desc')}
          onClick={() => store.abrirBienvenida()}
        />

        <hr className="separador" />
        <p className="version">Uma Afinidad v{__APP_VERSION__}</p>
      </div>
    </div>
  )
}

function FilaInterruptor({
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

function Opcion({
  titulo,
  descripcion,
  seleccionado,
  onClick,
}: {
  titulo: string
  descripcion: string
  seleccionado: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={seleccionado}
      className={seleccionado ? 'opcion seleccionada' : 'opcion'}
      onClick={onClick}
    >
      <span className={seleccionado ? 'radio activo' : 'radio'} aria-hidden="true" />
      <span className="opcion-texto">
        <span className="opcion-titulo">{titulo}</span>
        <span className="secundario">{descripcion}</span>
      </span>
    </button>
  )
}

function SeccionDesplegable({
  titulo,
  subtitulo,
  abierto,
  onToggle,
  children,
}: {
  titulo: string
  subtitulo?: string
  abierto: boolean
  onToggle: () => void
  children: ReactNode
}) {
  const { t } = useI18n()
  const idEstado = `estado-${titulo}`
  return (
    <section className="card seccion-desplegable">
      <button
        type="button"
        className="seccion-cabecera"
        aria-expanded={abierto}
        aria-describedby={idEstado}
        onClick={onToggle}
      >
        <span id={idEstado} className="oculto-visualmente">
          {t(abierto ? 'expandido' : 'contraido')}
        </span>
        <span className="seccion-titulos">
          <span className="seccion-titulo">{titulo}</span>
          {subtitulo ? <span className="secundario seccion-subtitulo">{subtitulo}</span> : null}
        </span>
        <span className="seccion-flecha" aria-hidden="true">
          {abierto ? '∧' : '∨'}
        </span>
      </button>
      {abierto ? <div className="seccion-contenido">{children}</div> : null}
    </section>
  )
}

function TarjetaAcceso({
  icono,
  titulo,
  descripcion,
  onClick,
}: {
  icono: ReactNode
  titulo: string
  descripcion: string
  onClick: () => void
}) {
  return (
    <button type="button" className="card tarjeta-acceso" onClick={onClick}>
      <span className="tarjeta-acceso-icono" aria-hidden="true">
        {icono}
      </span>
      <span className="tarjeta-acceso-texto">
        <span className="opcion-titulo">{titulo}</span>
        <span className="secundario">{descripcion}</span>
      </span>
    </button>
  )
}
