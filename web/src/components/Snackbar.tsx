/* Avisos tipo snackbar con región viva (equivalente a SnackbarHost). */

import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { ContextoSnackbar } from './snackbar'

export function SnackbarProvider({ children }: { children: ReactNode }) {
  const [mensaje, setMensaje] = useState<string | null>(null)
  const temporizador = useRef<number | null>(null)

  const avisar = useCallback((texto: string) => {
    setMensaje(texto)
    if (temporizador.current !== null) window.clearTimeout(temporizador.current)
    temporizador.current = window.setTimeout(() => setMensaje(null), 4000)
  }, [])

  useEffect(() => {
    return () => {
      if (temporizador.current !== null) window.clearTimeout(temporizador.current)
    }
  }, [])

  return (
    <ContextoSnackbar.Provider value={{ avisar }}>
      {children}
      <div className="snackbar-host">
        <div role="status" aria-live="polite" className={mensaje ? 'snackbar visible' : 'snackbar'}>
          {mensaje}
        </div>
      </div>
    </ContextoSnackbar.Provider>
  )
}
