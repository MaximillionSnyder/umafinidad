/* Extrae app/src/main/res/values{,-es,-ja}/strings.xml a JSON para i18n.
   - Desescapa entidades XML y escapes Android (\', \n, \uXXXX…).
   - Convierte argumentos posicionales Android (%1$s, %2$d) a {0}, {1}.
   - Verifica paridad de claves entre los tres idiomas.

   Uso: npm run strings */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const sitioRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const appRoot = path.resolve(sitioRoot, '..')
const res = path.join(appRoot, 'app', 'src', 'main', 'res')
const salida = path.join(sitioRoot, 'src', 'lib', 'i18n', 'locales')

const idiomas = [
  { carpeta: 'values', codigo: 'en' },
  { carpeta: 'values-es', codigo: 'es' },
  { carpeta: 'values-ja', codigo: 'ja' },
]

const desescapaXml = (s) =>
  s
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&amp;/g, '&')

const desescapaAndroid = (s) =>
  s.replace(/\\(u[0-9a-fA-F]{4}|.)/g, (_, grupo) => {
    if (grupo.startsWith('u')) return String.fromCharCode(parseInt(grupo.slice(1), 16))
    if (grupo === 'n') return '\n'
    if (grupo === 't') return '\t'
    return grupo
  })

/* %1$s / %2$d (con flags opcionales) → {0} / {1} */
const conversaFormato = (s) => s.replace(/%(\d+)\$[sd]/g, (_, n) => `{${Number(n) - 1}}`)

const parsear = (xml) => {
  const strings = {}
  const re = /<string\s+name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g
  let m
  while ((m = re.exec(xml)) !== null) {
    strings[m[1]] = conversaFormato(desescapaAndroid(desescapaXml(m[2])))
  }
  return strings
}

fs.mkdirSync(salida, { recursive: true })
const porIdioma = {}
for (const { carpeta, codigo } of idiomas) {
  const archivo = path.join(res, carpeta, 'strings.xml')
  if (!fs.existsSync(archivo)) {
    console.error(`No existe: ${archivo}`)
    process.exit(1)
  }
  const strings = parsear(fs.readFileSync(archivo, 'utf8'))
  const ordenado = Object.fromEntries(Object.entries(strings).sort(([a], [b]) => a.localeCompare(b)))
  fs.writeFileSync(path.join(salida, `${codigo}.json`), `${JSON.stringify(ordenado, null, 2)}\n`)
  porIdioma[codigo] = new Set(Object.keys(strings))
  console.log(`${codigo}: ${porIdioma[codigo].size} strings`)
}

const base = porIdioma.en
for (const { codigo } of idiomas) {
  const actual = porIdioma[codigo]
  const faltan = [...base].filter((k) => !actual.has(k))
  const sobran = [...actual].filter((k) => !base.has(k))
  if (faltan.length || sobran.length) {
    console.error(`Paridad rota en ${codigo}: faltan=${faltan.length} sobran=${sobran.length}`)
    if (faltan.length) console.error('  faltan:', faltan.slice(0, 10).join(', '))
    if (sobran.length) console.error('  sobran:', sobran.slice(0, 10).join(', '))
    process.exit(1)
  }
}
console.log('Paridad de claves OK (en/es/ja)')
