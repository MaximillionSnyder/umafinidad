/* DTOs del JSON datamined (snake_case) y conversión al dominio camelCase.
   Espejo de data/Dtos.kt. El parser ignora los campos que no se usan. */

import type { Character, Member, Relation } from '../domain/models'

interface CharacterDto {
  char_id: number
  en_name?: string | null
  jp_name?: string | null
  playable?: boolean | null
  active?: boolean | null
  url_name?: string | null
}

interface RelationDto {
  relation_type: number
  relation_point: number
}

interface MemberDto {
  chara_id: number
  relation_type: number
}

export function parseCharacters(raw: unknown): Character[] {
  const lista = raw as CharacterDto[]
  return lista.map((d) => ({
    charId: d.char_id,
    enName: d.en_name ?? null,
    jpName: d.jp_name ?? null,
    playable: d.playable ?? null,
    active: d.active ?? null,
    urlName: d.url_name ?? null,
  }))
}

export function parseRelations(raw: unknown): Relation[] {
  return (raw as RelationDto[]).map((d) => ({
    relationType: d.relation_type,
    relationPoint: d.relation_point,
  }))
}

export function parseMembers(raw: unknown): Member[] {
  return (raw as MemberDto[]).map((d) => ({
    charaId: d.chara_id,
    relationType: d.relation_type,
  }))
}

/* aptitudes.json: mapa char_id (como string) → 10 letras A–G. Las claves
   se convierten a Int para alinearlas con el resto del modelo. */
export function parseAptitudes(raw: unknown): Map<number, string[]> {
  const mapa = raw as Record<string, string[]>
  return new Map(Object.entries(mapa).map(([id, apt]) => [Number(id), apt]))
}
