package com.maximillionsnyder.umafinidad.cartas.domain

/* Las condiciones de las skills llegan de GameTora como una expresión con claves
   internas: "distance_rate>=50&distance_rate<=60&order_rate>50". Leídas así no
   hay quien las entienda, así que se parten en términos y se traducen las claves
   conocidas al idioma elegido: los cuatro de los datos (en/ja/ko/tw) más el
   español, que es de la interfaz. Lo que no está en la tabla se deja tal cual y
   se marca como no traducido, para que la ficha pueda enseñar también la
   expresión original y no se pierda información.

   Ojo con los valores: son los del juego y no significan lo mismo de una clave a
   otra. En `distance_type` el 2 es milla y el 3 media; en `weather` el 2 es
   nublado y el 3 lluvia; y `motivation` va de 1 (fatal) a 5 (perfecta). Cada
   tabla de abajo está contrastada con la descripción oficial de las skills que
   usan ese valor, que es la única fuente fiable: el número por sí solo engaña.

   Las claves no se traducen; los textos de en/ja/ko/tw salen de los assets, no
   inventados: el coreano de los datos llama 선입 a 差し (no 차입) y el chino
   領頭/前列/居中/後追 a los estilos. El español no está en los datos: es
   traducción de la etiqueta inglesa, con la misma terminología que el resto de
   la sección (última recta, arranque final, rail interior…), y va también en la
   tabla para que la ficha no caiga al inglés con la interfaz en español. */

/* Claves que son un número (posición, metros, porcentaje…): siempre se enseña el
   valor. El resto se leen como sí/no cuando el valor es 0 o 1. */
private val NUMERICAS: Set<String> = setOf(
    "order", "order_rate", "distance_rate", "distance_rate_after_random",
    "remain_distance", "course_distance", "accumulatetime", "blocked_side_continuetime",
    "blocked_front_continuetime", "corner_count", "near_infront_count", "fan_count",
    "visiblehorse",
    /* Contadores de skills activadas y de rivales. */
    "activate_count_all_team", "activate_count_end_after", "activate_count_heal",
    "activate_count_later_half", "activate_count_middle", "activate_count_param_skill",
    "activate_count_start", "same_skill_horse_count", "succession_skill_count",
    "temptation_opponent_count_behind", "temptation_opponent_count_infront",
    "running_style_count_nige_otherself", "running_style_count_senko_otherself",
    "running_style_count_sashi_otherself", "running_style_count_oikomi_otherself",
    "running_style_count_same", "running_style_count_same_rate",
    "running_style_temptation_opponent_count_nige", "running_style_temptation_opponent_count_senko",
    "running_style_temptation_opponent_count_sashi", "running_style_temptation_opponent_count_oikomi",
    /* Números sueltos. */
    "base_guts", "overtake_target_no_order_up_time", "post_number", "random_lot",
    "random_lot_shared", "remain_distance_viewer_id", "behind_near_lane_time_set1",
    "change_order_up_finalcorner_after", "distance_diff_top_float",
    "distance_diff_top", "distance_diff_rate", "bashin_diff_behind", "bashin_diff_infront",
    "slope", "base_power", "base_wiz", "base_speed", "base_stamina", "temptation_count",
    "popularity", "track_id", "near_count", "compete_fight_count", "infront_near_lane_time",
    "behind_near_lane_time", "overtake_target_time", "hp_per", "activate_count_all",
    "is_used_skill_id",
)

/* Los cinco idiomas de una fila, en el mismo orden que IDIOMAS_UI
   (en, ja, ko, tw, es). */
private fun textosCondicion(en: String, ja: String, ko: String, tw: String, es: String): Map<String, String> =
    mapOf("en" to en, "ja" to ja, "ko" to ko, "tw" to tw, "es" to es)

private val TABLA_ETIQUETAS: Map<String, Map<String, String>> = mapOf(
    /* dónde y cuándo */
    "always" to textosCondicion("always", "常時", "항상", "始終", "siempre"),
    "phase" to textosCondicion("phase", "フェーズ", "페이즈", "時期", "fase"),
    "corner" to textosCondicion("on a corner", "コーナー", "코너", "彎道", "en una curva"),
    "straight_random" to textosCondicion("on a straight (any)", "直線のどこか", "직선 어딘가", "直線任意位置", "en una recta (cualquiera)"),
    "down_slope_random" to textosCondicion("on a downhill (any)", "下り坂のどこか", "내리막 어딘가", "下坡任意位置", "en una bajada (cualquiera)"),
    "up_slope_random" to textosCondicion("on an uphill (any)", "上り坂のどこか", "오르막 어딘가", "上坡任意位置", "en una subida (cualquiera)"),
    "up_slope_random_later_half" to textosCondicion("on an uphill in the second half (any)", "後半の上り坂のどこか", "후반 오르막 어딘가", "後半上坡任意位置", "en una subida de la segunda mitad (cualquiera)"),
    "last_straight_random" to textosCondicion("on the last straight (any)", "最終直線のどこか", "최종 직선 어딘가", "最後直線任意位置", "en la última recta (cualquiera)"),
    "run_at_full_speed_random" to textosCondicion("at full speed (any)", "全力のどこか", "전력 어딘가", "全力任意位置", "a máxima velocidad (cualquiera)"),
    /* El carril 0 es el rail interior: las skills dicen "near the inner rail". */
    "lane_type" to textosCondicion("lane", "レーン", "레인", "跑道", "carril"),
    "straight_front_type" to textosCondicion("on a straight", "直線", "직선", "直線", "en una recta"),
    "is_finalcorner" to textosCondicion("on the final corner", "最終コーナー", "최종 코너", "最後彎道", "en la última curva"),
    "is_finalcorner_laterhalf" to textosCondicion("on the second half of the final corner", "最終コーナーの後半", "최종 코너 후반", "最後彎道後半", "en la segunda mitad de la última curva"),
    "is_finalcorner_random" to textosCondicion("on the final corner (any)", "最終コーナーのどこか", "최종 코너 어딘가", "最後彎道任意位置", "en la última curva (cualquiera)"),
    "is_last_straight" to textosCondicion("on the last straight", "最終直線", "최종 직선", "最後直線", "en la última recta"),
    "is_last_straight_onetime" to textosCondicion("on the last straight (once)", "最終直線（1回）", "최종 직선 (1회)", "最後直線（1次）", "en la última recta (una vez)"),
    "is_lastspurt" to textosCondicion("in the last spurt", "ラストスパート中", "라스트 스퍼트 중", "最後衝刺中", "en el arranque final"),
    "lastspurt" to textosCondicion("last spurt", "ラストスパート", "라스트 스퍼트", "最後衝刺", "arranque final"),
    "all_corner_random" to textosCondicion("on any corner", "いずれかのコーナー", "아무 코너", "任一彎道", "en cualquier curva"),
    /* qué pasa en la carrera */
    "order" to textosCondicion("position", "順位", "순위", "名次", "posición"),
    "order_rate" to textosCondicion("position (%)", "順位 (%)", "순위 (%)", "名次 (%)", "posición (%)"),
    "distance_rate" to textosCondicion("race progress (%)", "進行度 (%)", "진행도 (%)", "進度 (%)", "progreso de carrera (%)"),
    "distance_rate_after_random" to textosCondicion("race progress after (%)", "その後の進行度 (%)", "이후 진행도 (%)", "之後進度 (%)", "progreso de carrera después (%)"),
    "remain_distance" to textosCondicion("remaining distance", "残り距離", "남은 거리", "剩餘距離", "distancia restante"),
    "course_distance" to textosCondicion("race distance", "レース距離", "레이스 거리", "競賽距離", "distancia de la carrera"),
    "distance_diff_top" to textosCondicion("gap to the leader", "先頭との差", "선두와의 차이", "與領先者差距", "diferencia con la líder"),
    "distance_diff_rate" to textosCondicion("gap to the leader (%)", "先頭との差 (%)", "선두와의 차이 (%)", "與領先者差距 (%)", "diferencia con la líder (%)"),
    "bashin_diff_behind" to textosCondicion("gap to the runner behind", "後ろとの差", "뒤와의 차이", "與後方差距", "diferencia con la corredora de detrás"),
    "bashin_diff_infront" to textosCondicion("gap to the runner ahead", "前との差", "앞과의 차이", "與前方差距", "diferencia con la corredora de delante"),
    "is_overtake" to textosCondicion("when overtaking", "追い抜き時", "추월 시", "超越時", "al adelantar"),
    "overtake_target_time" to textosCondicion("overtaking time", "追い抜き時間", "추월 시간", "超越時間", "tiempo adelantando"),
    "is_behind_in" to textosCondicion("behind", "後方", "후방", "後方", "detrás"),
    "is_move_lane" to textosCondicion("when changing lanes", "進路変更時", "진로 변경 시", "變換跑道時", "al cambiar de carril"),
    "is_goodstart" to textosCondicion("good start", "好スタート", "좋은 출발", "良好起跑", "buena salida"),
    "is_badstart" to textosCondicion("late start", "出遅れ", "출발 지연", "起跑延遲", "salida tardía"),
    "change_order_onetime" to textosCondicion("position change (once)", "順位変動（1回）", "순위 변동 (1회)", "名次變動（1次）", "cambio de posición (una vez)"),
    "change_order_up_end_after" to textosCondicion("after moving up late", "終盤の順位上げ後", "종반 순위 상승 후", "後期名次提升後", "tras subir posiciones en el tramo final"),
    "change_order_up_middle" to textosCondicion("when moving up", "順位上げ時", "순위 상승 시", "名次提升時", "al subir posiciones"),
    "blocked_side_continuetime" to textosCondicion("blocked time", "ブロックされた時間", "방해받은 시간", "受阻時間", "tiempo bloqueada"),
    /* El paso tapado por delante: "when the way ahead is jammed". */
    "blocked_front" to textosCondicion("with the way ahead blocked", "前方が塞がれた状態", "앞이 막힌 상태", "前方受阻", "con el paso bloqueado delante"),
    "blocked_front_continuetime" to textosCondicion("time with the way ahead blocked", "前方が塞がれた時間", "앞이 막힌 시간", "前方受阻時間", "tiempo con el paso bloqueado delante"),
    "is_surrounded" to textosCondicion("surrounded", "包囲された状態", "포위된 상태", "被包圍", "rodeada"),
    /* 掛かり (rushed): el aviso de "tentado" de toda la vida; el chino dice 暴衝. */
    "is_temptation" to textosCondicion("rushed", "掛かり状態", "흥분 상태", "暴衝狀態", "tentada"),
    "near_infront_count" to textosCondicion("rivals right ahead", "直前のライバル", "바로 앞 라이벌", "正前方對手", "rivales justo delante"),
    /* Pistas sin curvas: "tracks with no corners". */
    "corner_count" to textosCondicion("track corners", "コースのコーナー数", "코스 코너 수", "賽道彎道數", "curvas de la pista"),
    "infront_near_lane_time" to textosCondicion("time right behind a rival", "前方に張り付いた時間", "앞에 붙어 있는 시간", "緊貼前方時間", "tiempo justo detrás de una rival"),
    "behind_near_lane_time" to textosCondicion("time with a rival right behind", "後方に張り付かれた時間", "뒤에 붙어 있는 시간", "被緊貼時間", "tiempo con una rival justo detrás"),
    "near_count" to textosCondicion("nearby rivals", "近くのライバル", "근처 라이벌", "附近對手", "rivales cercanas"),
    "compete_fight_count" to textosCondicion("rivals in a duel", "競り合い中のライバル", "경쟁 중인 라이벌", "競爭中的對手", "rivales en un duelo"),
    /* "Mantenerse" en un puesto: las skills dicen "for the entire first half",
       "stayed well-positioned from the start up until the second half". El
       número va en la propia clave, así que la frase ya lo lleva. */
    "order_rate_in20_continue" to textosCondicion("stay in the top 20%", "トップ20%を維持", "상위 20% 유지", "維持前20%", "mantenerse en el primer 20 %"),
    "order_rate_in40_continue" to textosCondicion("stay in the top 40%", "トップ40%を維持", "상위 40% 유지", "維持前40%", "mantenerse en el primer 40 %"),
    "order_rate_in50_continue" to textosCondicion("stay in the top 50%", "トップ50%を維持", "상위 50% 유지", "維持前50%", "mantenerse en el primer 50 %"),
    "order_rate_in80_continue" to textosCondicion("stay in the top 80%", "トップ80%を維持", "상위 80% 유지", "維持前80%", "mantenerse en el primer 80 %"),
    "order_rate_out20_continue" to textosCondicion("stay outside the top 20%", "トップ20%圏外を維持", "상위 20% 밖 유지", "維持前20%之外", "mantenerse fuera del primer 20 %"),
    "order_rate_out40_continue" to textosCondicion("stay outside the top 40%", "トップ40%圏外を維持", "상위 40% 밖 유지", "維持前40%之外", "mantenerse fuera del primer 40 %"),
    "order_rate_out50_continue" to textosCondicion("stay outside the top 50%", "トップ50%圏外を維持", "상위 50% 밖 유지", "維持前50%之外", "mantenerse fuera del primer 50 %"),
    "order_rate_out70_continue" to textosCondicion("stay outside the top 70%", "トップ70%圏外を維持", "상위 70% 밖 유지", "維持前70%之外", "mantenerse fuera del primer 70 %"),
    /* estado de la carrera y del corredor */
    "running_style" to textosCondicion("running style", "脚質", "작전", "作戰", "estilo de carrera"),
    "distance_type" to textosCondicion("distance", "距離", "거리", "距離", "distancia"),
    "ground_type" to textosCondicion("ground", "馬場", "트랙", "場地", "superficie"),
    "ground_condition" to textosCondicion("track condition", "馬場状態", "주로 상태", "場地狀態", "estado de la pista"),
    "weather" to textosCondicion("weather", "天気", "날씨", "天氣", "clima"),
    "season" to textosCondicion("season", "季節", "계절", "季節", "estación"),
    "rotation" to textosCondicion("track direction", "コース向き", "코스 방향", "賽道方向", "sentido de la pista"),
    "slope" to textosCondicion("slope", "坂", "경사", "坡度", "pendiente"),
    "popularity" to textosCondicion("popularity", "人気", "인기", "支持度", "popularidad"),
    "motivation" to textosCondicion("mood", "やる気", "컨디션", "幹勁", "motivación"),
    "hp_per" to textosCondicion("stamina (%)", "持久力 (%)", "지구력 (%)", "持久力 (%)", "resistencia (%)"),
    "base_speed" to textosCondicion("base speed", "基礎スピード", "기본 스피드", "基礎速度", "velocidad base"),
    "base_stamina" to textosCondicion("base stamina", "基礎スタミナ", "기본 스태미나", "基礎持久力", "resistencia base"),
    "base_power" to textosCondicion("base power", "基礎パワー", "기본 파워", "基礎力量", "potencia base"),
    "base_wiz" to textosCondicion("base wit", "基礎賢さ", "기본 지능", "基礎智力", "ingenio base"),
    "temptation_count" to textosCondicion("rushes", "掛かり回数", "흥분 횟수", "暴衝次數", "veces tentada"),
    "temptation_opponent_count_behind" to textosCondicion("rushed rivals behind", "後方の掛かりライバル", "뒤쪽 흥분 라이벌", "後方暴衝對手", "rivales tentadas por detrás"),
    "temptation_opponent_count_infront" to textosCondicion("rushed rivals ahead", "前方の掛かりライバル", "앞쪽 흥분 라이벌", "前方暴衝對手", "rivales tentadas por delante"),
    /* Rivales por estilo: 逃げ/先行/差し/追込, igual que la tabla running_style. */
    "running_style_count_nige_otherself" to textosCondicion("Front Runner rivals", "逃げのライバル", "도주 라이벌", "領頭對手", "rivales punteras"),
    "running_style_count_senko_otherself" to textosCondicion("Pace Chaser rivals", "先行のライバル", "선행 라이벌", "前列對手", "rivales perseguidoras"),
    "running_style_count_sashi_otherself" to textosCondicion("Late Surger rivals", "差しのライバル", "선입 라이벌", "居中對手", "rivales remontadoras"),
    "running_style_count_oikomi_otherself" to textosCondicion("End Closer rivals", "追込のライバル", "추입 라이벌", "後追對手", "rivales rematadoras"),
    "running_style_count_same" to textosCondicion("rivals with your running style", "同じ脚質のライバル", "같은 작전 라이벌", "相同作戰對手", "rivales con tu mismo estilo de carrera"),
    "running_style_count_same_rate" to textosCondicion("rivals with your running style (%)", "同じ脚質のライバル (%)", "같은 작전 라이벌 (%)", "相同作戰對手 (%)", "rivales con tu mismo estilo de carrera (%)"),
    "running_style_temptation_opponent_count_nige" to textosCondicion("rushed Front Runner rivals", "掛かった逃げのライバル", "흥분한 도주 라이벌", "暴衝的領頭對手", "rivales punteras tentadas"),
    "running_style_temptation_opponent_count_senko" to textosCondicion("rushed Pace Chaser rivals", "掛かった先行のライバル", "흥분한 선행 라이벌", "暴衝的前列對手", "rivales perseguidoras tentadas"),
    "running_style_temptation_opponent_count_sashi" to textosCondicion("rushed Late Surger rivals", "掛かった差しのライバル", "흥분한 선입 라이벌", "暴衝的居中對手", "rivales remontadoras tentadas"),
    "running_style_temptation_opponent_count_oikomi" to textosCondicion("rushed End Closer rivals", "掛かった追込のライバル", "흥분한 추입 라이벌", "暴衝的後追對手", "rivales rematadoras tentadas"),
    "running_style_equal_popularity_one" to textosCondicion("the favorite has your running style", "1番人気が同じ脚質", "1번 인기가 같은 작전", "支持度第1相同作戰", "la favorita tiene tu mismo estilo de carrera"),
    /* Contadores de skills. "param" son las verdes: la skill dice "green skills". */
    "activate_count_start" to textosCondicion("skills activated early", "序盤に発動したスキル", "초반 발동 스킬", "前期發動技能", "skills activadas al principio"),
    "activate_count_middle" to textosCondicion("skills activated mid-race", "中盤に発動したスキル", "중반 발동 스킬", "中期發動技能", "skills activadas a mitad de carrera"),
    "activate_count_later_half" to textosCondicion("skills activated in the second half", "後半に発動したスキル", "후반 발동 스킬", "後半發動技能", "skills activadas en la segunda mitad"),
    "activate_count_end_after" to textosCondicion("skills activated late", "終盤に発動したスキル", "종반 발동 스킬", "後期發動技能", "skills activadas al final"),
    "activate_count_heal" to textosCondicion("recovery skills activated", "回復スキル発動数", "회복 스킬 발동 수", "回復技能發動數", "skills de recuperación activadas"),
    "activate_count_param_skill" to textosCondicion("green skills activated", "能力を引き出すスキル発動数", "능력 스킬 발동 수", "能力技能發動數", "skills verdes activadas"),
    "activate_count_all_team" to textosCondicion("team skills activated", "チームのスキル発動数", "팀 스킬 발동 수", "隊伍技能發動數", "skills de equipo activadas"),
    "same_skill_horse_count" to textosCondicion("rivals with the same skill", "同じスキルのライバル", "같은 스킬 라이벌", "相同技能對手", "rivales con la misma skill"),
    "succession_skill_count" to textosCondicion("inherited skills", "継承スキル", "계승 스킬", "繼承技能", "skills heredadas"),
    /* Números sueltos. */
    "base_guts" to textosCondicion("base guts", "基礎根性", "기본 근성", "基礎毅力", "agallas base"),
    "post_number" to textosCondicion("starting bracket", "枠番", "출발 그룹", "閘位", "cajón de salida"),
    "furlong" to textosCondicion("furlong", "ハロン", "펄롱", "化朗", "furlong"),
    "grade" to textosCondicion("race grade", "レース格", "레이스 등급", "競賽級別", "categoría de la carrera"),
    "time" to textosCondicion("time of day", "時間帯", "시간대", "時段", "momento del día"),
    "random_lot" to textosCondicion("chance (%)", "発動確率 (%)", "발동 확률 (%)", "發動機率 (%)", "probabilidad (%)"),
    "random_lot_shared" to textosCondicion("shared chance (%)", "共有確率 (%)", "공유 확률 (%)", "共享機率 (%)", "probabilidad compartida (%)"),
    "remain_distance_viewer_id" to textosCondicion("remaining distance of the watched runner", "注目ウマ娘の残り距離", "주목 우마무스메의 남은 거리", "注目賽馬娘的剩餘距離", "distancia restante de la corredora observada"),
    "overtake_target_no_order_up_time" to textosCondicion("time stuck behind a rival without passing", "抜けずに張り付いた時間", "추월하지 못하고 붙어 있는 시간", "未超越緊貼時間", "tiempo detrás de una rival sin adelantarla"),
    "change_order_up_finalcorner_after" to textosCondicion("overtakes after the final corner", "最終コーナー後の追い抜き", "최종 코너 이후 추월", "最後彎道後超越", "adelantamientos tras la última curva"),
    "distance_diff_top_float" to textosCondicion("gap to the leader", "先頭との差", "선두와의 차이", "與領先者差距", "diferencia con la líder"),
    /* El sufijo _set1 no aparece documentado en el dato: se deja la misma frase
       que la clave base en vez de inventarse un matiz. */
    "behind_near_lane_time_set1" to textosCondicion("time with a rival right behind", "後方に張り付かれた時間", "뒤에 붙어 있는 시간", "被緊貼時間", "tiempo con una rival justo detrás"),
    "track_id" to textosCondicion("racetrack", "競馬場", "경기장", "賽馬場", "hipódromo"),
    "is_basis_distance" to textosCondicion("standard distance", "基準距離", "기준 거리", "基準距離", "distancia estándar"),
    "accumulatetime" to textosCondicion("accumulated time", "累積時間", "누적 시간", "累計時間", "tiempo acumulado"),
    /* Carreras de fuera y de tierra de grado (Kawasaki, Funabashi, Morioka, Ooi). */
    "is_abroad" to textosCondicion("overseas", "海外", "해외", "海外", "en el extranjero"),
    "is_dirtgrade" to textosCondicion("in a dirt graded race", "ダート重賞", "더트 등급 경주", "泥地重賞", "en una carrera de grado sobre tierra"),
    /* Hipódromos de curvas cerradas y aforo. */
    "is_tight_track" to textosCondicion("on a track with sharp turns", "小回りコース", "코너가 작은 코스", "急彎賽道", "en una pista de curvas cerradas"),
    "fan_count" to textosCondicion("fans", "ファン数", "팬 수", "粉絲數", "fans"),
    "visiblehorse" to textosCondicion("visible rivals", "視界のライバル", "보이는 라이벌", "可見對手", "rivales visibles"),
    /* skills */
    "is_activate_any_skill" to textosCondicion("with any skill activated", "いずれかのスキル発動時", "아무 스킬 발동 시", "任一技能發動時", "al activarse cualquier skill"),
    "is_activate_other_skill_detail" to textosCondicion("with another skill activated", "他のスキル発動時", "다른 스킬 발동 시", "其他技能發動時", "al activarse otra skill"),
    "is_other_character_activate_advantage_skill" to textosCondicion("when another runner activates an advantage skill", "他のウマ娘が有利スキル発動時", "다른 우마무스메가 유리 스킬 발동 시", "其他賽馬娘發動有利技能時", "cuando otra corredora activa una skill ventajosa"),
    "is_used_skill_id" to textosCondicion("specific skill", "特定スキル", "특정 스킬", "特定技能", "skill concreta"),
    "is_used_skill_id_with_detail_one" to textosCondicion("specific skill", "特定スキル", "특정 스킬", "特定技能", "skill concreta"),
    "is_exist_skill_id" to textosCondicion("skill in the race", "レース内のスキル", "레이스 내 스킬", "競賽內技能", "skill en la carrera"),
    "is_activate_heal_skill" to textosCondicion("with a recovery skill used", "回復スキル使用時", "회복 스킬 사용 시", "使用回復技能時", "al usar una skill de recuperación"),
    "is_popularity_top_character_activate_advantage_skill" to textosCondicion("the favorite activates an advantage skill", "1番人気が有利スキル発動", "1번 인기가 유리 스킬 발동", "支持度第1發動有利技能", "la favorita activa una skill ventajosa"),
    /* Qué personaje tiene que estar en la carrera (1002 es Silence Suzuka). */
    "is_exist_chara_id" to textosCondicion("character in the race", "レース内のキャラ", "레이스 내 캐릭터", "競賽內角色", "personaje en la carrera"),
    "activate_count_all" to textosCondicion("skills activated", "発動スキル数", "발동 스킬 수", "發動技能數", "skills activadas"),
)

private val ETIQUETAS = Textos(TABLA_ETIQUETAS)

/* Valores con nombre. Se leen "etiqueta: valor" cuando el operador es ==. */
private val TABLA_VALORES: Map<String, Map<String, Map<String, String>>> = mapOf(
    /* 0 apertura, 1 mitad, 2 recta final, 3 último esprint. */
    "phase" to mapOf(
        "0" to textosCondicion("Opening Leg", "序盤", "초반", "前期", "tramo inicial"),
        "1" to textosCondicion("Middle Leg", "中盤", "중반", "中期", "tramo intermedio"),
        "2" to textosCondicion("Final Leg", "終盤", "종반", "後期", "tramo final"),
        "3" to textosCondicion("Last Spurt", "ラストスパート", "라스트 스퍼트", "最後衝刺", "arranque final"),
    ),
    /* 1 逃げ, 2 先行, 3 差し, 4 追込. */
    "running_style" to mapOf(
        "1" to textosCondicion("Front Runner", "逃げ", "도주", "領頭", "Puntera"),
        "2" to textosCondicion("Pace Chaser", "先行", "선행", "前列", "Perseguidora"),
        "3" to textosCondicion("Late Surger", "差し", "선입", "居中", "Remontadora"),
        "4" to textosCondicion("End Closer", "追込", "추입", "後追", "Rematadora"),
    ),
    /* Ojo: 1 corta, 2 milla, 3 media y 4 larga. "Arena" no es una distancia. */
    "distance_type" to mapOf(
        "1" to textosCondicion("Short", "短距離", "단거리", "短距離", "Corta"),
        "2" to textosCondicion("Mile", "マイル", "마일", "一哩", "Milla"),
        "3" to textosCondicion("Medium", "中距離", "중거리", "中距離", "Media"),
        "4" to textosCondicion("Long", "長距離", "장거리", "長距離", "Larga"),
    ),
    "ground_type" to mapOf(
        "1" to textosCondicion("Turf", "芝", "잔디", "草地", "Césped"),
        "2" to textosCondicion("Dirt", "ダート", "더트", "沙地", "Tierra"),
    ),
    /* El orden del dato es el del juego: 良, 稍重, 重 y 不良. */
    "ground_condition" to mapOf(
        "1" to textosCondicion("Firm", "良", "양호", "良好", "Firme"),
        "2" to textosCondicion("Slightly soft", "稍重", "다소 무거움", "稍重", "Algo blanda"),
        "3" to textosCondicion("Soft", "重", "무거움", "重", "Blanda"),
        "4" to textosCondicion("Heavy", "不良", "불량", "不良", "Pesada"),
    ),
    /* Ojo: 1 sol, 2 nublado, 3 lluvia y 4 nieve. */
    "weather" to mapOf(
        "1" to textosCondicion("Sunny", "晴れ", "맑음", "晴天", "Soleado"),
        "2" to textosCondicion("Cloudy", "曇り", "흐림", "陰天", "Nublado"),
        "3" to textosCondicion("Rainy", "雨", "비", "雨天", "Lluvioso"),
        "4" to textosCondicion("Snowy", "雪", "눈", "雪天", "Nevado"),
    ),
    /* El 5 es la temporada de sakura, que para la skill cuenta como primavera. */
    "season" to mapOf(
        "1" to textosCondicion("Spring", "春", "봄", "春季", "Primavera"),
        "2" to textosCondicion("Summer", "夏", "여름", "夏季", "Verano"),
        "3" to textosCondicion("Autumn", "秋", "가을", "秋季", "Otoño"),
        "4" to textosCondicion("Winter", "冬", "겨울", "冬季", "Invierno"),
        "5" to textosCondicion("Spring (sakura)", "春（桜）", "봄 (벚꽃)", "春季（櫻花）", "Primavera (sakura)"),
    ),
    "rotation" to mapOf(
        "1" to textosCondicion("right", "右", "오른쪽", "右", "derecha"),
        "2" to textosCondicion("left", "左", "왼쪽", "左", "izquierda"),
    ),
    /* El 0 es el rail de dentro ("near the inner rail"); el 2, ir en cabeza. */
    "lane_type" to mapOf(
        "0" to textosCondicion("inner", "内", "안쪽", "內側", "interior"),
    ),
    "straight_front_type" to mapOf(
        "2" to textosCondicion("at the front", "先頭", "선두", "最前", "en cabeza"),
    ),
    /* `furlong` viene con base 0: el 1 es el segundo furlong (No Second
       Thoughts pide "at the second furlong" con furlong==1). Se enseña 1-based. */
    "furlong" to mapOf(
        "0" to textosCondicion("1st", "1本目", "1번째", "第1", "1.º"),
        "1" to textosCondicion("2nd", "2本目", "2번째", "第2", "2.º"),
        "2" to textosCondicion("3rd", "3本目", "3번째", "第3", "3.º"),
        "3" to textosCondicion("4th", "4本目", "4번째", "第4", "4.º"),
    ),
    /* grade==100 es una carrera de G1 ("if it's a G1 or similarly major race"). */
    "grade" to mapOf(
        "100" to textosCondicion("G1", "G1", "G1", "G1", "G1"),
    ),
    /* time==4 es de noche (Night Races ◎: "Increase performance at night"). */
    "time" to mapOf(
        "4" to textosCondicion("night", "夜", "밤", "夜晚", "noche"),
    ),
    /* El -1 es el "sí" de esta bandera ("when the most popular girl activates a skill"). */
    "is_popularity_top_character_activate_advantage_skill" to mapOf(
        "-1" to textosCondicion("yes", "はい", "예", "是", "sí"),
    ),
    /* Escala de cinco pasos, del 1 (fatal) al 5 (perfecta). */
    "motivation" to mapOf(
        "1" to textosCondicion("awful", "絶不調", "최악", "最差", "Fatal"),
        "2" to textosCondicion("bad", "不調", "부진", "不佳", "Mala"),
        "3" to textosCondicion("normal", "普通", "보통", "普通", "Normal"),
        "4" to textosCondicion("good", "好調", "양호", "良好", "Buena"),
        "5" to textosCondicion("great", "絶好調", "최상", "絕佳", "Perfecta"),
    ),
)

private val VALORES: Map<String, Textos> =
    TABLA_VALORES.mapValues { (_, valores) -> Textos(valores) }

/* Claves de fase: el valor es el número de fase y su nombre entra en la frase.
   Sin esto, "phase_random==1" se leía "fase (cualquiera)" —que es justo lo
   contrario de lo que dice, porque la fase media sí importa— y
   "phase_laterhalf_random==0" acababa en "no segunda mitad de la fase". */
private val TABLA_ETIQUETAS_FASE: Map<String, Map<String, String>> = mapOf(
    "phase_random" to textosCondicion("anywhere in the %s", "%sのどこか", "%s 어딘가", "%s任意位置", "en cualquier punto del %s"),
    "phase_firsthalf" to textosCondicion("first half of the %s", "%sの前半", "%s 전반", "%s前半", "primera mitad del %s"),
    "phase_firsthalf_random" to textosCondicion("first half of the %s (any)", "%sの前半のどこか", "%s 전반 어딘가", "%s前半任意位置", "primera mitad del %s (cualquier punto)"),
    "phase_laterhalf" to textosCondicion("second half of the %s", "%sの後半", "%s 후반", "%s後半", "segunda mitad del %s"),
    "phase_laterhalf_random" to textosCondicion("second half of the %s (any)", "%sの後半のどこか", "%s 후반 어딘가", "%s後半任意位置", "segunda mitad del %s (cualquier punto)"),
    "phase_firstquarter_random" to textosCondicion("first quarter of the %s (any)", "%sの最初の1/4のどこか", "%s 첫 1/4 어딘가", "%s前四分之一任意位置", "primer cuarto del %s (cualquier punto)"),
    /* El dato usa las dos grafías: "firsthalf/laterhalf" y "first_half/latter_half". */
    "phase_firstquarter" to textosCondicion("first quarter of the %s", "%sの最初の1/4", "%s 첫 1/4", "%s前四分之一", "primer cuarto del %s"),
    "phase_first_half_straight_random" to textosCondicion("on a straight in the first half of the %s (any)", "%sの前半の直線のどこか", "%s 전반 직선 어딘가", "%s前半直線任意位置", "en una recta de la primera mitad del %s (cualquier punto)"),
    "phase_latter_half_straight_random" to textosCondicion("on a straight in the second half of the %s (any)", "%sの後半の直線のどこか", "%s 후반 직선 어딘가", "%s後半直線任意位置", "en una recta de la segunda mitad del %s (cualquier punto)"),
    "phase_straight_random" to textosCondicion("on a straight in the %s (any)", "%sの直線のどこか", "%s 직선 어딘가", "%s直線任意位置", "en una recta del %s (cualquier punto)"),
    "phase_corner_random" to textosCondicion("on a corner in the %s (any)", "%sのコーナーのどこか", "%s 코너 어딘가", "%s彎道任意位置", "en una curva del %s (cualquier punto)"),
)

private val ETIQUETAS_FASE = Textos(TABLA_ETIQUETAS_FASE)

private val TABLA_FASES: Map<String, Map<String, String>> = mapOf(
    "0" to textosCondicion("Opening Leg", "序盤", "초반", "前期", "tramo inicial"),
    "1" to textosCondicion("Middle Leg", "中盤", "중반", "中期", "tramo intermedio"),
    "2" to textosCondicion("Final Leg", "終盤", "종반", "後期", "tramo final"),
    "3" to textosCondicion("Last Spurt", "ラストスパート", "라스트 스퍼트", "最後衝刺", "arranque final"),
)

private val FASES = Textos(TABLA_FASES)

/* Une dos alternativas de la expresión ("@"). */
private val TABLA_ALTERNATIVA: Map<String, Map<String, String>> = mapOf(
    "alternativa" to textosCondicion("or", "または", "또는", "或", "o"),
)

private val ALTERNATIVA = Textos(TABLA_ALTERNATIVA)

private val OPERADORES = listOf("==", "!=", ">=", "<=", ">", "<")

/* Un término ya legible, si se pudo traducir y si es sólo el separador "o" que
   une dos alternativas (esos no llevan punto). */
data class TerminoCondicion(
    val texto: String,
    val traducido: Boolean,
    val separador: Boolean = false,
)

/* Parte la expresión en términos legibles. Devuelve la lista vacía si no hay
   nada que enseñar.

   La expresión puede traer alternativas separadas por "@": la skill se activa
   si se cumple CUALQUIERA de ellas. Se enseñan seguidas, con un "o" en medio,
   en vez de mezclarlas con los "&", que sí son todos obligatorios. */
fun leerCondiciones(expresion: String?, idioma: String): List<TerminoCondicion> {
    if (expresion.isNullOrBlank()) return emptyList()
    val alternativas = expresion.split('@')
        .map { rama ->
            rama.split('&').mapNotNull { leerTermino(it.trim(), idioma) }.filter { it.texto.isNotBlank() }
        }
        .filter { it.isNotEmpty() }
    if (alternativas.isEmpty()) return emptyList()
    if (alternativas.size == 1) return alternativas.first()
    val salida = mutableListOf<TerminoCondicion>()
    for ((indice, rama) in alternativas.withIndex()) {
        if (indice > 0) salida.add(TerminoCondicion(ALTERNATIVA["alternativa", idioma], true, separador = true))
        salida.addAll(rama)
    }
    return salida
}

private fun leerTermino(termino: String, idioma: String): TerminoCondicion? {
    if (termino.isBlank()) return null
    val operador = OPERADORES.firstOrNull { termino.contains(it) }
        ?: return TerminoCondicion(termino, false)
    val clave = termino.substringBefore(operador).trim()
    val valor = termino.substringAfter(operador).trim()

    if (clave in ETIQUETAS_FASE.claves) {
        if (valor !in FASES.claves) return TerminoCondicion(termino, false)
        if (operador != "==" && operador != "!=") return TerminoCondicion(termino, false)
        val texto = ETIQUETAS_FASE[clave, idioma].replace("%s", FASES[valor, idioma])
        return TerminoCondicion(if (operador == "==") texto else negativo(texto, idioma), true)
    }

    /* "corner_random==3" es una curva concreta, la tercera, no una cualquiera. */
    if (clave == "corner_random") {
        val numero = valor.toIntOrNull() ?: return TerminoCondicion(termino, false)
        if (operador != "==") return TerminoCondicion(termino, false)
        return TerminoCondicion(curva(numero, idioma, cualquiera = true), true)
    }

    if (clave !in ETIQUETAS.claves) return TerminoCondicion(termino, false)
    val etiqueta = ETIQUETAS[clave, idioma]

    /* `corner` es la excepción: 0 y 1 se leen como sí/no ("no en curva"), pero
       un número mayor es la curva enésima ("en la curva 3", "no en la curva 3"). */
    if (clave == "corner" && (operador == "==" || operador == "!=") && valor != "0" && valor != "1") {
        val numero = valor.toIntOrNull() ?: return TerminoCondicion(termino, false)
        val texto = curva(numero, idioma, cualquiera = false)
        return TerminoCondicion(if (operador == "==") texto else negativo(texto, idioma), true)
    }

    val nombreValor = VALORES[clave]?.let { textos ->
        if (valor in textos.claves) textos[valor, idioma] else null
    }
    val texto = when {
        nombreValor != null && operador == "==" -> parEtiquetaValor(etiqueta, nombreValor, idioma, contrario = false)
        nombreValor != null && operador == "!=" -> parEtiquetaValor(etiqueta, nombreValor, idioma, contrario = true)
        clave in NUMERICAS -> "$etiqueta ${simbolo(operador)} $valor"
        valor == "1" && operador == "==" -> etiqueta
        valor == "0" && operador == "==" -> negativo(etiqueta, idioma)
        valor == "1" && operador == "!=" -> negativo(etiqueta, idioma)
        valor == "0" && operador == "!=" -> etiqueta
        else -> "$etiqueta ${simbolo(operador)} $valor"
    }
    return TerminoCondicion(texto, true)
}

/* La negación de un término: prefijo en inglés y español ("no en una curva") y
   sufijo en los otros tres, que es donde suena natural ("不以彎道" sería raro;
   "彎道以外" no). */
private fun negativo(texto: String, idioma: String): String = when (idioma) {
    "ja", "tw" -> "${texto}以外"
    "ko" -> "$texto 아님"
    "es" -> "no $texto"
    else -> "not $texto"
}

/* Un valor con nombre se lee "etiqueta: valor" con "==" y "etiqueta: not valor"
   con "!=" ("etiqueta: no valor" en español). */
private fun parEtiquetaValor(
    etiqueta: String,
    valor: String,
    idioma: String,
    contrario: Boolean,
): String = when (idioma) {
    "ja" -> if (contrario) "$etiqueta：${valor}以外" else "$etiqueta：$valor"
    "tw" -> if (contrario) "$etiqueta：${valor}以外" else "$etiqueta：$valor"
    "ko" -> if (contrario) "$etiqueta: $valor 아님" else "$etiqueta: $valor"
    "es" -> if (contrario) "$etiqueta: no $valor" else "$etiqueta: $valor"
    else -> if (contrario) "$etiqueta: not $valor" else "$etiqueta: $valor"
}

/* "en la curva 3" en cada idioma; el chino y el japonés numeran con 第. */
private fun curva(numero: Int, idioma: String, cualquiera: Boolean): String = when (idioma) {
    "ja" -> if (cualquiera) "第${numero}コーナーのどこか" else "第${numero}コーナー"
    "ko" -> if (cualquiera) "${numero}번째 코너 어딘가" else "${numero}번째 코너"
    "tw" -> if (cualquiera) "第${numero}彎道任意位置" else "第${numero}彎道"
    "es" -> if (cualquiera) "en la curva $numero (cualquiera)" else "en la curva $numero"
    else -> if (cualquiera) "on corner $numero (any)" else "on corner $numero"
}

private fun simbolo(operador: String): String = when (operador) {
    "==" -> "="
    "!=" -> "≠"
    ">=" -> "≥"
    "<=" -> "≤"
    else -> operador
}

/* Todas las filas de las tablas, con un nombre legible delante, para el test que
   exige que cada una esté en los cinco idiomas de la interfaz (no vale caer al
   inglés). */
val FILAS_CONDICIONES: List<Pair<String, Map<String, String>>> =
    TABLA_ETIQUETAS.entries.map { it.key to it.value } +
        TABLA_ETIQUETAS_FASE.entries.map { "fase:${it.key}" to it.value } +
        TABLA_FASES.entries.map { "fase=${it.key}" to it.value } +
        TABLA_VALORES.flatMap { (clave, valores) -> valores.map { (valor, textos) -> "$clave=$valor" to textos } } +
        TABLA_ALTERNATIVA.entries.map { it.key to it.value }
