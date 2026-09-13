/* Modelos de entrada (JSON datamined de GameTora). Espejo de domain/Models.kt. */

export interface Character {
  charId: number
  enName: string | null
  jpName: string | null
  playable: boolean | null
  active: boolean | null
  urlName: string | null
}

export interface Relation {
  relationType: number
  relationPoint: number
}

export interface Member {
  charaId: number
  relationType: number
}

/* Nombre visible según idioma: japonés → jp_name, cualquier otro → en_name
   (con fallback cruzado). */
export function displayName(c: Character, japones: boolean): string {
  const principal = japones ? c.jpName ?? c.enName : c.enName ?? c.jpName
  return principal ?? `char ${c.charId}`
}
