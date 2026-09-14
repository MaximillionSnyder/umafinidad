/* Textos propios de la web que todavía no existen en los strings.xml de
   la app Android (compartir por URL y export/import de árboles).
   Se fusionan con los locales generados por scripts/extract-strings.mjs. */

import type { CodigoIdioma } from './index'

type Tabla = Record<string, string>

export const EXTRA: Record<CodigoIdioma, Tabla> = {
  es: {
    compartir: 'Compartir',
    compartir_titulo: 'Compartir esta selección',
    compartir_desc: 'Copiá el enlace para abrir exactamente esta herencia en otro dispositivo.',
    copiar_enlace: 'Copiar enlace',
    enlace_copiado: 'Enlace copiado',
    enlace_invalido: 'El enlace compartido no es válido.',
    datos_titulo: 'Datos',
    exportar_arboles: 'Exportar configuraciones',
    exportar_arboles_desc: 'Descarga un archivo JSON con todas tus configuraciones guardadas.',
    exportar_vacio: 'No hay configuraciones guardadas para exportar.',
    exportado_snack: 'Configuraciones exportadas',
    importar_arboles: 'Importar configuraciones',
    importar_arboles_desc: 'Carga un archivo exportado; las configuraciones repetidas se actualizan.',
    importados_snack: 'Se importaron {0} configuraciones',
    importar_error: 'No se pudo importar el archivo.',
  },
  en: {
    compartir: 'Share',
    compartir_titulo: 'Share this selection',
    compartir_desc: 'Copy the link to open exactly this inheritance on another device.',
    copiar_enlace: 'Copy link',
    enlace_copiado: 'Link copied',
    enlace_invalido: 'The shared link is not valid.',
    datos_titulo: 'Data',
    exportar_arboles: 'Export configurations',
    exportar_arboles_desc: 'Download a JSON file with all your saved configurations.',
    exportar_vacio: 'There are no saved configurations to export.',
    exportado_snack: 'Configurations exported',
    importar_arboles: 'Import configurations',
    importar_arboles_desc: 'Load an exported file; duplicated configurations are updated.',
    importados_snack: 'Imported {0} configurations',
    importar_error: 'The file could not be imported.',
  },
  ja: {
    compartir: '共有',
    compartir_titulo: 'この選択を共有',
    compartir_desc: 'リンクをコピーすると、別の端末で同じ継承を開けます。',
    copiar_enlace: 'リンクをコピー',
    enlace_copiado: 'リンクをコピーしました',
    enlace_invalido: '共有リンクが正しくありません。',
    datos_titulo: 'データ',
    exportar_arboles: '設定をエクスポート',
    exportar_arboles_desc: '保存した設定を JSON ファイルとしてダウンロードします。',
    exportar_vacio: 'エクスポートする設定がありません。',
    exportado_snack: '設定をエクスポートしました',
    importar_arboles: '設定をインポート',
    importar_arboles_desc: 'エクスポートしたファイルを読み込みます。重複する設定は更新されます。',
    importados_snack: '{0} 件の設定をインポートしました',
    importar_error: 'ファイルをインポートできませんでした。',
  },
}
