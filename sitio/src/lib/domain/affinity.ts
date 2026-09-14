/* Porte 1:1 de domain/AffinityModel.kt (que a su vez es porte de
   src/affinity.js de la PWA). Los puntajes, rankings y desempates deben
   dar idénticos a la app Android; los tests de paridad con fixtures lo
   garantizan.

   Nota: Kotlin ordena con TimSort estable y desempates explícitos; acá se
   usan los mismos comparadores y el sort estable de JS (ES2019+). */

import type { Character, Member, Relation } from './models'
import {
  armarArbol,
  puedeIrEn,
  rolDeSlot,
  Rol,
  vinculos,
  type Seleccion,
} from './herencia'

const K_TOP = 300

export interface GrupoCompartido {
  tipo: number
  puntos: number
}

export interface GrupoInfo {
  tipo: number
  puntos: number
  miembros: string[]
}

export interface GrupoResumen {
  tipo: number
  puntos: number
  cantidad: number
}

export interface Rango {
  simbolo: string
  clase: string
}

export interface MejorParAbuelos {
  g1: number
  g2: number
  puntos: number
}

export interface Linaje {
  hijo: Character
  padre: Character
  madre: Character
  /* [rama del padre, rama de la madre][abuelo 1, abuelo 2] */
  abuelos: Character[][]
  puntos: number
}

/* Candidato para reemplazar un slot del árbol de Mi corredora.
   puntosDirectos = aporte del candidato a SUS vínculos con el resto fijo;
   total = puntaje del árbol completo con el cambio aplicado. */
export interface AlternativaSlot {
  personaje: Character
  puntosDirectos: number
  total: number
}

export interface RankingAfinidad {
  personaje: Character
  total: number
}

export interface RankingPadre {
  personaje: Character
  veces: number
  porcentaje: number
  puntosMedios: number
  totalAfinidad: number
}

interface SetBase {
  a: number
  b: number
  c: number
  base: number
}

interface Candidato {
  h: number
  p1: number
  p2: number
  base: number
  total: number
  b1: MejorParAbuelos
  b2: MejorParAbuelos
}

export class AffinityModel {
  readonly personajes: Character[]
  private readonly porIdMap: Map<number, Character>
  private readonly aptitudesPorId: Map<number, string[]>

  private readonly puntoPorTipo = new Map<number, number>()
  private readonly tiposPorChar = new Map<number, Set<number>>()
  private readonly miembrosPorTipo = new Map<number, number[]>()
  private readonly relacionesOrdenadas: Relation[]

  constructor(
    characters: Character[],
    relations: Relation[],
    members: Member[],
    aptitudes: Map<number, string[]> = new Map(),
  ) {
    this.personajes = characters
    this.porIdMap = new Map(characters.map((c) => [c.charId, c]))
    this.aptitudesPorId = aptitudes
    this.relacionesOrdenadas = relations
    for (const r of relations) this.puntoPorTipo.set(r.relationType, r.relationPoint)
    for (const m of members) {
      let tipos = this.tiposPorChar.get(m.charaId)
      if (!tipos) {
        tipos = new Set()
        this.tiposPorChar.set(m.charaId, tipos)
      }
      tipos.add(m.relationType)
      let miembros = this.miembrosPorTipo.get(m.relationType)
      if (!miembros) {
        miembros = []
        this.miembrosPorTipo.set(m.relationType, miembros)
      }
      miembros.push(m.charaId)
    }
  }

  /* Aptitudes (pista/distancia/estilo) de la carta base; null si el
     personaje no está en la tabla. No afecta ningún cálculo. */
  aptitudesDe(id: number): string[] | null {
    return this.aptitudesPorId.get(id) ?? null
  }

  gruposCompartidos(ids: number[]): GrupoCompartido[] {
    if (ids.length < 2) return []
    const sets = ids.map((id) => this.tiposPorChar.get(id) ?? new Set<number>())
    const [primero, ...resto] = sets
    const compartidos: GrupoCompartido[] = []
    for (const tipo of primero) {
      if (resto.every((set) => set.has(tipo))) {
        compartidos.push({ tipo, puntos: this.puntoPorTipo.get(tipo) ?? 0 })
      }
    }
    return compartidos.sort((a, b) => b.puntos - a.puntos || a.tipo - b.tipo)
  }

  puntajePar(a: number, b: number): number {
    if (a === b) return 0
    return this.gruposCompartidos([a, b]).reduce((suma, g) => suma + g.puntos, 0)
  }

  puntajeTrio(a: number, b: number, c: number): number {
    return this.gruposCompartidos([a, b, c]).reduce((suma, g) => suma + g.puntos, 0)
  }

  rango(puntos: number): Rango | null {
    if (puntos >= 20) return { simbolo: '◎', clase: 'rank-great' }
    if (puntos >= 10) return { simbolo: '○', clase: 'rank-good' }
    if (puntos >= 4) return { simbolo: '△', clase: 'rank-fair' }
    return null
  }

  /* Umbrales del juego sobre el total de herencia: ○ ≥ 51, ◎ ≥ 151. */
  rangoTotal(puntos: number): Rango {
    if (puntos >= 151) return { simbolo: '◎', clase: 'rank-great' }
    if (puntos >= 51) return { simbolo: '○', clase: 'rank-good' }
    return { simbolo: '△', clase: 'rank-fair' }
  }

  gruposDeChar(id: number): GrupoInfo[] {
    const tipos = this.tiposPorChar.get(id) ?? new Set<number>()
    const out: GrupoInfo[] = []
    for (const tipo of tipos) {
      out.push({
        tipo,
        puntos: this.puntoPorTipo.get(tipo) ?? 0,
        miembros: (this.miembrosPorTipo.get(tipo) ?? []).map(
          (mid) => this.porIdMap.get(mid)?.enName ?? String(mid),
        ),
      })
    }
    return out.sort((a, b) => b.puntos - a.puntos || a.tipo - b.tipo)
  }

  todosLosGrupos(): GrupoResumen[] {
    return this.relacionesOrdenadas
      .map((r) => ({
        tipo: r.relationType,
        puntos: r.relationPoint,
        cantidad: this.miembrosPorTipo.get(r.relationType)?.length ?? 0,
      }))
      .sort(
        (a, b) =>
          b.puntos - a.puntos || b.cantidad - a.cantidad || a.tipo - b.tipo,
      )
  }

  miembrosDeGrupo(tipo: number): Character[] {
    const out: Character[] = []
    for (const id of this.miembrosPorTipo.get(tipo) ?? []) {
      const c = this.porIdMap.get(id)
      if (c) out.push(c)
    }
    return out
  }

  /* Trío sin allocations, para los barridos masivos del top. */
  puntajeTrioRapido(a: number, b: number, c: number): number {
    const sa = this.tiposPorChar.get(a)
    if (!sa) return 0
    const sb = this.tiposPorChar.get(b)
    if (!sb) return 0
    const sc = this.tiposPorChar.get(c)
    if (!sc) return 0
    let total = 0
    for (const tipo of sa) {
      if (sb.has(tipo) && sc.has(tipo)) total += this.puntoPorTipo.get(tipo) ?? 0
    }
    return total
  }

  /* ===== Estructuras compartidas (top global y mejorLinajeDe) ===== */

  private charsTopCache: Character[] | null = null
  private get charsTop(): Character[] {
    if (this.charsTopCache === null) {
      this.charsTopCache = this.personajes.filter((c) => c.playable === true && c.active === true)
    }
    return this.charsTopCache
  }

  private idsTopCache: number[] | null = null
  private get idsTop(): number[] {
    if (this.idsTopCache === null) this.idsTopCache = this.charsTop.map((c) => c.charId)
    return this.idsTopCache
  }

  /* Matriz triangular superior de puntajes par-a-par. */
  private matrizParesCache: Int32Array | null = null
  private get matrizPares(): Int32Array {
    if (this.matrizParesCache === null) {
      const m = this.charsTop.length
      const ids = this.idsTop
      const s = new Int32Array(m * m)
      for (let i = 0; i < m; i++) {
        for (let j = i + 1; j < m; j++) s[i * m + j] = this.puntajePar(ids[i], ids[j])
      }
      this.matrizParesCache = s
    }
    return this.matrizParesCache
  }

  private parEn(i: number, j: number): number {
    const m = this.charsTop.length
    return i < j ? this.matrizPares[i * m + j] : this.matrizPares[j * m + i]
  }

  private readonly cacheAbuelos = new Map<number, MejorParAbuelos>()

  /* Mejor par de abuelos para la rama del padre p con hijo h.
     Reglas del juego: nadie puede ser abuelo de su propia rama (g === p
     prohibido); el hijo sí puede ser abuelo pero esa relación vale 0
     (corredora). */
  private mejorParAbuelos(h: number, p: number): MejorParAbuelos {
    const m = this.charsTop.length
    const key = h * m + p
    const cacheado = this.cacheAbuelos.get(key)
    if (cacheado) return cacheado
    let t1 = -1
    let t2 = -1
    let g1 = -1
    let g2 = -1
    for (let g = 0; g < m; g++) {
      if (g === p) continue
      const t = g === h ? 0 : this.puntajeTrioRapido(this.idsTop[h], this.idsTop[p], this.idsTop[g])
      if (t > t1) {
        t2 = t1
        g2 = g1
        t1 = t
        g1 = g
      } else if (t > t2) {
        t2 = t
        g2 = g
      }
    }
    const v: MejorParAbuelos = { g1, g2, puntos: t1 + t2 }
    this.cacheAbuelos.set(key, v)
    return v
  }

  /* Top de linajes completos (hijo + padres + mejores abuelos por rama).
     Mismo algoritmo heurístico que la web (K=300 triples base + mejor par
     de abuelos por rama), con los mismos criterios de orden para que los
     empates queden idénticos. */
  topLinajes(n = 20): Linaje[] {
    return this.cacheTop.slice(0, n)
  }

  private cacheTopCache: Linaje[] | null = null
  private get cacheTop(): Linaje[] {
    if (this.cacheTopCache === null) {
      this.cacheTopCache = this.calcularTopSobre(
        Array.from({ length: this.charsTop.length }, (_, i) => i),
        this.cacheAbuelos,
      )
    }
    return this.cacheTopCache
  }

  /* Top de linajes calculado SOLO con los personajes que el usuario posee
     (ids de characters.json). Los ids fuera del pool jugable/activo se
     ignoran; con menos de 3 personajes no hay combinaciones posibles.
     Mismo algoritmo heurístico que el top global. */
  topLinajesDeElenco(idsElenco: Iterable<number>, n = 20): Linaje[] {
    const posPorId = new Map<number, number>()
    for (let i = 0; i < this.idsTop.length; i++) posPorId.set(this.idsTop[i], i)
    const indices = [...new Set([...idsElenco].map((id) => posPorId.get(id)).filter((v): v is number => v !== undefined))]
      .sort((a, b) => a - b)
    if (indices.length < 3) return []
    /* Cache local: los abuelos se eligen dentro del elenco, no valen las
       entradas calculadas para el pool completo. */
    return this.calcularTopSobre(indices, new Map()).slice(0, n)
  }

  /* Núcleo compartido del top de linajes sobre un subconjunto de índices
     de charsTop. Con indices = 0..m-1 y el cache global reproduce
     exactamente el algoritmo de la web: misma enumeración de triples
     (i < j < k sobre índices crecientes) y mismos ordenamientos estables,
     para que los empates queden idénticos. */
  private calcularTopSobre(
    indices: number[],
    cacheAbuelos: Map<number, MejorParAbuelos>,
  ): Linaje[] {
    const chars = this.charsTop
    const m = chars.length
    const ids = this.idsTop

    /* Mejor par de abuelos para la rama del padre p con hijo h,
       restringido al subconjunto. */
    const mejorPar = (h: number, p: number): MejorParAbuelos => {
      const key = h * m + p
      const cacheado = cacheAbuelos.get(key)
      if (cacheado) return cacheado
      let t1 = -1
      let t2 = -1
      let g1 = -1
      let g2 = -1
      for (const g of indices) {
        if (g === p) continue
        const t = g === h ? 0 : this.puntajeTrioRapido(ids[h], ids[p], ids[g])
        if (t > t1) {
          t2 = t1
          g2 = g1
          t1 = t
          g1 = g
        } else if (t > t2) {
          t2 = t
          g2 = g
        }
      }
      const v: MejorParAbuelos = { g1, g2, puntos: t1 + t2 }
      cacheAbuelos.set(key, v)
      return v
    }

    const sets: SetBase[] = []
    for (let i = 0; i < indices.length; i++) {
      for (let j = i + 1; j < indices.length; j++) {
        for (let k = j + 1; k < indices.length; k++) {
          const h = indices[i]
          const p1 = indices[j]
          const p2 = indices[k]
          const base = this.parEn(h, p1) + this.parEn(h, p2) + this.parEn(p1, p2)
          if (sets.length < K_TOP || base > sets[sets.length - 1].base) {
            sets.push({ a: h, b: p1, c: p2, base })
            sets.sort((x, y) => y.base - x.base) /* estable, igual que JS */
            if (sets.length > K_TOP) sets.length = K_TOP
          }
        }
      }
    }

    const candidatos: Candidato[] = []
    for (const st of sets) {
      const permutaciones: [number, number, number][] = [
        [st.a, st.b, st.c],
        [st.b, st.a, st.c],
        [st.c, st.a, st.b],
      ]
      for (const [h, p1, p2] of permutaciones) {
        const b1 = mejorPar(h, p1)
        const b2 = mejorPar(h, p2)
        candidatos.push({
          h,
          p1,
          p2,
          base: st.base,
          total: st.base + b1.puntos + b2.puntos,
          b1,
          b2,
        })
      }
    }

    candidatos.sort((x, y) => y.total - x.total) /* estable, igual que JS */

    const charEn = (i: number): Character => chars[i]

    return candidatos.map((r) => ({
      hijo: charEn(r.h),
      padre: charEn(r.p1),
      madre: charEn(r.p2),
      abuelos: [
        [charEn(r.b1.g1), charEn(r.b1.g2)],
        [charEn(r.b2.g1), charEn(r.b2.g2)],
      ],
      puntos: r.total,
    }))
  }

  /* Mejor linaje EXACTO para un hijo dado: se recorren todos los pares de
     padres posibles (~m²/2) y se completa cada rama con su mejor par de
     abuelos. Sin heurística: es el óptimo real para esa corredora.
     Devuelve null si el personaje no está en el pool jugable/activo. */
  mejorLinajeDe(hijoId: number): Linaje | null {
    const chars = this.charsTop
    const m = chars.length
    const h = this.idsTop.indexOf(hijoId)
    if (h < 0) return null

    /* Calienta la matriz una sola vez. */
    void this.matrizPares

    let mejorP1 = -1
    let mejorP2 = -1
    let mejorTotal = -1
    let mejorB1: MejorParAbuelos | null = null
    let mejorB2: MejorParAbuelos | null = null

    for (let p1 = 0; p1 < m; p1++) {
      if (p1 === h) continue
      for (let p2 = p1 + 1; p2 < m; p2++) {
        if (p2 === h) continue
        const base = this.parEn(h, p1) + this.parEn(h, p2) + this.parEn(p1, p2)
        const b1 = this.mejorParAbuelos(h, p1)
        const b2 = this.mejorParAbuelos(h, p2)
        const total = base + b1.puntos + b2.puntos
        if (total > mejorTotal) {
          mejorTotal = total
          mejorP1 = p1
          mejorP2 = p2
          mejorB1 = b1
          mejorB2 = b2
        }
      }
    }
    if (mejorB1 === null || mejorB2 === null) return null

    const charEn = (i: number): Character => chars[i]

    return {
      hijo: charEn(h),
      padre: charEn(mejorP1),
      madre: charEn(mejorP2),
      abuelos: [
        [charEn(mejorB1.g1), charEn(mejorB1.g2)],
        [charEn(mejorB2.g1), charEn(mejorB2.g2)],
      ],
      puntos: mejorTotal,
    }
  }

  porId(id: number): Character | null {
    return this.porIdMap.get(id) ?? null
  }

  /* ===== Ranking “Umas más versátiles” (total afinidad) ===== */

  private umbralRankingGreat: number | null = null
  private umbralRankingGood: number | null = null

  private cacheRankingCache: RankingAfinidad[] | null = null

  rankingAfinidad(): RankingAfinidad[] {
    if (this.cacheRankingCache === null) this.cacheRankingCache = this.calcularRankingAfinidad()
    return this.cacheRankingCache
  }

  rangoRanking(total: number): Rango {
    if (this.umbralRankingGreat === null) {
      // Asegura que los umbrales estén calculados (inicializa cache si es necesario)
      if (this.charsTop.length === 0) return { simbolo: '△', clase: 'rank-fair' }
      this.rankingAfinidad()
    }
    if (total >= this.umbralRankingGreat!) return { simbolo: '◎', clase: 'rank-great' }
    if (total >= this.umbralRankingGood!) return { simbolo: '○', clase: 'rank-good' }
    return { simbolo: '△', clase: 'rank-fair' }
  }

  private calcularRankingAfinidad(): RankingAfinidad[] {
    const m = this.charsTop.length
    if (m === 0) {
      this.umbralRankingGreat = Number.MAX_SAFE_INTEGER
      this.umbralRankingGood = Number.MAX_SAFE_INTEGER
      return []
    }
    // Suma por fila de la matriz simétrica (reusa matrizPares/parEn)
    const totales = new Int32Array(m)
    for (let i = 0; i < m; i++) {
      let t = 0
      for (let j = 0; j < m; j++) if (i !== j) t += this.parEn(i, j)
      totales[i] = t
    }
    const ordenados = [...totales].sort((a, b) => b - a)
    const idxGreat = Math.min(Math.max(Math.ceil(m * 0.1) - 1, 0), m - 1)
    const idxGood = Math.min(Math.max(Math.ceil(m * 0.5) - 1, 0), m - 1)
    this.umbralRankingGreat = ordenados[idxGreat]
    this.umbralRankingGood = ordenados[idxGood]
    return Array.from({ length: m }, (_, i) => ({
      personaje: this.charsTop[i],
      total: totales[i],
    })).sort((a, b) => b.total - a.total || a.personaje.charId - b.personaje.charId)
  }

  /* ===== Ranking “Mejores padres” (primera opción como padre) ===== */

  private cacheRankingPadresCache: RankingPadre[] | null = null

  rankingPadres(): RankingPadre[] {
    if (this.cacheRankingPadresCache === null) {
      this.cacheRankingPadresCache = this.calcularRankingPadres()
    }
    return this.cacheRankingPadresCache
  }

  private calcularRankingPadres(): RankingPadre[] {
    const m = this.charsTop.length
    if (m === 0) return []
    // Totales de afinidad para el desempate (misma fuente que rankingAfinidad).
    const totalPorId = new Map<number, number>()
    for (let i = 0; i < m; i++) {
      let t = 0
      for (let j = 0; j < m; j++) if (i !== j) t += this.parEn(i, j)
      totalPorId.set(this.idsTop[i], t)
    }
    const apariciones = new Map<number, number[]>()
    let hijosComputados = 0
    for (const hijoId of this.idsTop) {
      const linaje = this.mejorLinajeDe(hijoId)
      if (!linaje) continue
      hijosComputados++
      for (const id of [linaje.padre.charId, linaje.madre.charId]) {
        let lista = apariciones.get(id)
        if (!lista) {
          lista = []
          apariciones.set(id, lista)
        }
        lista.push(linaje.puntos)
      }
    }
    return this.charsTop
      .map((c) => {
        const puntos = apariciones.get(c.charId) ?? []
        const veces = puntos.length
        const suma = puntos.reduce((s, v) => s + v, 0)
        return {
          personaje: c,
          veces,
          porcentaje: hijosComputados === 0 ? 0 : (veces * 100) / hijosComputados,
          puntosMedios: puntos.length === 0 ? 0 : Math.trunc(suma / puntos.length),
          totalAfinidad: totalPorId.get(c.charId) ?? 0,
        }
      })
      .sort(
        (a, b) =>
          b.veces - a.veces ||
          b.puntosMedios - a.puntosMedios ||
          b.totalAfinidad - a.totalAfinidad ||
          a.personaje.charId - b.personaje.charId,
      )
  }

  /* ===== Alternativas por slot (Mi corredora) ===== */

  /* Total del árbol completo con la semántica de result.js:
     hijo×padres + entre padres + tríos hijo-padre-abuelo (corredora 0). */
  private totalDeSeleccion(seleccion: Seleccion): number {
    return vinculos(armarArbol(seleccion)).reduce((total, v) => {
      if (v.esCorredora) return total
      if (v.ids.length === 3) return total + this.puntajeTrioRapido(v.ids[0], v.ids[1], v.ids[2])
      return total + this.puntajePar(v.ids[0], v.ids[1])
    }, 0)
  }

  /* Candidatos para reemplazar el ocupante de un slot (1..6), ordenados
     por total resultante descendente. Respeta todas las reglas del juego
     vía puedeIrEn. El slot del hijo no es intercambiable. */
  alternativasParaSlot(seleccion: Seleccion, slot: number, limite = 8): AlternativaSlot[] {
    if (slot <= 0 || slot >= seleccion.length) return []
    const ocupante = seleccion[slot]
    if (ocupante === null) return []
    const hId = seleccion[0]
    if (hId === null) return []
    const selArr = seleccion.slice()

    const resultados: AlternativaSlot[] = []
    const m = this.charsTop.length

    for (let idx = 0; idx < m; idx++) {
      const candidato = this.charsTop[idx]
      if (candidato.charId === ocupante) continue
      if (!puedeIrEn(selArr, slot, candidato.charId)) continue

      /* Aporte directo según el rol del slot. */
      let directos: number
      const rol = rolDeSlot(slot)
      if (rol === Rol.PADRE) {
        const rama = slot - 1
        const otroPadre = slot === 1 ? seleccion[2]! : seleccion[1]!
        let d =
          this.puntajePar(hId, candidato.charId) +
          this.puntajePar(candidato.charId, otroPadre)
        for (const g of [seleccion[3 + rama * 2], seleccion[4 + rama * 2]]) {
          if (g !== null && g !== candidato.charId && g !== hId) {
            d += this.puntajeTrioRapido(hId, candidato.charId, g)
          }
        }
        directos = d
      } else if (rol === Rol.ABUELO) {
        const padreId = seleccion[1 + Math.floor((slot - 3) / 2)]!
        directos = candidato.charId === hId ? 0 : this.puntajeTrioRapido(hId, padreId, candidato.charId)
      } else {
        continue
      }

      const nuevo = selArr.slice()
      nuevo[slot] = candidato.charId
      resultados.push({
        personaje: candidato,
        puntosDirectos: directos,
        total: this.totalDeSeleccion(nuevo),
      })
    }

    return resultados
      .sort((a, b) => b.total - a.total || b.puntosDirectos - a.puntosDirectos)
      .slice(0, limite)
  }
}
