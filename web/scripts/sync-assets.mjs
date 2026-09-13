/* Copia a web/ los activos que son fuente de verdad del repo Android:
   - app/src/main/assets/data/*.json        → public/data/
   - app/src/main/res/drawable-nodpi/avatar*.png → public/avatars/{color,bw5,bw1}/
   - app/src/test/resources/fixtures/*.json → src/test/fixtures/

   Uso: npm run assets
   No se versionan las copias de public/ (ver .gitignore del proyecto web);
   los fixtures sí, para que `npm test` funcione sin pasos previos. */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const webRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const appRoot = path.resolve(webRoot, '..')

const datosOrigen = path.join(appRoot, 'app', 'src', 'main', 'assets', 'data')
const avataresOrigen = path.join(appRoot, 'app', 'src', 'main', 'res', 'drawable-nodpi')
const fixturesOrigen = path.join(appRoot, 'app', 'src', 'test', 'resources', 'fixtures')

const datosDestino = path.join(webRoot, 'public', 'data')
const avataresDestino = path.join(webRoot, 'public', 'avatars')
const fixturesDestino = path.join(webRoot, 'src', 'test', 'fixtures')

const exigir = (ruta) => {
  if (!fs.existsSync(ruta)) {
    console.error(`No existe: ${ruta}`)
    console.error('Corré el script desde web/ (npm run assets) con el repo Android completo.')
    process.exit(1)
  }
}

const copiarDirectorio = (origen, destino, filtro = () => true) => {
  fs.mkdirSync(destino, { recursive: true })
  let n = 0
  for (const nombre of fs.readdirSync(origen)) {
    if (!filtro(nombre)) continue
    fs.copyFileSync(path.join(origen, nombre), path.join(destino, nombre))
    n++
  }
  return n
}

/* ---- JSON datamined ---- */
exigir(datosOrigen)
const nDatos = copiarDirectorio(datosOrigen, datosDestino, (n) => n.endsWith('.json'))
console.log(`data: ${nDatos} JSON → public/data/`)

/* ---- Fixtures de paridad ---- */
exigir(fixturesOrigen)
const nFixtures = copiarDirectorio(fixturesOrigen, fixturesDestino, (n) => n.endsWith('.json'))
console.log(`fixtures: ${nFixtures} JSON → src/test/fixtures/`)

/* ---- Avatares pixel-art (3 estilos) ---- */
exigir(avataresOrigen)
const estilos = [
  { prefijo: /^avatar_(\d+)\.png$/, carpeta: 'color' },
  { prefijo: /^avatar_bw5_(\d+)\.png$/, carpeta: 'bw5' },
  { prefijo: /^avatar_bw1_(\d+)\.png$/, carpeta: 'bw1' },
]
const conteo = Object.fromEntries(estilos.map((e) => [e.carpeta, 0]))
for (const nombre of fs.readdirSync(avataresOrigen)) {
  for (const { prefijo, carpeta } of estilos) {
    const m = nombre.match(prefijo)
    if (!m) continue
    const destino = path.join(avataresDestino, carpeta)
    fs.mkdirSync(destino, { recursive: true })
    fs.copyFileSync(path.join(avataresOrigen, nombre), path.join(destino, `${m[1]}.png`))
    conteo[carpeta]++
  }
}
console.log(
  `avatars: color=${conteo.color} bw5=${conteo.bw5} bw1=${conteo.bw1} → public/avatars/`,
)
