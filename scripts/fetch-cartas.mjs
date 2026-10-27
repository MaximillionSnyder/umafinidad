import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
/* Escribe directo en los assets de la app: no hay copia intermedia. */
const OUT_DIR = path.join(root, 'app', 'src', 'main', 'assets', 'data');
const MANIFEST_URL = 'https://gametora.com/data/manifests/umamusume.json';
const DATA_BASE = 'https://gametora.com/data/umamusume';

const RETRIES = 3;
const USER_AGENT = 'uma-pedigree/1.0';

const KEYS = {
  skills: 'skills',
  characterCards: 'character-cards',
  supportCards: 'support-cards',
  scenarios: 'scenarios',
};

const LOCALES = { en: 'en', ja: 'ja', ko: 'ko', tw: 'zh_tw' };
const KIND_ORDER = ['unique', 'innate', 'awakening', 'evolution'];

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

function toIntSet(values) {
  const set = new Set();
  for (const value of values ?? []) set.add(Number(value));
  return set;
}

function sameSet(a, b) {
  if (a.size !== b.size) return false;
  for (const value of a) if (!b.has(value)) return false;
  return true;
}

function buildCardIndex(cards) {
  const index = new Map();
  for (const card of cards) {
    const skills = new Map();
    const add = (ids, kind) => {
      for (const raw of ids ?? []) {
        const id = Number(raw);
        const kinds = skills.get(id) ?? new Set();
        kinds.add(kind);
        skills.set(id, kinds);
      }
    };
    add(card.skills_unique, 'unique');
    add(card.skills_innate, 'innate');
    add(card.skills_awakening, 'awakening');
    for (const evo of card.skills_evo ?? []) {
      if (evo && evo.new != null) add([evo.new], 'evolution');
    }
    index.set(Number(card.card_id), { skills, events: toIntSet(card.skills_event) });
  }
  return index;
}

function buildSupportIndex(cards) {
  const index = new Map();
  for (const card of cards) {
    index.set(Number(card.support_id), {
      events: toIntSet(card.event_skills),
      hints: toIntSet(card.hints?.hint_skills),
    });
  }
  return index;
}

function computeSources(skill, cardIndex, supportIndex) {
  const id = Number(skill.id);
  const characterCards = [];
  const characterEvents = [];
  for (const [cardId, entry] of cardIndex) {
    const kinds = entry.skills.get(id);
    if (kinds) characterCards.push({ cardId, kinds: KIND_ORDER.filter((kind) => kinds.has(kind)) });
    if (entry.events.has(id)) characterEvents.push(cardId);
  }
  const supportHints = [];
  const supportEvents = [];
  for (const [supportId, entry] of supportIndex) {
    if (entry.hints.has(id)) supportHints.push(supportId);
    if (entry.events.has(id)) supportEvents.push(supportId);
  }
  const asc = (a, b) => a - b;
  characterCards.sort((a, b) => a.cardId - b.cardId);
  characterEvents.sort(asc);
  supportHints.sort(asc);
  supportEvents.sort(asc);
  const scenarioEvents = [...toIntSet(skill.sce_e)].sort(asc);
  return { characterCards, characterEvents, supportHints, supportEvents, scenarioEvents };
}

function verifySources(skill, sources, stats, mismatches) {
  const checks = [
    ['char', toIntSet(skill.char), new Set(sources.characterCards.map((entry) => entry.cardId))],
    ['char_e', toIntSet(skill.char_e), new Set(sources.characterEvents)],
    ['sup_e', toIntSet((skill.sup_e ?? []).flat()), new Set(sources.supportEvents)],
    ['sup_hint', toIntSet((skill.sup_hint ?? []).flat()), new Set(sources.supportHints)],
    ['sce_e', toIntSet(skill.sce_e), new Set(sources.scenarioEvents)],
  ];
  for (const [field, expected, got] of checks) {
    stats[field] += expected.size;
    if (!sameSet(expected, got)) {
      mismatches.push({ id: skill.id, field, expected: [...expected].sort((a, b) => a - b), got: [...got].sort((a, b) => a - b) });
    }
  }
}

function namesFor(skill) {
  const name = {};
  const desc = {};
  for (const [locale, key] of Object.entries(LOCALES)) {
    const merged = { ...skill, ...(skill.loc?.[key] ?? {}) };
    if (locale === 'en') {
      name.en = merged.name_en ?? merged.enname ?? null;
      desc.en = merged.desc_en ?? merged.endesc ?? merged.jpdesc ?? null;
    } else if (locale === 'ja') {
      name.ja = merged.jpname ?? null;
      desc.ja = merged.jpdesc ?? null;
    } else {
      name[locale] = merged[`name_${locale}`] ?? merged.jpname ?? null;
      desc[locale] = merged[`desc_${locale}`] ?? merged.jpdesc ?? null;
    }
  }
  return { name, desc };
}

/* Idiomas en los que la skill todavía no ha salido, con los añadidos a mano. */
function sinEn(skill) {
  const lista = new Set(skill.unreleased ?? []);
  if (SIN_EN_A_MANO.has(Number(skill.id))) lista.add('en');
  return [...lista];
}

/* Skills que GameTora todavía no marca como no publicadas fuera de Japón. Su
   propia tabla de cartas sí sabe que la carta de origen no ha salido del
   servidor global, pero el campo `unreleased` de la skill va por detrás. Se
   mantiene aquí la lista, con el motivo, y se aplica al generar el volcado. */
const SIN_EN_A_MANO = new Map([
  [200014, '右回りの鬼 (Right-Handed Demon): solo está en el servidor japonés'],
]);

function outputSkill(skill, sources) {
  const { name, desc } = namesFor(skill);
  return {
    id: Number(skill.id),
    iconId: skill.iconid ?? null,
    rarity: skill.rarity ?? null,
    types: skill.type ?? [],
    cost: skill.cost ?? null,
    activation: skill.activation ?? null,
    name,
    desc,
    unreleased: sinEn(skill),
    conditions: skill.condition_groups ?? [],
    geneVersion: skill.gene_version ?? null,
    evolution: {
      evo: skill.evo ?? null,
      preEvo: skill.pre_evo ?? null,
      conditions: skill.evo_cond ?? null,
    },
    sources,
  };
}

function outputCharacterCard(card) {
  return {
    cardId: Number(card.card_id),
    charId: Number(card.char_id),
    rarity: card.rarity ?? null,
    obtained: card.obtained ?? null,
    urlName: card.url_name ?? null,
    name: {
      en: card.name_en ?? null,
      ja: card.name_jp ?? null,
      ko: card.name_ko ?? null,
      tw: card.name_tw ?? null,
    },
    title: {
      en: card.title_en_gl ?? card.title ?? null,
      ja: card.title_jp ?? card.title ?? null,
      ko: card.title_ko ?? card.title_jp ?? card.title ?? null,
      tw: card.title_tw ?? card.title_jp ?? card.title ?? null,
    },
    image: `https://gametora.com/images/umamusume/characters/thumb/chara_stand_${card.char_id}_${card.card_id}.png`,
  };
}

function outputSupportCard(card) {
  return {
    supportId: Number(card.support_id),
    charId: Number(card.char_id),
    type: card.type ?? null,
    rarity: card.rarity ?? null,
    obtained: card.obtained ?? null,
    urlName: card.url_name ?? null,
    name: {
      en: card.name_en_gl ?? card.char_name ?? null,
      ja: card.name_jp ?? null,
      ko: card.name_ko ?? null,
      tw: card.name_tw ?? null,
    },
    title: {
      en: card.title_en ?? null,
      ja: card.title_ja ?? null,
      ko: card.title_ko ?? null,
      tw: card.title_zh_tw ?? null,
    },
    image: `https://gametora.com/images/umamusume/supports/support_card_s_${card.support_id}.png`,
  };
}

function outputScenario(scenario) {
  return {
    id: Number(scenario.id),
    name: {
      en: scenario.name_en ?? scenario.name_en_old ?? scenario.name_en_full ?? null,
      ja: scenario.name_ja ?? null,
      ko: scenario.name_ko ?? null,
      tw: scenario.name_zh_tw ?? null,
    },
    urlName: scenario.url_name ?? null,
  };
}

function sortById(entries) {
  return Object.fromEntries([...entries].sort((a, b) => Number(a[0]) - Number(b[0])));
}

async function main() {
  const dryRun = process.argv.includes('--dry-run');

  console.log('Obteniendo manifiesto de GameTora...');
  const manifest = await fetchJson(MANIFEST_URL);

  const files = {};
  for (const [name, key] of Object.entries(KEYS)) {
    const url = resolveUrl(manifest, key);
    console.log(`Descargando ${name} (${key})...`);
    files[name] = await fetchJson(url);
  }

  const cardIndex = buildCardIndex(files.characterCards);
  const supportIndex = buildSupportIndex(files.supportCards);
  const scenarioIds = new Set(files.scenarios.map((scenario) => Number(scenario.id)));

  const stats = { char: 0, char_e: 0, sup_e: 0, sup_hint: 0, sce_e: 0 };
  const mismatches = [];
  const missingRefs = [];
  const kindCounts = new Map();
  const usedCards = new Set();
  const usedSupports = new Set();
  const usedScenarios = new Set();

  const skills = files.skills
    .map((skill) => {
      const sources = computeSources(skill, cardIndex, supportIndex);
      verifySources(skill, sources, stats, mismatches);
      for (const entry of sources.characterCards) {
        usedCards.add(entry.cardId);
        for (const kind of entry.kinds) kindCounts.set(kind, (kindCounts.get(kind) ?? 0) + 1);
      }
      for (const cardId of sources.characterEvents) usedCards.add(cardId);
      for (const supportId of sources.supportHints) usedSupports.add(supportId);
      for (const supportId of sources.supportEvents) usedSupports.add(supportId);
      for (const scenarioId of sources.scenarioEvents) usedScenarios.add(scenarioId);
      return outputSkill(skill, sources);
    })
    .sort((a, b) => a.id - b.id);

  for (const cardId of usedCards) {
    if (!cardIndex.has(cardId)) missingRefs.push(`carta de personaje ${cardId}`);
  }
  for (const supportId of usedSupports) {
    if (!supportIndex.has(supportId)) missingRefs.push(`carta de apoyo ${supportId}`);
  }
  for (const scenarioId of usedScenarios) {
    if (!scenarioIds.has(scenarioId)) missingRefs.push(`escenario ${scenarioId}`);
  }

  const withSources = skills.filter(
    (skill) =>
      skill.sources.characterCards.length ||
      skill.sources.characterEvents.length ||
      skill.sources.supportHints.length ||
      skill.sources.supportEvents.length ||
      skill.sources.scenarioEvents.length,
  ).length;
  const unreleased = skills.filter((skill) => skill.unreleased.includes('en')).length;

  console.log(`Skills: ${skills.length} (${withSources} con fuentes, ${unreleased} sin lanzar en EN)`);
  console.log(`Cartas de personaje: ${files.characterCards.length} (referenciadas: ${usedCards.size})`);
  console.log(`Cartas de apoyo: ${files.supportCards.length} (referenciadas: ${usedSupports.size})`);
  console.log(`Escenarios: ${files.scenarios.length} (referenciados: ${usedScenarios.size})`);
  console.log(`Pares verificados -> char: ${stats.char}, char_e: ${stats.char_e}, sup_e: ${stats.sup_e}, sup_hint: ${stats.sup_hint}, sce_e: ${stats.sce_e}`);
  console.log('Kinds de carta de personaje:', [...kindCounts.entries()].map(([kind, count]) => `${kind}: ${count}`).join(', '));

  if (mismatches.length) {
    console.warn(`Aviso: ${mismatches.length} discrepancias entre skills.json y las cartas.`);
    for (const mismatch of mismatches.slice(0, 5)) {
      console.warn(`  ! skill ${mismatch.id} ${mismatch.field}: esperado ${JSON.stringify(mismatch.expected)}, calculado ${JSON.stringify(mismatch.got)}`);
    }
  }
  if (missingRefs.length) {
    console.warn(`Aviso: ${missingRefs.length} referencias sin metadata: ${missingRefs.slice(0, 5).join(', ')}`);
  }
  console.log(`Ejemplo 200012 (Right Turns ○): ${JSON.stringify(skills.find((skill) => skill.id === 200012)?.sources.characterCards.slice(0, 3))}`);
  console.log(`Ejemplo 110031 (unique de Tokai Teio): ${JSON.stringify(skills.find((skill) => skill.id === 110031)?.sources)}`);

  if (dryRun) {
    console.log('[dry-run] No se escribió nada.');
    return;
  }

  fs.mkdirSync(OUT_DIR, { recursive: true });
  const write = (name, value) => fs.writeFileSync(path.join(OUT_DIR, name), `${JSON.stringify(value, null, 2)}\n`);
  write('skills.json', skills);
  write('character-cards.json', sortById(files.characterCards.map((card) => [Number(card.card_id), outputCharacterCard(card)])));
  write('support-cards.json', sortById(files.supportCards.map((card) => [Number(card.support_id), outputSupportCard(card)])));
  write('scenarios.json', sortById(files.scenarios.map((scenario) => [Number(scenario.id), outputScenario(scenario)])));
  console.log(`Guardado en ${path.relative(root, OUT_DIR)}/`);
}

main().catch((error) => {
  console.error('Error fatal:', error);
  process.exitCode = 1;
});
