#!/usr/bin/env node
/**
 * fetch-umamoe-search.mjs
 *
 * Busca **padres** (veteranos que la comunidad comparte) por requisitos de
 * chispas/factores en uma.moe y guarda sus atributos completos:
 *
 *   - el padre (main) y sus dos padres (abuelos), cada uno con sus factores;
 *   - estrellas por color (azul/rosa/verde/blanco) y sus sumas;
 *   - carreras ganadas, rango, rareza y afinidad contra la uma objetivo;
 *   - entrenador que lo presta y carta de apoyo que usó.
 *
 * Uso:
 *   node scripts/fetch-umamoe-search.mjs --url 'https://uma.moe/database?filters=<base64>'
 *   node scripts/fetch-umamoe-search.mjs --filters <base64> --player 100201
 *   node scripts/fetch-umamoe-search.mjs --pink 340:8-9 --blue 20:9-9 \
 *        --main-blue 20:3-3 --main-pink 340:3-3
 *   node scripts/fetch-umamoe-search.mjs --parent 1002 --pink 330:9-9
 *
 * Requisitos en formato `factor:estrella` o `factor:min-max`, separados por coma.
 * La uma objetivo (`--player`, sale de `t[0]` en el enlace) no filtra: solo mueve
 * la afinidad de cada resultado.
 *
 * `--parent` sí filtra: exige que el padre principal (el veterano que se presta,
 * o sea cada resultado) sea esa carta. Espera el id de carta de 6 cifras
 * (`100201`); el id de personaje de 4 cifras (`1002`) NO matchea nada, el
 * servidor compara contra el id de carta. Los ids salen del catálogo público
 * `character.json` (https://uma.moe/resources/current/). Para cubrir varias
 * skins del mismo personaje, pasalas separadas por coma (`100201,100202`).
 *
 * La API key se lee de UMA_MOE_API_KEY (o --key) y NUNCA se escribe en la salida.
 * Sin key el endpoint responde 403; con una key inválida, 401.
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const OUT_DIR = path.join(root, 'scripts', 'out');
const CACHE_DIR = path.join(OUT_DIR, '.cache');

const BASE = 'https://uma.moe';
const USER_AGENT = 'umafinidad/1.0 (script local)';
const RETRIES = 3;
const LIMITE_MAX = 100; // máximo que acepta la API por página
const PAUSA_ENTRE_PAGINAS = 120; // ms de cortesía entre páginas de una búsqueda larga

// Grupo del enlace -> parámetro de la API.
const GRUPOS = {
  p: { parametro: 'pink_sparks', etiqueta: 'rosa', bandera: '--pink' },
  b: { parametro: 'blue_sparks', etiqueta: 'azul', bandera: '--blue' },
  g: { parametro: 'green_sparks', etiqueta: 'verde', bandera: '--green' },
  w: { parametro: 'white_sparks', etiqueta: 'blanco', bandera: '--white' },
  mp: { parametro: 'main_parent_pink_sparks', etiqueta: 'rosa del padre', bandera: '--main-pink' },
  mb: { parametro: 'main_parent_blue_sparks', etiqueta: 'azul del padre', bandera: '--main-blue' },
  mg: { parametro: 'main_parent_green_sparks', etiqueta: 'verde del padre', bandera: '--main-green' },
  mw: { parametro: 'main_parent_white_sparks', etiqueta: 'blanco del padre', bandera: '--main-white' },
};

// Tipo de factor -> color de la chispa (los tipos 2/3/4 son blancas).
const COLOR_POR_TIPO = {
  0: 'azul', // stats
  1: 'rosa', // aptitudes
  2: 'blanco', // carreras
  3: 'blanco', // skills
  4: 'blanco', // escenarios
  5: 'verde', // habilidad única
  '-1': 'blanco', // otros
};

const USO = `
Busca padres (veteranos) por requisitos de chispas en uma.moe.

Entrada (una de estas):
  --url <url>             enlace de uma.moe/database con ?filters=<base64>
  --filters <base64>      el parámetro filters suelto
  --pink 340:8-9          requisitos sueltos (repetible, separados por coma)
  --blue 20:9             ...  también: --green, --white
  --main-pink 340:3-3     ...  también: --main-blue, --main-green, --main-white
  --player 100201         uma objetivo (solo afecta la afinidad)
  --parent 100201         el padre (cada resultado) debe ser esa carta (id de 6 cifras)

Opciones:
  --key <k>               API key (por defecto: variable UMA_MOE_API_KEY)
  --out <archivo>         JSON de salida (por defecto scripts/out/umamoe-<fecha>.json)
  --csv                   exporta además un CSV al lado del JSON
  --limit <n>             resultados por página (máx ${LIMITE_MAX}, por defecto ${LIMITE_MAX})
  --max <n>               tope de resultados a traer (por defecto: todos)
  --pages <n>             tope de páginas
  --top <n>               filas del resumen por consola (por defecto 10)
  --refresh               vuelve a bajar los catálogos públicos
  --dry-run               muestra la consulta sin llamar a la API
  --help                  esto
`.trim();

// ---------------------------------------------------------------- utilidades

function log(...args) {
  console.log(...args);
}

function morir(mensaje) {
  console.error(`\nError: ${mensaje}\n`);
  process.exit(1);
}

async function dormir(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

/** GET con reintentos; devuelve { status, texto }. */
async function pedir(url, headers = {}) {
  let ultimoError;
  for (let intento = 0; intento < RETRIES; intento++) {
    try {
      const res = await fetch(url, { headers: { 'User-Agent': USER_AGENT, ...headers } });
      const texto = await res.text();
      return { status: res.status, texto };
    } catch (error) {
      ultimoError = error;
      if (intento < RETRIES - 1) await dormir(800 * (intento + 1));
    }
  }
  throw ultimoError;
}

async function pedirJson(url, headers) {
  const { status, texto } = await pedir(url, headers);
  if (status !== 200) {
    const detalle = texto.slice(0, 200).replace(/\s+/g, ' ');
    throw new Error(`HTTP ${status} en ${url}${detalle ? ` :: ${detalle}` : ''}`);
  }
  return JSON.parse(texto);
}

function carpetaOut() {
  fs.mkdirSync(OUT_DIR, { recursive: true });
  return OUT_DIR;
}

// ------------------------------------------------------------ argumentos

function parsearArgs(argv) {
  const opciones = {
    grupos: {}, // 'p' -> [[factor, min, max], ...]
    objetivo: null,
    padreId: null,
    key: process.env.UMA_MOE_API_KEY ?? null,
    out: null,
    csv: false,
    limit: LIMITE_MAX,
    max: null,
    pages: null,
    top: 10,
    refresh: false,
    dryRun: false,
    ayuda: false,
  };

  const banderas = new Map();
  for (const [clave, grupo] of Object.entries(GRUPOS)) {
    banderas.set(grupo.bandera, { clave, grupo });
  }

  const tomarValor = (i, nombre) => {
    const valor = argv[i + 1];
    if (valor === undefined || valor.startsWith('--')) morir(`falta el valor de ${nombre}`);
    return valor;
  };

  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    switch (arg) {
      case '--help':
      case '-h':
        opciones.ayuda = true;
        break;
      case '--url': {
        const url = tomarValor(i++, arg);
        const filtros = new URL(url).searchParams.get('filters');
        if (!filtros) morir('la URL no trae el parámetro ?filters=');
        opciones.filtrosBase64 = filtros;
        break;
      }
      case '--filters':
        opciones.filtrosBase64 = tomarValor(i++, arg);
        break;
      case '--player':
        opciones.objetivo = String(tomarValor(i++, arg));
        break;
      case '--parent':
        opciones.padreId = String(tomarValor(i++, arg));
        break;
      case '--key':
        opciones.key = tomarValor(i++, arg);
        break;
      case '--out':
        opciones.out = tomarValor(i++, arg);
        break;
      case '--limit':
        opciones.limit = Math.min(LIMITE_MAX, Number(tomarValor(i++, arg)) || LIMITE_MAX);
        break;
      case '--max':
        opciones.max = Number(tomarValor(i++, arg)) || null;
        break;
      case '--pages':
        opciones.pages = Number(tomarValor(i++, arg)) || null;
        break;
      case '--top':
        opciones.top = Number(tomarValor(i++, arg)) || 0;
        break;
      case '--csv':
        opciones.csv = true;
        break;
      case '--refresh':
        opciones.refresh = true;
        break;
      case '--dry-run':
        opciones.dryRun = true;
        break;
      default: {
        const bandera = banderas.get(arg);
        if (bandera) {
          const valor = tomarValor(i++, arg);
          const previos = opciones.grupos[bandera.clave] ?? [];
          opciones.grupos[bandera.clave] = previos.concat(parsearRequisitos(valor, arg));
          break;
        }
        morir(`opción desconocida: ${arg} (probá --help)`);
      }
    }
  }
  return opciones;
}

/** '340:8-9,110:1' -> [[340,8,9],[110,1,1]] */
function parsearRequisitos(valor, bandera) {
  return valor.split(',').map((parte) => {
    const [factor, rango] = parte.trim().split(':');
    if (!factor || !rango) morir(`${bandera} espera factor:estrella o factor:min-max (recibí "${parte}")`);
    const idFactor = Number(factor);
    const [min, max = min] = rango.split('-').map(Number);
    if (!Number.isInteger(idFactor) || !Number.isInteger(min) || !Number.isInteger(max)) {
      morir(`${bandera}: valores no numéricos en "${parte}"`);
    }
    if (min < 1 || max > 9 || min > max) morir(`${bandera}: rango de estrellas inválido en "${parte}" (1-9)`);
    return [idFactor, min, max];
  });
}

/** El `filters` del enlace: JSON en base64 con los requisitos compactos. */
function decodificarFiltros(base64) {
  const normalizado = base64.replace(/-/g, '+').replace(/_/g, '/');
  let estado;
  try {
    estado = JSON.parse(Buffer.from(normalizado, 'base64').toString('utf8'));
  } catch {
    morir('no pude decodificar el base64 de `filters` (¿lo copiaste completo?)');
  }
  if (!estado || typeof estado !== 'object') morir('`filters` no decodifica a un objeto JSON');

  const grupos = {};
  for (const clave of Object.keys(GRUPOS)) {
    const valor = estado[clave];
    if (!Array.isArray(valor)) continue;
    grupos[clave] = valor
      .filter((r) => Array.isArray(r) && r.length >= 2)
      .map(([factor, min, max = min]) => [Number(factor), Number(min), Number(max)]);
  }
  const objetivo = Array.isArray(estado.t) && estado.t[0] != null ? String(estado.t[0]) : null;
  return { grupos, objetivo };
}

/** [[factor, min, max]] -> '3408,3409' */
function expandirRequisitos(requisitos) {
  const ids = [];
  for (const [factor, min, max] of requisitos) {
    for (let estrella = min; estrella <= max; estrella++) ids.push(factor * 10 + estrella);
  }
  return ids;
}

function construirConsulta({ grupos, objetivo, padreId }, pagina, limite) {
  const params = new URLSearchParams({
    search_type: 'inheritance',
    page: String(pagina),
    limit: String(limite),
  });
  for (const [clave, grupo] of Object.entries(GRUPOS)) {
    const requisitos = grupos[clave];
    if (!requisitos?.length) continue;
    params.set(grupo.parametro, expandirRequisitos(requisitos).join(','));
  }
  if (objetivo) params.set('player_chara_id', objetivo);
  // El padre principal es el veterano que se presta, o sea cada resultado.
  if (padreId) params.set('main_parent_id', padreId);
  return params;
}

// ------------------------------------------------------------- catálogos

async function catalogoJson(nombre, { refresh }) {
  const cache = path.join(CACHE_DIR, nombre);
  if (!refresh && fs.existsSync(cache)) {
    return JSON.parse(fs.readFileSync(cache, 'utf8'));
  }
  const datos = await pedirJson(`${BASE}/resources/current/${nombre}.json.gz`);
  fs.mkdirSync(CACHE_DIR, { recursive: true });
  fs.writeFileSync(cache, JSON.stringify(datos));
  return datos;
}

async function leerCatalogos({ refresh }) {
  log('Catálogos públicos de uma.moe...');
  const [manifest, characters, nombres, factors, supports] = await Promise.all([
    pedirJson(`${BASE}/resources/manifest.json`),
    catalogoJson('character', { refresh }),
    catalogoJson('character_names', { refresh }),
    catalogoJson('factors', { refresh }),
    catalogoJson('support-cards-db', { refresh }),
  ]);

  return {
    version: manifest.version,
    generado: manifest.generated_at,
    cartas: new Map(characters.map((c) => [String(c.id), c])),
    personajes: new Map(Object.entries(nombres)),
    factores: new Map(factors.map((f) => [String(f.id), f])),
    apoyos: new Map(supports.map((s) => [String(s.id), s])),
  };
}

// ------------------------------------------------------------- decodificar

/** id de carta -> nombre + versión (skin) usando los catálogos públicos. */
function nombrarCarta(id, cat) {
  if (id == null) return null;
  const cartaId = String(id);
  const base = cartaId.slice(0, 4);
  const skin = cartaId.slice(-2);
  const personaje = cat.personajes.get(base);
  const nombre = cat.cartas.get(cartaId)?.name ?? personaje?.name ?? `Carta ${cartaId}`;
  const version = personaje?.skins?.[skin] ?? null;
  return {
    cartaId: Number(cartaId),
    nombre,
    version,
    etiqueta: version ? `${nombre} (${version})` : nombre,
  };
}

/**
 * Los campos `*_factors` de la API vienen como número (203) o como lista
 * ([1001502, ...]) según el color; los normalizo a lista.
 */
function listaFactores(valor) {
  if (valor == null) return [];
  return Array.isArray(valor) ? valor : [valor];
}

/** [209, 3408] -> [{ factor: 20, estrellas: 9, nombre: 'Stamina', color: 'azul' }] */
function decodificarFactores(valor, cat) {
  return listaFactores(valor).map((crudo) => {
    const id = Number(crudo);
    const factorId = Math.floor(id / 10);
    const estrellas = id % 10;
    const factor = cat.factores.get(String(factorId));
    const tipo = factor?.type ?? null;
    return {
      id,
      factorId,
      estrellas,
      nombre: factor?.text ?? `Factor ${factorId}`,
      tipo,
      color: COLOR_POR_TIPO[String(tipo)] ?? 'blanco',
    };
  });
}

/**
 * Los ids de cada rama y los prefijos de sus factores no se llaman igual:
 * `main_parent_id` usa `main_*`, `parent_left_id` usa `left_*`, etc.
 */
const RAMAS = {
  padre: { id: 'main_parent_id', factores: 'main' },
  abueloIzquierdo: { id: 'parent_left_id', factores: 'left' },
  abueloDerecho: { id: 'parent_right_id', factores: 'right' },
};

function decodificarRama(inh, { id, factores }, cat) {
  return {
    ...nombrarCarta(inh[id], cat),
    // Un factor por color (el que aporta esa rama); en blanco, la lista completa.
    factores: {
      azul: decodificarFactores(inh[`${factores}_blue_factors`], cat),
      rosa: decodificarFactores(inh[`${factores}_pink_factors`], cat),
      verde: decodificarFactores(inh[`${factores}_green_factors`], cat),
      blanco: decodificarFactores(inh[`${factores}_white_factors`], cat),
    },
    carrerasGanadas: inh[`${factores}_win_saddles`]?.length ?? 0,
    blancas: inh[`${factores}_white_count`] ?? null,
  };
}

function decodificarItem(item, cat) {
  const inh = item.inheritance ?? {};
  const apoyo = item.support_card;
  const cartaApoyo = apoyo ? cat.apoyos.get(String(apoyo.support_card_id)) : null;

  return {
    entrenador: {
      nombre: item.trainer_name ?? null,
      cuentaId: item.account_id ?? null,
      seguidores: item.follower_num ?? null,
      prestamos: item.borrow_copy_count ?? null,
      vistas: item.borrow_view_count ?? null,
      actualizado: item.last_updated ?? null,
    },
    padre: decodificarRama(inh, RAMAS.padre, cat),
    abueloIzquierdo: decodificarRama(inh, RAMAS.abueloIzquierdo, cat),
    abueloDerecho: decodificarRama(inh, RAMAS.abueloDerecho, cat),
    estrellas: {
      azul: inh.blue_stars_sum ?? null,
      rosa: inh.pink_stars_sum ?? null,
      verde: inh.green_stars_sum ?? null,
      blanco: inh.white_stars_sum ?? null,
    },
    chispas: {
      azul: decodificarFactores(inh.blue_sparks, cat),
      rosa: decodificarFactores(inh.pink_sparks, cat),
      verde: decodificarFactores(inh.green_sparks, cat),
      blanco: decodificarFactores(inh.white_sparks, cat),
    },
    carreras: {
      ganadas: inh.win_count ?? null,
      blancas: inh.white_count ?? null,
      resultados: inh.race_results?.length ?? 0,
    },
    afinidad: inh.affinity_score ?? null,
    rango: inh.parent_rank ?? null,
    rareza: inh.parent_rarity ?? null,
    herenciaId: inh.inheritance_id ?? null,
    cartaApoyo: cartaApoyo
      ? {
          id: Number(apoyo.support_card_id),
          nombre: cartaApoyo.card_full_name ?? cartaApoyo.name ?? null,
          limites: apoyo.limit_break_count ?? null,
          experiencia: apoyo.experience ?? null,
        }
      : null,
  };
}

// ----------------------------------------------------------------- buscar

/**
 * El campo `total` de la API es un string: `"16"`, o `"over 10000"` cuando hay
 * más resultados de los que el servidor cuenta. Paginar calculándolo daba `NaN`
 * y dejaba la búsqueda en la primera página en silencio; el valor que sí viene
 * exacto es `total_pages`, así que `total` queda solo para mostrar.
 */
function interpretarTotal(crudo) {
  const texto = String(crudo ?? '').trim();
  if (/^\d+$/.test(texto)) return { valor: Number(texto), exacto: true };
  const exceso = /^over\s+(\d+)$/i.exec(texto);
  if (exceso) return { valor: Number(exceso[1]), exacto: false };
  return { valor: 0, exacto: false };
}

/** 16 -> '16'; inexacto -> 'más de 10000' */
function mostrarTotal(total) {
  return total.exacto ? String(total.valor) : `más de ${total.valor}`;
}

async function buscar(opciones, cat) {
  const { grupos, objetivo } = opciones.estado;
  const headers = { 'x-api-key': opciones.key };
  const filtros = { grupos, objetivo, padreId: opciones.padreId };
  const primeros = await pedir(`${BASE}/search/query?${construirConsulta(filtros, 0, opciones.limit)}`, headers);

  if (primeros.status === 403) {
    morir('403: falta la API key (creala en uma.moe → Ajustes → API keys y exportala como UMA_MOE_API_KEY)');
  }
  if (primeros.status === 401) morir('401: la API key fue rechazada (¿regenerada o mal copiada?)');
  if (primeros.status !== 200) {
    morir(`HTTP ${primeros.status} en /search/query :: ${primeros.texto.slice(0, 200)}`);
  }

  const primera = JSON.parse(primeros.texto);
  const total = interpretarTotal(primera.total);
  const limite = opciones.limit;
  const paginasApi = Number(primera.total_pages);
  let paginas = Number.isInteger(paginasApi) && paginasApi > 0
    ? paginasApi
    : Math.max(1, Math.ceil(total.valor / limite));
  if (opciones.pages) paginas = Math.min(paginas, opciones.pages);
  if (opciones.max) paginas = Math.min(paginas, Math.ceil(opciones.max / limite));

  if (!total.exacto && !opciones.pages && !opciones.max) {
    log(`  aviso: hay ${mostrarTotal(total)} resultados (${paginas} páginas de ${limite}).`);
    log('  Es una búsqueda amplia; acotala con --max <n> o --pages <n> si no querés traerla entera.');
  }

  const items = primera.items ?? [];
  for (let pagina = 1; pagina < paginas; pagina++) {
    await dormir(PAUSA_ENTRE_PAGINAS);
    const res = await pedir(`${BASE}/search/query?${construirConsulta(filtros, pagina, limite)}`, headers);
    if (res.status !== 200) {
      log(`  aviso: página ${pagina} devolvió ${res.status}, corto acá`);
      break;
    }
    const nuevos = JSON.parse(res.texto).items ?? [];
    if (!nuevos.length) {
      log(`  página ${pagina + 1}: vacía, corto acá`);
      break;
    }
    items.push(...nuevos);
    log(`  página ${pagina + 1}/${paginas} (${items.length}/${mostrarTotal(total)})`);
    // Una página corta es la última: no hace falta pedir las que siguen.
    if (nuevos.length < limite) break;
  }

  return {
    total,
    paginas,
    items: opciones.max ? items.slice(0, opciones.max) : items,
  };
}

// ------------------------------------------------------------------ salida

function resumen(registros, meta, opciones) {
  log(`\n${mostrarTotal(meta.total)} padre(s) en ${meta.paginas} página(s) · datos de uma.moe ${meta.version}`);
  const filas = registros.slice(0, opciones.top || registros.length);
  for (const [i, r] of filas.entries()) {
    const e = r.estrellas;
    log(
      `${String(i + 1).padStart(2)}. ${r.padre.etiqueta}  ` +
        `azul ${e.azul}★ rosa ${e.rosa}★ verde ${e.verde}★ blanco ${e.blanco}★  ` +
        `afinidad ${r.afinidad}  ` +
        `← ${r.abueloIzquierdo.etiqueta} + ${r.abueloDerecho.etiqueta}  ` +
        `(${r.entrenador.nombre ?? 'sin nombre'})`,
    );
  }
  if (filas.length < registros.length) log(`... y ${registros.length - filas.length} más`);
}

function aCsv(registros) {
  const escapar = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
  const cabecera = [
    'padre',
    'version',
    'carta_id',
    'estrellas_azul',
    'estrellas_rosa',
    'estrellas_verde',
    'estrellas_blanco',
    'abuelo_izquierdo',
    'abuelo_derecho',
    'afinidad',
    'rango',
    'carreras_ganadas',
    'carta_apoyo',
    'entrenador',
    'actualizado',
  ];
  const filas = registros.map((r) =>
    [
      r.padre.nombre,
      r.padre.version,
      r.padre.cartaId,
      r.estrellas.azul,
      r.estrellas.rosa,
      r.estrellas.verde,
      r.estrellas.blanco,
      r.abueloIzquierdo.etiqueta,
      r.abueloDerecho.etiqueta,
      r.afinidad,
      r.rango,
      r.carreras.ganadas,
      r.cartaApoyo?.nombre,
      r.entrenador.nombre,
      r.entrenador.actualizado,
    ]
      .map(escapar)
      .join(','),
  );
  return [cabecera.join(','), ...filas].join('\n') + '\n';
}

function marcaDeTiempo(fecha = new Date()) {
  // Fecha local sin depender de Intl: 2026-10-12-1542
  const p = (n) => String(n).padStart(2, '0');
  return `${fecha.getFullYear()}-${p(fecha.getMonth() + 1)}-${p(fecha.getDate())}-${p(fecha.getHours())}${p(fecha.getMinutes())}`;
}

// -------------------------------------------------------------------- main

async function main() {
  const opciones = parsearArgs(process.argv.slice(2));
  if (opciones.ayuda) {
    log(USO);
    return;
  }

  const estado = opciones.filtrosBase64 ? decodificarFiltros(opciones.filtrosBase64) : { grupos: opciones.grupos, objetivo: opciones.objetivo };
  if (opciones.objetivo) estado.objetivo = opciones.objetivo;
  opciones.estado = estado;

  const consulta = construirConsulta({ ...estado, padreId: opciones.padreId }, 0, opciones.limit);
  if (opciones.dryRun) {
    log(`GET ${BASE}/search/query?${consulta}`);
    return;
  }
  if (!Object.keys(estado.grupos).length && !estado.objetivo && !opciones.padreId) {
    morir('no hay requisitos: pasá --url/--filters, algún --pink/--blue/... o --parent (--help)');
  }
  if (!opciones.key) {
    morir('falta la API key: exportá UMA_MOE_API_KEY=... o pasá --key (uma.moe → Ajustes → API keys)');
  }

  log(`GET /search/query?${consulta}`);
  const cat = await leerCatalogos(opciones);
  const meta = await buscar(opciones, cat);
  const registros = meta.items.map((item) => decodificarItem(item, cat));

  resumen(registros, { ...meta, version: cat.version }, opciones);

  const salida = opciones.out ?? path.join(carpetaOut(), `umamoe-${marcaDeTiempo()}.json`);
  fs.mkdirSync(path.dirname(salida), { recursive: true });
  fs.writeFileSync(
    salida,
    JSON.stringify(
      {
        fuente: {
          sitio: BASE,
          endpoint: '/search/query?search_type=inheritance',
          versionDatos: cat.version,
          generado: cat.generado,
          consultado: new Date().toISOString(),
        },
        consulta: {
          parametros: Object.fromEntries(consulta),
          base64: opciones.filtrosBase64 ?? null,
          requisitos: estado.grupos,
          objetivo: estado.objetivo,
          padreId: opciones.padreId,
        },
        total: meta.total.valor,
        totalExacto: meta.total.exacto,
        paginas: meta.paginas,
        padres: registros,
      },
      null,
      2,
    ) + '\n',
  );
  log(`\nJSON: ${salida}`);

  if (opciones.csv) {
    const csv = salida.replace(/\.json$/, '.csv');
    fs.writeFileSync(csv, aCsv(registros));
    log(`CSV:  ${csv}`);
  }
}

main().catch((error) => morir(error.message));
