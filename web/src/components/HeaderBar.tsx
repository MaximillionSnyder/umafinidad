/* Cabeceras uniformes (altura fija) para tabs y pantallas de Más.
   Espejo de ui/componentes/Componentes.kt (HeaderBar y
   HeaderBarConVolver) con semántica de encabezado. */

import type { ReactNode } from 'react'
import { IconAtras } from './Icons'
import { useI18n } from '../i18n'

interface HeaderBarProps {
  titulo: string
  pillTexto?: string
  chip?: ReactNode
}

export function HeaderBar({ titulo, pillTexto, chip }: HeaderBarProps) {
  return (
    <header className="header-bar">
      <h1>{titulo}</h1>
      {chip}
      {pillTexto ? <span className="header-pill">{pillTexto}</span> : null}
    </header>
  )
}

interface HeaderBarConVolverProps extends HeaderBarProps {
  onVolver: () => void
}

export function HeaderBarConVolver({ titulo, onVolver, pillTexto }: HeaderBarConVolverProps) {
  const { t } = useI18n()
  return (
    <header className="header-bar" style={{ paddingLeft: '0.25rem' }}>
      <button type="button" className="icon-button" onClick={onVolver} aria-label={t('volver')}>
        <IconAtras />
      </button>
      <h1>{titulo}</h1>
      {pillTexto ? <span className="header-pill">{pillTexto}</span> : null}
    </header>
  )
}
