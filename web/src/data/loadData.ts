/* Carga de los JSON datamined. En el navegador se hace por fetch;
   en tests se usa el helper de src/test/data.ts (import estático).

   Espejo de data/AffinityRepository.kt: se cargan una sola vez y se arma
   el modelo. */

import { AffinityModel } from '../domain/affinity'
import type { Character, Member, Relation } from '../domain/models'
import { parseAptitudes, parseCharacters, parseMembers, parseRelations } from './dtos'

export interface DatosCrudos {
  characters: Character[]
  relations: Relation[]
  members: Member[]
  aptitudes: Map<number, string[]>
}

async function cargarJson(ruta: string): Promise<unknown> {
  const respuesta = await fetch(ruta)
  if (!respuesta.ok) throw new Error(`No se pudo cargar ${ruta}: HTTP ${respuesta.status}`)
  return respuesta.json()
}

export async function cargarDatos(baseUrl = `${import.meta.env.BASE_URL}data/`): Promise<DatosCrudos> {
  const [charactersRaw, relationsRaw, membersRaw, aptitudesRaw] = await Promise.all([
    cargarJson(`${baseUrl}characters.json`),
    cargarJson(`${baseUrl}succession_relation.json`),
    cargarJson(`${baseUrl}succession_relation_member.json`),
    cargarJson(`${baseUrl}aptitudes.json`),
  ])
  return {
    characters: parseCharacters(charactersRaw),
    relations: parseRelations(relationsRaw),
    members: parseMembers(membersRaw),
    aptitudes: parseAptitudes(aptitudesRaw),
  }
}

export function crearModelo(datos: DatosCrudos): AffinityModel {
  return new AffinityModel(datos.characters, datos.relations, datos.members, datos.aptitudes)
}
