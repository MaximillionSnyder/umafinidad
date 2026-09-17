/* Preferencias de UI persistidas en localStorage. Espejo de
   data/PrefsRepository.kt + enums de data/ (ThemeMode, Idioma,
   TamanoTexto, EstiloAvatar, ModoGrilla). */

export enum ModoGrilla {
  TARJETAS = 'TARJETAS',
  LISTA = 'LISTA',
}

export enum TamanoTexto {
  NORMAL = 'NORMAL',
  GRANDE = 'GRANDE',
  MUY_GRANDE = 'MUY_GRANDE',
}

export const ESCALA_TAMANO: Record<TamanoTexto, number> = {
  [TamanoTexto.NORMAL]: 1,
  [TamanoTexto.GRANDE]: 1.15,
  [TamanoTexto.MUY_GRANDE]: 1.3,
}

export enum EstiloAvatar {
  COLOR = 'COLOR',
  GRISES = 'GRISES',
  MONOCROMO = 'MONOCROMO',
}

/* SISTEMA respeta el ajuste del sistema; ALTO_CONTRASTE también lo respeta
   pero con colores de mayor contraste. */
export enum ThemeMode {
  SISTEMA = 'SISTEMA',
  CLARO = 'CLARO',
  OSCURO = 'OSCURO',
  ALTO_CONTRASTE = 'ALTO_CONTRASTE',
}

export enum Idioma {
  SISTEMA = 'SISTEMA',
  ESPANOL = 'ESPANOL',
  INGLES = 'INGLES',
  JAPONES = 'JAPONES',
  CHINO_SIMPLIFICADO = 'CHINO_SIMPLIFICADO',
  CHINO_TRADICIONAL = 'CHINO_TRADICIONAL',
  COREANO = 'COREANO',
  INDONESIO = 'INDONESIO',
  TAILANDES = 'TAILANDES',
  VIETNAMITA = 'VIETNAMITA',
}

export type CodigoIdioma =
  | 'es'
  | 'en'
  | 'ja'
  | 'zh-CN'
  | 'zh-TW'
  | 'ko'
  | 'id'
  | 'th'
  | 'vi'

export function codigoIdioma(idioma: Idioma): CodigoIdioma | null {
  switch (idioma) {
    case Idioma.ESPANOL:
      return 'es'
    case Idioma.INGLES:
      return 'en'
    case Idioma.JAPONES:
      return 'ja'
    case Idioma.CHINO_SIMPLIFICADO:
      return 'zh-CN'
    case Idioma.CHINO_TRADICIONAL:
      return 'zh-TW'
    case Idioma.COREANO:
      return 'ko'
    case Idioma.INDONESIO:
      return 'id'
    case Idioma.TAILANDES:
      return 'th'
    case Idioma.VIETNAMITA:
      return 'vi'
    default:
      return null
  }
}

/* Tamaño sugerido según el fontScale del sistema. */
export function tamanoSegunFontScale(fontScale: number): TamanoTexto {
  if (fontScale >= 1.3) return TamanoTexto.MUY_GRANDE
  if (fontScale >= 1.15) return TamanoTexto.GRANDE
  return TamanoTexto.NORMAL
}

function leer<T>(clave: string, porDefecto: T): T {
  try {
    const raw = localStorage.getItem(clave)
    return raw === null ? porDefecto : (JSON.parse(raw) as T)
  } catch {
    return porDefecto
  }
}

function escribir(clave: string, valor: unknown): void {
  try {
    localStorage.setItem(clave, JSON.stringify(valor))
  } catch {
    /* Modo privado o cuota llena: la preferencia no persiste, la app sigue. */
  }
}

const CLAVES = {
  gridVertical: 'grid_vertical',
  estiloAvatar: 'estilo_avatar',
  tema: 'tema_modo',
  idioma: 'idioma_modo',
  tamanoTexto: 'tamano_texto',
  textoNegrita: 'texto_negrita',
  bienvenidaAcce: 'bienvenida_acce_vista',
  /* Solo lectura para migrar instalaciones con el interruptor viejo. */
  altoContraste: 'alto_contraste',
} as const

function enumValido<T extends string>(valores: T[], raw: unknown, porDefecto: T): T {
  return typeof raw === 'string' && (valores as string[]).includes(raw) ? (raw as T) : porDefecto
}

export class PrefsRepository {
  get modoGrilla(): ModoGrilla {
    return leer(CLAVES.gridVertical, true) ? ModoGrilla.TARJETAS : ModoGrilla.LISTA
  }
  set modoGrilla(valor: ModoGrilla) {
    escribir(CLAVES.gridVertical, valor === ModoGrilla.TARJETAS)
  }

  get estiloAvatar(): EstiloAvatar {
    return enumValido(Object.values(EstiloAvatar), leer(CLAVES.estiloAvatar, null), EstiloAvatar.COLOR)
  }
  set estiloAvatar(valor: EstiloAvatar) {
    escribir(CLAVES.estiloAvatar, valor)
  }

  get tema(): ThemeMode {
    const raw = leer<unknown>(CLAVES.tema, null)
    if (typeof raw === 'string' && Object.values(ThemeMode).includes(raw as ThemeMode)) {
      return raw as ThemeMode
    }
    // Migración: el interruptor de contraste (v3.9.x) ahora es un tema.
    if (leer(CLAVES.altoContraste, false)) {
      escribir(CLAVES.tema, ThemeMode.ALTO_CONTRASTE)
      try {
        localStorage.removeItem(CLAVES.altoContraste)
      } catch {
        /* sin storage disponible */
      }
      return ThemeMode.ALTO_CONTRASTE
    }
    return ThemeMode.SISTEMA
  }
  set tema(valor: ThemeMode) {
    escribir(CLAVES.tema, valor)
  }

  get idioma(): Idioma {
    return enumValido(Object.values(Idioma), leer(CLAVES.idioma, null), Idioma.SISTEMA)
  }
  set idioma(valor: Idioma) {
    escribir(CLAVES.idioma, valor)
  }

  get tamanoTexto(): TamanoTexto {
    return enumValido(Object.values(TamanoTexto), leer(CLAVES.tamanoTexto, null), TamanoTexto.NORMAL)
  }
  set tamanoTexto(valor: TamanoTexto) {
    escribir(CLAVES.tamanoTexto, valor)
  }

  get textoNegrita(): boolean {
    return leer(CLAVES.textoNegrita, false)
  }
  set textoNegrita(valor: boolean) {
    escribir(CLAVES.textoNegrita, valor)
  }

  get bienvenidaAccesibilidadVista(): boolean {
    return leer(CLAVES.bienvenidaAcce, false)
  }
  set bienvenidaAccesibilidadVista(valor: boolean) {
    escribir(CLAVES.bienvenidaAcce, valor)
  }
}
