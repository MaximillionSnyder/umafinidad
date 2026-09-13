/* Aptitudes de la carta base (data de GameTora): 10 letras A–G en orden
   fijo. Espejo de domain/Aptitudes.kt. */

export const APT_TURF = 0
export const APT_DIRT = 1
export const APT_CORTA = 2
export const APT_MILLA = 3
export const APT_MEDIA = 4
export const APT_LARGA = 5
export const APT_FUGA = 6
export const APT_VANGUARDIA = 7
export const APT_REMATE = 8
export const APT_RETRASO = 9

export function aptitudValida(apt: string[]): boolean {
  return apt.length === 10 && apt.every((letra) => letra.length === 1 && letra >= 'A' && letra <= 'G')
}

/* Índices de lo que la Uma hace bien (A o B). Para los chips compactos
   de las grillas. */
export function aptitudesDestacadas(apt: string[]): number[] {
  const out: number[] = []
  apt.forEach((v, i) => {
    if (v === 'A' || v === 'B') out.push(i)
  })
  return out
}
