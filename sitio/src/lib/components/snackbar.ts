/* Contexto de avisos tipo snackbar. */

import { getContext } from 'svelte'

export interface SnackbarContexto {
  avisar: (mensaje: string) => void
}

export const CLAVE_SNACKBAR = Symbol('snackbar')

export function useSnackbar(): SnackbarContexto {
  return getContext<SnackbarContexto>(CLAVE_SNACKBAR) ?? { avisar: () => {} }
}
