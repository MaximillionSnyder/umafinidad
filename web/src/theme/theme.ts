/* Tokens visuales. Espejo de ui/theme/Theme.kt: colores de rangos,
   genealogía, medallas y aptitudes (iguales en ambos temas), más la
   resolución de tema claro/oscuro/alto contraste. */

import { useEffect, useState } from 'react'
import { useAppStore } from '../state/store'
import { ThemeMode } from '../data/prefs'

/* Rangos de afinidad (mismos colores que la PWA) + fondo tintado al 12%. */
export const RankGreat = '#7ED07E'
export const RankGood = '#E7C86A'
export const RankFair = '#D98F8F'

export function colorDeRango(clase: string | null | undefined): string | null {
  if (clase === 'rank-great') return RankGreat
  if (clase === 'rank-good') return RankGood
  if (clase === 'rank-fair') return RankFair
  return null
}

export function fondoDeRango(clase: string | null | undefined): string | null {
  const color = colorDeRango(clase)
  return color === null ? null : `color-mix(in srgb, ${color} 12%, transparent)`
}

/* Colores por genealogía de los slots de herencia:
   hijo naranja; rama del Padre 1 azul (slots 1,3,4); rama del Padre 2
   verde (slots 2,5,6). */
export const RolHijo = '#F08C3A'
export const GenealogiaRama1 = '#82AADD'
export const GenealogiaRama2 = '#7ED07E'

export function colorDeGenealogia(slot: number): string {
  if (slot === 0) return RolHijo
  if (slot === 1 || slot === 3 || slot === 4) return GenealogiaRama1
  return GenealogiaRama2
}

/* Medallas del top de linajes. */
export const MedalOro = '#FFD54F'
export const MedalPlata = '#CFD8DC'
export const MedalBronce = '#CE9B64'

export function colorDeMedalla(pos: number): string | null {
  if (pos === 0) return MedalOro
  if (pos === 1) return MedalPlata
  if (pos === 2) return MedalBronce
  return null
}

/* Escala fija de color por letra (A la mejor → G la peor). */
export function colorDeLetra(letra: string): string {
  switch (letra) {
    case 'A':
      return '#7BD88F'
    case 'B':
      return '#AED581'
    case 'C':
      return '#F2DC6D'
    case 'D':
      return '#F5B455'
    case 'E':
      return '#F08A5D'
    case 'F':
      return '#E85D5D'
    default:
      return '#8A8F98'
  }
}

/* Mismo color base que la PWA: hsl((id × 137.508) % 360 55% 45%).
   Para los avatares se deriva un gradiente radial (claro → oscuro). */
export function colorDeAvatar(id: number): string {
  const hue = (id * 137.508) % 360
  return `hsl(${hue} 55% 45%)`
}

export function gradienteDeAvatar(id: number): string {
  const hue = (id * 137.508) % 360
  return `radial-gradient(circle at 35% 30%, hsl(${hue} 60% 58%), hsl(${hue} 55% 32%))`
}

export function inicialesDe(nombre: string): string {
  return nombre
    .split(/\s+/)
    .map((palabra) => palabra[0])
    .filter((letra): letra is string => letra !== undefined)
    .slice(0, 2)
    .join('')
    .toUpperCase()
}

export type TemaResuelto = 'dark' | 'light' | 'contrast'

export function resolverTema(tema: ThemeMode, sistemaOscuro: boolean): TemaResuelto {
  /* ALTO_CONTRASTE es un tema fijo de base clara con contraste reforzado. */
  if (tema === ThemeMode.ALTO_CONTRASTE) return 'contrast'
  if (tema === ThemeMode.CLARO) return 'light'
  if (tema === ThemeMode.OSCURO) return 'dark'
  return sistemaOscuro ? 'dark' : 'light'
}

function useSistemaOscuro(): boolean {
  const [oscuro, setOscuro] = useState(() =>
    typeof window !== 'undefined' && window.matchMedia
      ? window.matchMedia('(prefers-color-scheme: dark)').matches
      : true,
  )
  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return
    const media = window.matchMedia('(prefers-color-scheme: dark)')
    const onChange = (evento: MediaQueryListEvent) => setOscuro(evento.matches)
    media.addEventListener('change', onChange)
    return () => media.removeEventListener('change', onChange)
  }, [])
  return oscuro
}

export function useTema(): TemaResuelto {
  const { tema } = useAppStore()
  const sistemaOscuro = useSistemaOscuro()
  return resolverTema(tema, sistemaOscuro)
}
