/* Verifica que las 9 traducciones de la app tengan las mismas claves y los
   mismos marcadores de posición que `values/strings.xml` (la fuente en
   inglés), y que no haya apóstrofos sin escapar (aapt2 los rechaza).

   Uso: node scripts/verificar-strings.mjs */
import { readdirSync, readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const raiz = join(dirname(fileURLToPath(import.meta.url)), '..')
const res = join(raiz, 'app', 'src', 'main', 'res')

const locales = readdirSync(res)
  .filter((d) => d === 'values' || d.startsWith('values-'))
  .sort()

function leer(locale) {
  const xml = readFileSync(join(res, locale, 'strings.xml'), 'utf8')
  const claves = new Map()
  const re = /<string name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g
  let m
  while ((m = re.exec(xml)) !== null) {
    const [, nombre, cuerpo] = m
    if (claves.has(nombre)) throw new Error(`${locale}: clave duplicada ${nombre}`)
    claves.set(nombre, cuerpo)
  }
  return claves
}

const base = leer('values')
const problemas = []

for (const locale of locales) {
  const claves = leer(locale)
  for (const nombre of base.keys()) {
    if (!claves.has(nombre)) problemas.push(`${locale}: falta ${nombre}`)
  }
  for (const nombre of claves.keys()) {
    if (!base.has(nombre)) problemas.push(`${locale}: sobra ${nombre}`)
  }
  for (const [nombre, cuerpo] of claves) {
    const esperados = (base.get(nombre)?.match(/%\d+\$s/g) ?? []).join(',')
    const actuales = (cuerpo.match(/%\d+\$s/g) ?? []).join(',')
    if (esperados !== actuales) {
      problemas.push(`${locale}: ${nombre} espera [${esperados}] y tiene [${actuales}]`)
    }
    /* Apóstrofo sin escapar: rompe aapt2. */
    if (/(^|[^\\])'/.test(cuerpo)) problemas.push(`${locale}: ${nombre} tiene un apóstrofo sin escapar`)
  }
}

console.log(`${locales.length} locales, ${base.size} claves en values/`)
if (problemas.length) {
  problemas.forEach((p) => console.error(`  ✗ ${p}`))
  process.exit(1)
}
console.log('  ✓ claves y marcadores idénticos en todas las traducciones')
