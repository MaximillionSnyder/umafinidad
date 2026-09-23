/* Emite códigos de licencia Pro para Uma Afinidad.

   El algoritmo es el mismo que valida la app en
   `app/src/main/java/com/maximillionsnyder/umafinidad/data/ProRepository.kt`
   (`LicenciaPro`): cuerpo aleatorio de 8 caracteres y firma de 4 derivada con
   FNV-1a de 32 bits + finalizador de murmur3. Los vectores de `ProTest.kt`
   fijan ese contrato: si cambia acá, cambia allá.

   Uso:
     node scripts/generar-codigos-pro.mjs            # 5 códigos
     node scripts/generar-codigos-pro.mjs 20         # 20 códigos
     node scripts/generar-codigos-pro.mjs --verificar UMA-XXXX-XXXX-XXXX */
import { randomInt } from 'node:crypto'

const ALFABETO = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'
const PREFIJO = 'UMA'
const LARGO_CUERPO = 8
const LARGO_FIRMA = 4
const LARGO_TOTAL = PREFIJO.length + LARGO_CUERPO + LARGO_FIRMA

export function normalizar(bruto) {
  return [...bruto.toUpperCase()].filter((c) => /[A-Z0-9]/.test(c)).join('')
}

export function firmaDe(cuerpo) {
  let h = 2166136261
  for (const c of cuerpo) {
    h = (h ^ c.charCodeAt(0)) >>> 0
    h = Math.imul(h, 16777619) >>> 0
  }
  h = (h ^ (h >>> 16)) >>> 0
  h = Math.imul(h, 0x85ebca6b) >>> 0
  h = (h ^ (h >>> 13)) >>> 0
  h = Math.imul(h, 0xc2b2ae35) >>> 0
  h = (h ^ (h >>> 16)) >>> 0
  let salida = ''
  let x = h
  for (let i = 0; i < LARGO_FIRMA; i++) {
    salida += ALFABETO[x & 31]
    x >>>= 5
  }
  return salida
}

export function esValida(bruto) {
  const limpio = normalizar(bruto)
  if (limpio.length !== LARGO_TOTAL) return false
  if (!limpio.startsWith(PREFIJO)) return false
  const cuerpo = limpio.slice(PREFIJO.length, PREFIJO.length + LARGO_CUERPO)
  const firma = limpio.slice(PREFIJO.length + LARGO_CUERPO)
  if ([...cuerpo].some((c) => !ALFABETO.includes(c))) return false
  return firma === firmaDe(cuerpo)
}

export function formatear(bruto) {
  const limpio = normalizar(bruto)
  if (limpio.length !== LARGO_TOTAL) return limpio
  return [
    limpio.slice(0, 3),
    limpio.slice(3, 7),
    limpio.slice(7, 11),
    limpio.slice(11, 15),
  ].join('-')
}

export function generar() {
  let cuerpo = ''
  for (let i = 0; i < LARGO_CUERPO; i++) cuerpo += ALFABETO[randomInt(ALFABETO.length)]
  return formatear(PREFIJO + cuerpo + firmaDe(cuerpo))
}

/* Solo cuando se ejecuta como script (importarlo no imprime nada). */
if (process.argv[1] && process.argv[1].endsWith('generar-codigos-pro.mjs')) {
  const args = process.argv.slice(2)
  if (args[0] === '--verificar') {
    const codigo = args[1] ?? ''
    console.log(`${formatear(codigo)} -> ${esValida(codigo) ? 'valido' : 'invalido'}`)
  } else {
    const cantidad = Math.max(1, Number.parseInt(args[0] ?? '5', 10) || 5)
    for (let i = 0; i < cantidad; i++) console.log(generar())
  }
}
