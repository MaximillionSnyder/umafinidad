package com.maximillionsnyder.umafinidad.cartas.domain

/* Tablas de etiquetas de las fichas: los códigos que trae GameTora (tipos de
   skill, de dónde sale cada skill, tipo y obtención de una carta de apoyo) se
   enseñan en el idioma elegido. Viven en domain y no en la UI porque son texto
   puro y así se pueden probar: EtiquetasDatosTest recorre los assets y falla si
   aparece un código nuevo sin traducir en alguno de los cinco idiomas, igual
   que hace CondicionesDatosTest con las condiciones.

   Las claves son los códigos del dato y no se traducen; los textos están
   contrastados con los assets (descripciones en en/ja/ko/tw), no inventados, y
   el español —que los datos no traen— es traducción propia de la interfaz.
   Ojo con los falsos amigos: "dir" es DIRT (tierra), no arena; las
   descripciones oficiales dicen "if it's a dirt race" / "in Dirt races". Y el
   mismo 1 corta / 2 milla / 3 media / 4 larga de las condiciones. */

/* Los cinco idiomas de una fila, en el mismo orden que IDIOMAS_UI
   (en, ja, ko, tw, es). */
private fun textosEtiqueta(en: String, ja: String, ko: String, tw: String, es: String): Map<String, String> =
    mapOf("en" to en, "ja" to ja, "ko" to ko, "tw" to tw, "es" to es)

/* En los datos, 逃げ/先行/差し/追込 son Runner/Leader/Betweener/Chaser en las
   descripciones viejas y Front Runner/Pace Chaser/Late Surger/End Closer en las
   nuevas; se usan los nombres largos, que son los del juego global. El coreano
   de los datos llama 선입 a 差し (no 차입) y el chino 領頭/前列/居中/後追. */
private val TABLA_TIPO: Map<String, Map<String, String>> = mapOf(
    "nac" to textosEtiqueta("No condition", "条件なし", "조건 없음", "無條件", "Sin condición"),
    "run" to textosEtiqueta("Front Runner", "逃げ", "도주", "領頭", "Puntera"),
    "ldr" to textosEtiqueta("Pace Chaser", "先行", "선행", "前列", "Perseguidora"),
    "btw" to textosEtiqueta("Late Surger", "差し", "선입", "居中", "Remontadora"),
    "cha" to textosEtiqueta("End Closer", "追込", "추입", "後追", "Rematadora"),
    "sho" to textosEtiqueta("Short", "短距離", "단거리", "短距離", "Velocidad corta"),
    "mil" to textosEtiqueta("Mile", "マイル", "마일", "一哩", "Milla"),
    "med" to textosEtiqueta("Medium", "中距離", "중거리", "中距離", "Media"),
    "lng" to textosEtiqueta("Long", "長距離", "장거리", "長距離", "Larga"),
    "dir" to textosEtiqueta("Dirt", "ダート", "더트", "沙地", "Tierra"),
    "tur" to textosEtiqueta("Turf", "芝", "잔디", "草地", "Césped"),
    "l_0" to textosEtiqueta("Opening Leg", "序盤", "초반", "前期", "Tramo inicial"),
    "l_1" to textosEtiqueta("Middle Leg", "中盤", "중반", "中期", "Tramo intermedio"),
    "l_2" to textosEtiqueta("Final Leg", "終盤", "종반", "後期", "Tramo final"),
    "l_3" to textosEtiqueta("Last Spurt", "ラストスパート", "라스트 스퍼트", "最後衝刺", "Arranque final"),
    "cor" to textosEtiqueta("Corner", "コーナー", "코너", "彎道", "Curva"),
    "str" to textosEtiqueta("Straight", "直線", "직선", "直線", "Recta"),
    "f_c" to textosEtiqueta("Final Corner", "最終コーナー", "최종 코너", "最後彎道", "Última curva"),
    "f_s" to textosEtiqueta("Final Straight", "最終直線", "최종 직선", "最後直線", "Última recta"),
    "slo" to textosEtiqueta("Slope", "坂", "경사", "坡度", "Pendiente"),
    "dbf" to textosEtiqueta("Debuff", "デバフ", "디버프", "負面效果", "Penalización"),
)

private val ETIQUETAS_TIPO = Textos(TABLA_TIPO)

fun etiquetaTipo(tipo: String, idioma: String): String = ETIQUETAS_TIPO[tipo, idioma]

/* La rareza viene como número: 1 normal, 2 rara, 3 y 5 únicas, 4 y 6 evoluciones. */
private val TABLA_RAREZA: Map<String, Map<String, String>> = mapOf(
    "1" to textosEtiqueta("Normal", "ノーマル", "노멀", "普通", "Normal"),
    "2" to textosEtiqueta("Rare", "レア", "레어", "稀有", "Rara"),
    "3" to textosEtiqueta("Unique", "ユニーク", "유니크", "獨特", "Única"),
    "4" to textosEtiqueta("Evolution", "進化", "진화", "進化", "Evolución"),
    "5" to textosEtiqueta("Unique", "ユニーク", "유니크", "獨特", "Única"),
    "6" to textosEtiqueta("Evolution", "進化", "진화", "進化", "Evolución"),
)

private val ETIQUETAS_RAREZA = Textos(TABLA_RAREZA)

/* El "?" es el neutro de siempre para una rareza que no llega. */
fun etiquetaRareza(rarity: Int?, idioma: String): String = when (rarity) {
    1 -> ETIQUETAS_RAREZA["1", idioma]
    2 -> ETIQUETAS_RAREZA["2", idioma]
    3 -> ETIQUETAS_RAREZA["3", idioma]
    4 -> ETIQUETAS_RAREZA["4", idioma]
    5 -> ETIQUETAS_RAREZA["5", idioma]
    6 -> ETIQUETAS_RAREZA["6", idioma]
    else -> "?"
}

private val TABLA_KIND: Map<String, Map<String, String>> = mapOf(
    "unique" to textosEtiqueta("Unique", "固有", "고유", "固有", "Única"),
    "innate" to textosEtiqueta("Innate", "初期", "초기", "初期", "Innata"),
    "awakening" to textosEtiqueta("Awakening", "覚醒", "각성", "覺醒", "Despertar"),
    "evolution" to textosEtiqueta("Evolution", "進化", "진화", "進化", "Evolución"),
)

private val ETIQUETAS_KIND = Textos(TABLA_KIND)

fun etiquetaKind(kind: String, idioma: String): String = ETIQUETAS_KIND[kind, idioma]

/* El tipo de una carta de apoyo es el palo de entrenamiento (スピード, スタミナ,
   パワー, 根性, 賢さ, 友人, グループ). En los datos el chino usa 持久力 para el
   aguante y 毅力 para el coraje, y el coreano 지능 para el ingenio. */
private val TABLA_APOYO: Map<String, Map<String, String>> = mapOf(
    "speed" to textosEtiqueta("Speed", "スピード", "스피드", "速度", "Velocidad"),
    "stamina" to textosEtiqueta("Stamina", "スタミナ", "스태미나", "持久力", "Resistencia"),
    "power" to textosEtiqueta("Power", "パワー", "파워", "力量", "Potencia"),
    "guts" to textosEtiqueta("Guts", "根性", "근성", "毅力", "Agallas"),
    "intelligence" to textosEtiqueta("Wit", "賢さ", "지능", "智力", "Ingenio"),
    "friend" to textosEtiqueta("Friend", "友人", "친구", "朋友", "Amistad"),
    "group" to textosEtiqueta("Group", "グループ", "그룹", "團體", "Grupo"),
)

private val ETIQUETAS_APOYO = Textos(TABLA_APOYO)

fun etiquetaTipoApoyo(type: String?, idioma: String): String =
    type?.let { ETIQUETAS_APOYO[it, idioma] } ?: "?"

val TIPOS_APOYO: List<String> =
    listOf("speed", "stamina", "power", "guts", "intelligence", "friend", "group")

private val TABLA_OBTENCION: Map<String, Map<String, String>> = mapOf(
    "gacha" to textosEtiqueta("Gacha", "ガチャ", "가챠", "轉蛋", "Gacha"),
    "main_story" to textosEtiqueta("Main Story", "メインストーリー", "메인 스토리", "主線故事", "Historia principal"),
    "story_event" to textosEtiqueta("Story Event", "ストーリーイベント", "스토리 이벤트", "故事活動", "Evento de historia"),
    "promo" to textosEtiqueta("Promotion", "プロモ", "프로모션", "宣傳", "Promoción"),
    "limited_quest" to textosEtiqueta("Limited Quest", "限定クエスト", "한정 퀘스트", "限定任務", "Misión limitada"),
    "shop" to textosEtiqueta("Shop", "ショップ", "상점", "商店", "Tienda"),
)

private val ETIQUETAS_OBTENCION = Textos(TABLA_OBTENCION)

fun etiquetaObtencion(obtained: String?, idioma: String): String =
    obtained?.let { ETIQUETAS_OBTENCION[it, idioma] } ?: "?"

/* Todas las filas de las tablas, con un nombre legible delante, para el test que
   exige que cada una esté en los cinco idiomas de la interfaz (no vale caer al
   inglés). */
val FILAS_ETIQUETAS: List<Pair<String, Map<String, String>>> =
    TABLA_TIPO.entries.map { it.key to it.value } +
        TABLA_RAREZA.entries.map { "rareza=${it.key}" to it.value } +
        TABLA_KIND.entries.map { it.key to it.value } +
        TABLA_APOYO.entries.map { "apoyo=${it.key}" to it.value } +
        TABLA_OBTENCION.entries.map { "obtencion=${it.key}" to it.value }
