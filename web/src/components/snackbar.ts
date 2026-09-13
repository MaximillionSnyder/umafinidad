/* Contexto de avisos tipo snackbar (hook separado del componente para
   mantener el fast-refresh limpio). */

import { createContext, useContext } from 'react'

export interface SnackbarContexto {
  avisar: (mensaje: string) => void
}

export const ContextoSnackbar = createContext<SnackbarContexto>({ avisar: () => {} })

export function useSnackbar(): SnackbarContexto {
  return useContext(ContextoSnackbar)
}
