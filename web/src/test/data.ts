/* Helper de tests: carga los JSON del repo Android (copia en public/data)
   y los fixtures de paridad (src/test/fixtures) desde disco, igual que
   hacen los tests JUnit desde los assets. */

import { readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { AffinityModel } from '../domain/affinity'
import type { Character, Member, Relation } from '../domain/models'
import { parseAptitudes, parseCharacters, parseMembers, parseRelations } from '../data/dtos'

const raizWeb = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..')

export function leerJson<T>(rutaRelativa: string): T {
  return JSON.parse(readFileSync(path.join(raizWeb, rutaRelativa), 'utf8')) as T
}

export const characters: Character[] = parseCharacters(
  leerJson('public/data/characters.json'),
)
export const relations: Relation[] = parseRelations(
  leerJson('public/data/succession_relation.json'),
)
export const members: Member[] = parseMembers(
  leerJson('public/data/succession_relation_member.json'),
)
export const aptitudes: Map<number, string[]> = parseAptitudes(
  leerJson('public/data/aptitudes.json'),
)

export const ids: number[] = characters
  .filter((c) => c.playable === true && c.active === true)
  .map((c) => c.charId)

export function crearModelo(): AffinityModel {
  /* Igual que ParidadTest.kt: sin la tabla de aptitudes (no afecta
     ningún cálculo). La app arma el modelo con aptitudes. */
  return new AffinityModel(characters, relations, members)
}

let modeloCompartido: AffinityModel | null = null

/* Modelo único por proceso de test (evita recalcular matrices/top). */
export function modelo(): AffinityModel {
  if (modeloCompartido === null) modeloCompartido = crearModelo()
  return modeloCompartido
}

export function fixture<T>(nombre: string): T {
  return leerJson<T>(`src/test/fixtures/${nombre}`)
}

/* Selecciones base del generador de fixtures (mismas que ParidadTest). */
export function seleccionesBase(): (number | null)[][] {
  const sel0: (number | null)[] = Array(7).fill(null)
  const sel1: (number | null)[] = Array(7).fill(null)
  sel1[0] = ids[0]
  sel1[1] = ids[1]
  sel1[2] = ids[2]
  const sel2: (number | null)[] = Array(7).fill(null)
  sel2[0] = ids[0]
  sel2[1] = ids[1]
  sel2[2] = ids[2]
  sel2[3] = ids[0]
  sel2[4] = ids[3]
  sel2[5] = ids[4]
  sel2[6] = ids[5]
  return [sel0, sel1, sel2]
}
