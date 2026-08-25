/* Genera los íconos launcher legacy (pre-Android 8) a partir del mismo
   corazón usado por la PWA. Sin dependencias: escribe PNGs crudos con zlib.
   - ic_launcher.png        → cuadrado con esquinas redondeadas
   - ic_launcher_round.png  → círculo completo
   El foreground adaptativo (API 26+) es el maskable-512 sin cambios. */
import { deflateSync } from 'node:zlib'
import { mkdirSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const raiz = join(dirname(fileURLToPath(import.meta.url)), '..')
const res = join(raiz, 'app', 'src', 'main', 'res')

function trozo(tipo, datos) {
  const len = Buffer.alloc(4)
  len.writeUInt32BE(datos.length)
  const cuerpo = Buffer.concat([Buffer.from(tipo, 'ascii'), datos])
  const crc = Buffer.alloc(4)
  const t = new Int32Array(256)
  for (let n = 0; n < 256; n++) {
    let c = n
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1
    t[n] = c
  }
  let acc = -1
  for (const b of cuerpo) acc = (acc >>> 8) ^ t[(acc ^ b) & 255]
  crc.writeUInt32BE((acc ^ -1) >>> 0)
  return Buffer.concat([len, cuerpo, crc])
}

function png(ancho, alto, pixeles) {
  const firma = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(ancho, 0)
  ihdr.writeUInt32BE(alto, 4)
  ihdr[8] = 8
  ihdr[9] = 6
  const crudos = Buffer.alloc((ancho * 4 + 1) * alto)
  for (let y = 0; y < alto; y++) {
    const fila = y * (ancho * 4 + 1)
    crudos[fila] = 0
    pixeles.copy(crudos, fila + 1, y * ancho * 4, (y + 1) * ancho * 4)
  }
  return Buffer.concat([
    firma,
    trozo('IHDR', ihdr),
    trozo('IDAT', deflateSync(crudos)),
    trozo('IEND', Buffer.alloc(0)),
  ])
}

const FONDO_SUP = [36, 40, 49]
const FONDO_INF = [18, 20, 24]
const ACENTO = [240, 140, 58]

function dentroCorazon(x, y) {
  const q = x * x + y * y - 1
  return q * q * q - x * x * y * y * y < 0
}

function limitesCorazon() {
  let minX = Infinity
  let maxX = -Infinity
  let minY = Infinity
  let maxY = -Infinity
  for (let y = -2; y <= 2; y += 0.002) {
    for (let x = -2; x <= 2; x += 0.002) {
      if (dentroCorazon(x, y)) {
        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (y < minY) minY = y
        if (y > maxY) maxY = y
      }
    }
  }
  return { minX, maxX, minY, maxY }
}

const LIMITES = limitesCorazon()

function dentroCajaRedondeada(px, py, centro, lado, radio) {
  const qx = Math.abs(px) - lado / 2 + radio
  const qy = Math.abs(py) - lado / 2 + radio
  const dx = Math.max(qx, 0)
  const dy = Math.max(qy, 0)
  return Math.min(Math.max(qx, qy), 0) + Math.hypot(dx, dy) - radio < 0
}

function renderizar(tamano, { mascara }) {
  const muestreo = 4
  const paso = 1 / muestreo
  const centro = tamano / 2
  const anchoH = LIMITES.maxX - LIMITES.minX
  const altoH = LIMITES.maxY - LIMITES.minY
  const s = (tamano * (mascara === 'circulo' ? 0.5 : 0.56)) / Math.max(anchoH, altoH)
  const cx = centro - ((LIMITES.minX + LIMITES.maxX) / 2) * s
  const cy = centro - ((LIMITES.minY + LIMITES.maxY) / 2) * s
  const rgba = Buffer.alloc(tamano * tamano * 4)

  for (let py = 0; py < tamano; py++) {
    for (let px = 0; px < tamano; px++) {
      let fondo = 0
      let corazon = 0
      for (let sy = 0; sy < muestreo; sy++) {
        for (let sx = 0; sx < muestreo; sx++) {
          const X = px + (sx + 0.5) * paso
          const Y = py + (sy + 0.5) * paso
          let visible = true
          if (mascara === 'cuadrado') {
            visible = dentroCajaRedondeada(X - centro, Y - centro, centro, tamano, tamano * 0.16)
          } else if (mascara === 'circulo') {
            visible = Math.hypot(X - centro, Y - centro) <= centro
          }
          if (!visible) continue
          fondo++
          const hx = (X - cx) / s
          const hy = -(Y - cy) / s
          if (dentroCorazon(hx, hy)) corazon++
        }
      }

      const t = py / tamano
      const base = [
        FONDO_SUP[0] + (FONDO_INF[0] - FONDO_SUP[0]) * t,
        FONDO_SUP[1] + (FONDO_INF[1] - FONDO_SUP[1]) * t,
        FONDO_SUP[2] + (FONDO_INF[2] - FONDO_SUP[2]) * t,
      ]
      const mezcla = fondo / (muestreo * muestreo)
      const pesoCorazon = corazon / (muestreo * muestreo)

      const i = (py * tamano + px) * 4
      rgba[i] = Math.round(base[0] + (ACENTO[0] - base[0]) * pesoCorazon)
      rgba[i + 1] = Math.round(base[1] + (ACENTO[1] - base[1]) * pesoCorazon)
      rgba[i + 2] = Math.round(base[2] + (ACENTO[2] - base[2]) * pesoCorazon)
      rgba[i + 3] = Math.round(mezcla * 255)
    }
  }
  return png(tamano, tamano, rgba)
}

const DENSIDADES = [
  ['mipmap-mdpi', 48],
  ['mipmap-hdpi', 72],
  ['mipmap-xhdpi', 96],
  ['mipmap-xxhdpi', 144],
  ['mipmap-xxxhdpi', 192],
]

for (const [dir, tam] of DENSIDADES) {
  const destino = join(res, dir)
  mkdirSync(destino, { recursive: true })
  writeFileSync(join(destino, 'ic_launcher.png'), renderizar(tam, { mascara: 'cuadrado' }))
  writeFileSync(join(destino, 'ic_launcher_round.png'), renderizar(tam, { mascara: 'circulo' }))
  console.log(`ok ${dir} (${tam}x${tam})`)
}
