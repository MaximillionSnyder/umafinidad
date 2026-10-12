/* Mensajes entre la UI y el Web Worker de cálculo pesado. */

import type { AlternativaSlot, Linaje, RankingAfinidad, RankingPadre } from '../domain/affinity'

/* Base de los JSON que envía el hilo principal: el Worker no puede
   importar `$app/paths` (arrastra el runtime cliente y `window`). */
export type ConBase = { base: string }

export type ComandoCalculo =
  | { id: number; tipo: 'topLinajes'; n: number }
  | { id: number; tipo: 'topLinajesDeElenco'; ids: number[]; n: number }
  | { id: number; tipo: 'rankingAfinidad' }
  | { id: number; tipo: 'rankingPadres' }
  | { id: number; tipo: 'mejorLinajeDe'; hijoId: number }
  | { id: number; tipo: 'alternativasParaSlot'; seleccion: (number | null)[]; slot: number; limite: number }

export type PeticionCalculo = ComandoCalculo & ConBase

export type RespuestaCalculo =
  | { id: number; resultado: Linaje[] }
  | { id: number; resultado: RankingAfinidad[] }
  | { id: number; resultado: RankingPadre[] }
  | { id: number; resultado: Linaje | null }
  | { id: number; resultado: AlternativaSlot[] }

export type ErrorCalculo = { id: number; error: string }
