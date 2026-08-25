/* Genera fixtures de paridad ejecutando la lógica ORIGINAL de la PWA
   (affinity.js + herencia.js) sobre los mismos datos datamined embebidos en
   el app. Los tests JUnit de Kotlin verifican que el porte dé idéntico.

   Uso: node scripts/generate-fixtures.mjs <ruta-al-repo-pwa>
   Ej.: node scripts/generate-fixtures.mjs ../pwauma/MaximillionSnyder.github.io */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const pwaRoot = path.resolve(process.argv[2] ?? '../MaximillionSnyder.github.io');

const affinityUrl = pathToFileURL(path.join(pwaRoot, 'src', 'affinity.js')).href;
const herenciaUrl = pathToFileURL(path.join(pwaRoot, 'src', 'herencia.js')).href;

const { crearModelo } = await import(affinityUrl);
const H = await import(herenciaUrl);

const assets = path.join(root, 'app', 'src', 'main', 'assets', 'data');
const characters = JSON.parse(fs.readFileSync(path.join(assets, 'characters.json'), 'utf8'));
const relations = JSON.parse(fs.readFileSync(path.join(assets, 'succession_relation.json'), 'utf8'));
const members = JSON.parse(fs.readFileSync(path.join(assets, 'succession_relation_member.json'), 'utf8'));

const modelo = crearModelo({ characters, relations, members });
const chars = characters.filter((c) => c.playable && c.active);
const ids = chars.map((c) => c.char_id);
const m = ids.length;
console.log(`Personajes activos/jugables: ${m}`);

const outDir = path.join(root, 'app', 'src', 'test', 'resources', 'fixtures');
fs.mkdirSync(outDir, { recursive: true });

const guardar = (nombre, valor) => {
  fs.writeFileSync(path.join(outDir, nombre), `${JSON.stringify(valor)}\n`);
  console.log(`ok ${nombre}`);
};

const resumenChar = (c) => ({ char_id: c.char_id, en_name: c.en_name });

/* 1. Puntajes par: cada personaje contra sus 3 siguientes (determinista,
      cubre casi toda la matriz sin explotar de tamaño). */
const pares = [];
for (let i = 0; i < m; i++)
  for (let j = i + 1; j < Math.min(i + 4, m); j++)
    pares.push([ids[i], ids[j], modelo.puntajePar(ids[i], ids[j])]);
guardar('pares.json', pares);

/* 2. Tríos con salto 7 para cubrir combinaciones variadas. */
const trios = [];
for (let i = 0; i + 14 < m; i += 7)
  trios.push([ids[i], ids[i + 7], ids[i + 14], modelo.puntajeTrio(ids[i], ids[i + 7], ids[i + 14])]);
guardar('trios.json', trios);

/* 3. Grupos compartidos por par (estructura completa tipo+puntos). */
const gruposCompartidos = {};
for (const [a, b] of [[1024, 1026], [1001, 1030], [1050, 1061], [1071, 1080]]) {
  if (modelo.porId.has(a) && modelo.porId.has(b)) {
    gruposCompartidos[`${a}-${b}`] = modelo.gruposCompartidos([a, b]);
  }
}
guardar('grupos_compartidos.json', gruposCompartidos);

/* 4. Rangos individuales y totales para valores límite. */
const valores = [0, 3, 4, 9, 10, 19, 20, 50, 51, 150, 151, 500];
const rangos = Object.fromEntries(valores.map((v) => [
  String(v),
  { par: modelo.rango(v), total: modelo.rangoTotal(v) },
]));
guardar('rangos.json', rangos);

/* 5. Grupos de un personaje concreto (con nombres de miembros). */
const muestraChars = ids.slice(0, 5);
const gruposChar = Object.fromEntries(muestraChars.map((id) => [String(id), modelo.gruposDeChar(id)]));
guardar('grupos_char.json', gruposChar);

/* 6. Todos los grupos (orden completo). */
guardar('todos_grupos.json', modelo.todosLosGrupos());

/* 7. Top linajes completos (el pipeline pesado completo). */
const topLinajes = modelo.topLinajes(10).map((l) => ({
  hijo: resumenChar(l.hijo),
  padre: resumenChar(l.padre),
  madre: resumenChar(l.madre),
  abuelos: l.abuelos.map((rama) => rama.map(resumenChar)),
  puntos: l.puntos,
}));
guardar('top_linajes.json', topLinajes);

/* 8. Herencia: reglas de colocación, slotPara y vínculos. */
const candidatosHerencia = [...new Set([...ids.slice(0, 8), ...ids.slice(-3)])];
const seleccionesBase = [
  [null, null, null, null, null, null, null],
  [ids[0], ids[1], ids[2], null, null, null, null],
  [ids[0], ids[1], ids[2], ids[0], ids[3], ids[4], ids[5]], /* hijo como abuelo */
];
const casosPuedeIrEn = [];
for (let s = 0; s < seleccionesBase.length; s++) {
  const sel = [...seleccionesBase[s]];
  for (let slot = 0; slot < 7; slot++) {
    for (const id of candidatosHerencia) {
      casosPuedeIrEn.push([s, slot, id, H.puedeIrEn(sel, slot, id)]);
    }
  }
}
guardar('puede_ir_en.json', casosPuedeIrEn);

const casosSlotPara = [];
for (let s = 0; s < seleccionesBase.length; s++) {
  const sel = [...seleccionesBase[s]];
  for (const id of candidatosHerencia) casosSlotPara.push([s, id, H.slotPara(sel, id)]);
}
guardar('slot_para.json', casosSlotPara);

const vinculosPorSeleccion = seleccionesBase.map((sel) =>
  H.vinculos(H.armarArbol([...sel])).map((v) => ({ tipo: v.tipo, ids: v.ids, esCorredora: !!v.esCorredora })),
);
guardar('vinculos.json', vinculosPorSeleccion);

console.log('Fixtures generados en app/src/test/resources/fixtures/');
