/* Elenco personal: ids de los personajes que el usuario posee ("Mis
   Umas"). Espejo de data/ElencoRepository.kt. */

const CLAVE = 'mi_elenco.ids'

export function serializarElenco(ids: Set<number>): string {
  return JSON.stringify([...ids])
}

export function deserializarElenco(json: string): Set<number> {
  const datos = JSON.parse(json) as number[]
  if (!Array.isArray(datos)) throw new Error('Formato de elenco inválido')
  return new Set(datos)
}

export class ElencoRepository {
  obtener(): Set<number> {
    try {
      const raw = localStorage.getItem(CLAVE)
      return raw === null ? new Set() : deserializarElenco(raw)
    } catch {
      return new Set()
    }
  }

  reemplazar(ids: Set<number>): void {
    try {
      localStorage.setItem(CLAVE, serializarElenco(ids))
    } catch {
      /* sin storage disponible */
    }
  }
}
