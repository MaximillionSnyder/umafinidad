import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const OUT_DIR = path.join(root, 'app', 'src', 'main', 'assets', 'data');
const MANIFEST_URL = 'https://gametora.com/data/manifests/umamusume.json';
const DATA_BASE = 'https://gametora.com/data/umamusume';

const RETRIES = 3;
const USER_AGENT = 'uma-pedigree/1.0';

async function fetchJson(url) {
  let lastError;
  for (let attempt = 0; attempt < RETRIES; attempt++) {
    try {
      const res = await fetch(url, { headers: { 'User-Agent': USER_AGENT } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return await res.json();
    } catch (error) {
      lastError = error;
      if (attempt < RETRIES - 1) await new Promise((r) => setTimeout(r, 800 * (attempt + 1)));
    }
  }
  throw lastError;
}

function resolveUrl(manifest, key) {
  const hash = manifest[key];
  if (!hash) throw new Error(`Clave ausente en el manifiesto: ${key}`);
  return `${DATA_BASE}/${key}.${hash}.json`;
}

async function main() {
  const dryRun = process.argv.includes('--dry-run');
  /* Solo refrescar aptitudes.json (deja intactos los otros tres JSON). */
  const soloAptitudes = process.argv.includes('--solo-aptitudes');

  console.log('Obteniendo manifiesto de GameTora...');
  const manifest = await fetchJson(MANIFEST_URL);

  // Las tablas base (sin prefijo de idioma) son las completas y solo contienen
  // números; las versiones por idioma (en/, ko/, zh-tw/) están recortadas.
  // Los nombres localizados ya vienen en characters.json.
  const keys = {
    characters: 'characters',
    relation: 'db-files/succession_relation',
    member: 'db-files/succession_relation_member',
    cards: 'character-cards',
  };
  if (soloAptitudes) {
    delete keys.characters;
    delete keys.relation;
    delete keys.member;
  }

  const files = {};
  for (const [name, key] of Object.entries(keys)) {
    const url = resolveUrl(manifest, key);
    console.log(`Descargando ${name} (${key})...`);
    files[name] = await fetchJson(url);
  }

  if (!soloAptitudes) {
    const chars = files.characters.filter((c) => c.playable && c.active);
    const playableIds = new Set(chars.map((c) => c.char_id));
    const members = files.member.filter((m) => playableIds.has(m.chara_id));
    const usedTypes = new Set(members.map((m) => m.relation_type));
    const relations = files.relation.filter((r) => usedTypes.has(r.relation_type));

    const gruposPorPuntos = new Map();
    for (const r of relations) {
      gruposPorPuntos.set(r.relation_point, (gruposPorPuntos.get(r.relation_point) ?? 0) + 1);
    }

    console.log(`Personajes jugables: ${chars.length}`);
    console.log(`Grupos de afinidad: ${relations.length} (${members.length} membresías)`);
    console.log(
      'Grupos por puntos:',
      [...gruposPorPuntos.entries()].sort((a, b) => b[0] - a[0]).map(([p, n]) => `${n}x${p}pt`).join(', '),
    );
    files._salida = { chars, relations, members };
  }

  /* Aptitudes: solo la carta base por personaje (version: null); los
     trajes alternativos pueden variarlas. Orden fijo de las 10 letras:
     [Turf, Dirt, Corta, Milla, Media, Larga, 逃げ, 先行, 差し, 追込]. */
  const LETRAS = new Set(['A', 'B', 'C', 'D', 'E', 'F', 'G']);
  const base = new Map();
  let trajes = 0;
  for (const card of files.cards) {
    if (card.version != null) {
      trajes++;
      continue;
    }
    const previa = base.get(card.char_id);
    if (!previa || card.card_id < previa.card_id) base.set(card.char_id, card);
  }
  const aptitudes = {};
  let invalidas = 0;
  for (const [charId, card] of [...base.entries()].sort((a, b) => a[0] - b[0])) {
    const apt = card.aptitude;
    const ok = Array.isArray(apt) && apt.length === 10 && apt.every((l) => LETRAS.has(l));
    if (!ok) {
      invalidas++;
      console.warn(`  ! char_id ${charId}: aptitud inválida ${JSON.stringify(apt)}`);
      continue;
    }
    aptitudes[String(charId)] = apt;
  }
  console.log(`Cartas: ${files.cards.length} (base: ${base.size}, trajes: ${trajes})`);
  console.log(`Aptitudes extraídas: ${Object.keys(aptitudes).length} (${invalidas} inválidas)`);

  if (dryRun) return;

  fs.mkdirSync(OUT_DIR, { recursive: true });
  if (!soloAptitudes) {
    const output = (value) => `${JSON.stringify(value, null, 2)}\n`;
    fs.writeFileSync(path.join(OUT_DIR, 'characters.json'), output(files.characters));
    fs.writeFileSync(path.join(OUT_DIR, 'succession_relation.json'), output(files._salida.relations));
    fs.writeFileSync(path.join(OUT_DIR, 'succession_relation_member.json'), output(files._salida.members));
  }
  /* Una línea por personaje: legible y diff-friendly. */
  const cuerpo = Object.entries(aptitudes)
    .map(([id, apt]) => `  ${JSON.stringify(id)}: ${JSON.stringify(apt)}`)
    .join(',\n');
  fs.writeFileSync(path.join(OUT_DIR, 'aptitudes.json'), `{\n${cuerpo}\n}\n`);
  console.log(`Guardado en ${path.relative(root, OUT_DIR)}/`);
}

main().catch((error) => {
  console.error('Error fatal:', error);
  process.exitCode = 1;
});
