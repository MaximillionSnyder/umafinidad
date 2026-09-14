/* Mensajes entre la UI y el Web Worker de cálculo pesado. */

import type { AlternativaSlot, Linaje, RankingAfinidad, RankingPadre } from '../domain/affinity'

export type PeticionCalculo =
  | { id: number; tipo: 'topLinajes'; n: number }
  | { id: number; tipo: 'topLinajesDeElenco'; ids: number[]; n: number }
  | { id: number; tipo: 'rankingAfinidad' }
  | { id: number; tipo: 'rankingPadres' }
  | { id: number; tipo: 'mejorLinajeDe'; hijoId: number }
  | { id: number; tipo: 'alternativasParaSlot'; seleccion: (number | null)[]; slot: number; limite: number }

export type RespuestaCalculo =
  | { id: number; resultado: Linaje[] }
  | { id: number; resultado: RankingAfinidad[] }
  | { id: number; resultado: RankingPadre[] }
  | { id: number; resultado: Linaje | null }
  | { id: number; resultado: AlternativaSlot[] }

export type ErrorCalculo = { id: number; error: string }
