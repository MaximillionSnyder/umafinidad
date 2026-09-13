/* Hook mínimo para cálculos asíncronos (worker). El componente que lo usa
   se remonta con key cuando cambian las entradas, así "cargando" arranca
   en true sin setState sincrónico dentro del efecto. */

import { useEffect, useState } from 'react'

export interface Async<T> {
  datos: T | null
  cargando: boolean
  error: boolean
}

export function useAsync<T>(fn: () => Promise<T>, deps: unknown[]): Async<T> {
  const [datos, setDatos] = useState<T | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    let activo = true
    fn()
      .then((valor) => {
        if (!activo) return
        setDatos(valor)
        setCargando(false)
      })
      .catch(() => {
        if (!activo) return
        setError(true)
        setCargando(false)
      })
    return () => {
      activo = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return { datos, cargando, error }
}
