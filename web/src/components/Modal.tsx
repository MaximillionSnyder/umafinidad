/* Modal inferior y diálogo de confirmación con semántica accesible:
   role dialog/alertdialog, foco inicial, Escape, trampa de foco y
   devolución del foco al cerrar. */

import { useEffect, useRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { useI18n } from '../i18n'

function useFocoModal(
  open: boolean,
  onClose: () => void,
  ref: React.RefObject<HTMLElement | null>,
  bloqueante = false,
) {
  useEffect(() => {
    if (!open) return
    const anterior = document.activeElement as HTMLElement | null
    const contenedor = ref.current
    contenedor?.focus()

    const onKeyDown = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        if (bloqueante) {
          evento.preventDefault()
          evento.stopPropagation()
          return
        }
        evento.stopPropagation()
        onClose()
        return
      }
      if (evento.key !== 'Tab' || !contenedor) return
      const focusables = contenedor.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      )
      if (focusables.length === 0) {
        evento.preventDefault()
        return
      }
      const primero = focusables[0]
      const ultimo = focusables[focusables.length - 1]
      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primero.focus()
      }
    }

    document.addEventListener('keydown', onKeyDown, true)
    const overflowPrevio = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKeyDown, true)
      document.body.style.overflow = overflowPrevio
      anterior?.focus()
    }
  }, [open, onClose, ref, bloqueante])
}

function BotonFondo({ onClose }: { onClose: () => void }) {
  const { t } = useI18n()
  return (
    <button
      type="button"
      className="sheet-backdrop-boton"
      aria-label={t('volver')}
      tabIndex={-1}
      onClick={onClose}
    />
  )
}

interface BottomSheetProps {
  open: boolean
  onClose: () => void
  labelledBy: string
  children: ReactNode
}

export function BottomSheet({ open, onClose, labelledBy, children }: BottomSheetProps) {
  const ref = useRef<HTMLDivElement>(null)
  useFocoModal(open, onClose, ref)
  if (!open) return null
  return createPortal(
    <div className="sheet-backdrop">
      <BotonFondo onClose={onClose} />
      <div ref={ref} tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby={labelledBy} className="sheet">
        <div className="sheet-handle" aria-hidden="true" />
        {children}
      </div>
    </div>,
    document.body,
  )
}

interface DialogoProps {
  open: boolean
  titulo: string
  descripcion?: string
  onClose: () => void
  /* Bloqueante: sin cierre por fondo ni Escape (bienvenida de accesibilidad). */
  bloqueante?: boolean
  children: ReactNode
}

export function Dialogo({ open, titulo, descripcion, onClose, bloqueante = false, children }: DialogoProps) {
  const ref = useRef<HTMLDivElement>(null)
  useFocoModal(open, onClose, ref, bloqueante)
  if (!open) return null
  const idTitulo = 'dialogo-titulo'
  return createPortal(
    <div className="sheet-backdrop centrado-arriba">
      {bloqueante ? null : <BotonFondo onClose={onClose} />}
      <div
        ref={ref}
        tabIndex={-1}
        role="alertdialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="dialogo"
      >
        <h2 id={idTitulo}>{titulo}</h2>
        {descripcion ? <p className="secundario">{descripcion}</p> : null}
        <div className="dialogo-acciones">{children}</div>
      </div>
    </div>,
    document.body,
  )
}
