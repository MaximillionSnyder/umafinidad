package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.leerCondiciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Los valores esperados de las tablas de traducción no salen del número, salen
   de la descripción oficial de las skills que usan ese valor en GameTora. Por
   eso cada caso lleva al lado la skill de la que se copió: si algún día cambia
   la tabla, el test dice contra qué se contrastó. Las frases se comprueban en
   inglés (el idioma base) y se exige que ja/ko/tw/es digan algo distinto y no
   vacío. */
class CondicionesTest {

    private fun textos(expresion: String, idioma: String = "en") =
        leerCondiciones(expresion, idioma).map { it.texto }

    @Test
    fun expresionVaciaNoDevuelveTerminos() {
        assertTrue(leerCondiciones(null, "en").isEmpty())
        assertTrue(leerCondiciones("", "en").isEmpty())
        assertTrue(leerCondiciones("   ", "en").isEmpty())
        assertTrue(leerCondiciones(null, "ja").isEmpty())
    }

    @Test
    fun porcentajesYPosicionesSeLeenConSimbolo() {
        assertEquals(
            listOf("race progress (%) ≥ 50", "race progress (%) ≤ 60", "position (%) > 50"),
            textos("distance_rate>=50&distance_rate<=60&order_rate>50"),
        )
        assertEquals(listOf("position ≤ 2", "stamina (%) ≥ 30"), textos("order<=2&hp_per>=30"))
        assertEquals(
            listOf("progreso de carrera (%) ≥ 50", "progreso de carrera (%) ≤ 60", "posición (%) > 50"),
            textos("distance_rate>=50&distance_rate<=60&order_rate>50", "es"),
        )
        assertEquals(listOf("posición ≤ 2", "resistencia (%) ≥ 30"), textos("order<=2&hp_per>=30", "es"))
    }

    @Test
    fun clavesDeSiONoSeLeenComoFrase() {
        assertEquals(listOf("on the last straight"), textos("is_last_straight==1"))
        assertEquals(listOf("not on a corner"), textos("corner==0"))
        assertEquals(listOf("on a corner"), textos("corner!=0"))
        assertEquals(listOf("always"), textos("always==1"))
        assertEquals(listOf("en la última recta"), textos("is_last_straight==1", "es"))
        assertEquals(listOf("no en una curva"), textos("corner==0", "es"))
        assertEquals(listOf("siempre"), textos("always==1", "es"))
    }

    @Test
    fun valoresConNombreSeTraducen() {
        assertEquals(listOf("phase: Final Leg"), textos("phase==2"))
        assertEquals(listOf("running style: Pace Chaser"), textos("running_style==2"))
        assertEquals(listOf("ground: Turf"), textos("ground_type==1"))
        assertEquals(listOf("fase: tramo final"), textos("phase==2", "es"))
        assertEquals(listOf("estilo de carrera: Perseguidora"), textos("running_style==2", "es"))
        assertEquals(listOf("superficie: Césped"), textos("ground_type==1", "es"))
    }

    /* Steadfast (200732, innata de Winning Ticket 103501) es "Medium" y su
       condición es distance_type==3: el 3 es MEDIA, no larga. La escala es
       1 corta / 2 milla / 3 media / 4 larga —"arena" no es una distancia—, y
       se comprueba contra las etiquetas (Sprint/Mile/Medium/Long) que GameTora
       escribe en la descripción de cada skill. */
    @Test
    fun laDistanciaUsaLaEscalaDelJuego() {
        assertEquals(listOf("distance: Short"), textos("distance_type==1"))
        assertEquals(listOf("distance: Mile"), textos("distance_type==2"))
        assertEquals(listOf("distance: Medium"), textos("distance_type==3"))
        assertEquals(listOf("distance: Long"), textos("distance_type==4"))
    }

    /* Rainy Days ◎ es weather==3 y Cloudy Days ◎ es weather==2: el 2 es nublado
       y el 3 lluvia, no al revés. */
    @Test
    fun elClimaNoIntercambiaLluviaYNieve() {
        assertEquals(listOf("weather: Sunny"), textos("weather==1"))
        assertEquals(listOf("weather: Cloudy"), textos("weather==2"))
        assertEquals(listOf("weather: Rainy"), textos("weather==3"))
        assertEquals(listOf("weather: Snowy"), textos("weather==4"))
    }

    /* La motivación va de 1 a 5: las skills que piden Motivation "Great" usan
       motivation==5, así que el 5 es la perfecta y el 4 sólo la buena. */
    @Test
    fun laMotivacionTieneCincoPasos() {
        assertEquals(listOf("mood: awful"), textos("motivation==1"))
        assertEquals(listOf("mood: normal"), textos("motivation==3"))
        assertEquals(listOf("mood: good"), textos("motivation==4"))
        assertEquals(listOf("mood: great"), textos("motivation==5"))
    }

    /* Spring Runner ◎ pide season==1@season==5: el 5 es la temporada de sakura,
       que la skill cuenta como primavera. */
    @Test
    fun laQuintaTemporadaSigueSiendoPrimavera() {
        assertEquals(
            listOf("season: Spring", "or", "season: Spring (sakura)"),
            textos("season==1@season==5"),
        )
    }

    /* Las claves de fase llevan el número de fase en el valor. Antes
       phase_random==1 se leía "fase (cualquiera)" (justo lo contrario: la fase
       media importa) y phase_laterhalf_random==0 acababa en "no segunda mitad
       de la fase". Los textos de referencia son los de Fast & Furious (200591,
       "mid-race"), Slick Surge (200602, "early part of late-race") y Calm and
       Collected (200561, "second half of the Opening Leg"). */
    @Test
    fun lasFasesDicenDeQueParteDeLaCarreraSeHabla() {
        assertEquals(listOf("anywhere in the Middle Leg"), textos("phase_random==1"))
        assertEquals(listOf("anywhere in the Opening Leg"), textos("phase_random==0"))
        assertEquals(listOf("anywhere in the Final Leg"), textos("phase_random==2"))
        assertEquals(listOf("first half of the Final Leg (any)"), textos("phase_firsthalf_random==2"))
        assertEquals(listOf("first half of the Last Spurt (any)"), textos("phase_firsthalf_random==3"))
        assertEquals(listOf("second half of the Opening Leg (any)"), textos("phase_laterhalf_random==0"))
        assertEquals(listOf("on a straight in the Final Leg (any)"), textos("phase_straight_random==2"))
        assertEquals(listOf("en cualquier punto del tramo intermedio"), textos("phase_random==1", "es"))
        assertEquals(listOf("segunda mitad del tramo inicial (cualquier punto)"), textos("phase_laterhalf_random==0", "es"))
    }

    /* corner_random==3 es la tercera curva (Beautiful Form, 202521: "on a third
       corner just before the final corner"), y corner==3 también. */
    @Test
    fun lasCurvasConNumeroDicenCualSon() {
        assertEquals(listOf("on corner 3 (any)"), textos("corner_random==3"))
        assertEquals(listOf("on corner 3"), textos("corner==3"))
        assertEquals(listOf("not on corner 3"), textos("corner!=3"))
        assertEquals(listOf("en la curva 3 (cualquiera)"), textos("corner_random==3", "es"))
        assertEquals(listOf("en la curva 3"), textos("corner==3", "es"))
        assertEquals(listOf("no en la curva 3"), textos("corner!=3", "es"))
    }

    /* El "@" separa alternativas: la skill vale si se cumple cualquiera. */
    @Test
    fun lasAlternativasSeSeparanConUnaO() {
        assertEquals(
            listOf(
                "distance: Medium",
                "on corner 3 (any)",
                "or",
                "distance: Long",
                "on corner 3 (any)",
            ),
            textos("distance_type==3&corner_random==3@distance_type==4&corner_random==3"),
        )
        val terminos = leerCondiciones("weather==1@weather==2", "en")
        assertEquals(3, terminos.size)
        assertTrue(terminos[1].separador)
        assertFalse(terminos[0].separador)
        assertEquals(
            listOf(
                "distancia: Media",
                "en la curva 3 (cualquiera)",
                "o",
                "distancia: Larga",
                "en la curva 3 (cualquiera)",
            ),
            textos("distance_type==3&corner_random==3@distance_type==4&corner_random==3", "es"),
        )
    }

    @Test
    fun loQueNoSeConoceQuedaEnCrudoYMarcado() {
        val terminos = leerCondiciones("clave_rara>=3&is_overtake==1", "en")
        assertEquals(2, terminos.size)
        assertFalse(terminos[0].traducido)
        assertEquals("clave_rara>=3", terminos[0].texto)
        assertTrue(terminos[1].traducido)
        assertEquals("when overtaking", terminos[1].texto)
    }

    /* Una rama con una clave desconocida no puede hacer que se pierda la otra:
       se enseña en crudo y marcada, y el separador también se traduce. */
    @Test
    fun unaRamaDesconocidaNoSePierdeEntreLasAlternativas() {
        val terminos = leerCondiciones("clave_rara>=3@weather==1", "en")
        assertFalse(terminos[0].traducido)
        assertEquals("clave_rara>=3", terminos[0].texto)
        assertEquals("or", terminos[1].texto)
        assertEquals("weather: Sunny", terminos[2].texto)
        assertEquals("または", leerCondiciones("clave_rara>=3@weather==1", "ja")[1].texto)
    }

    @Test
    fun elHipodromoYLasClavesNumericasMuestranSuValor() {
        assertEquals(listOf("racetrack = 10008"), textos("track_id==10008"))
        assertEquals(listOf("remaining distance ≥ 600"), textos("remain_distance>=600"))
        assertEquals(listOf("hipódromo = 10008"), textos("track_id==10008", "es"))
        assertEquals(listOf("distancia restante ≥ 600"), textos("remain_distance>=600", "es"))
    }

    /* Claves que salían en crudo y ahora se traducen. Cada texto sale de la
       descripción oficial de una skill que usa ese valor, que va al lado: si
       alguna vez se cambia la etiqueta, el comentario dice contra qué se
       contrastó. El guardián de que no quede NINGUNA en crudo es
       CondicionesDatosTest, que recorre el asset entero. */
    @Test
    fun lasClavesQueFaltabanTambienSeTraducen() {
        // No Second Thoughts: "If there's no one in front of you at the second furlong"
        assertEquals(listOf("furlong: 2nd"), textos("furlong==1"))
        // Inner Post Proficiency: "Increase performance in brackets 1–3"
        assertEquals(listOf("starting bracket ≤ 3"), textos("post_number<=3"))
        // Raise My Soul's Blade!: "if it's a G1 or similarly major race"
        assertEquals(listOf("race grade: G1"), textos("grade==100"))
        // Night Races ◎: "Increase performance at night"
        assertEquals(listOf("time of day: night"), textos("time==4"))
        // Innate Experience: "when near the inner rail on the final corner"
        assertEquals(listOf("lane: inner"), textos("lane_type==0"))
        // Determined Stroke: "in the top 40% for the entire first half of the race"
        assertEquals(listOf("stay in the top 40%"), textos("order_rate_in40_continue==1"))
        // Frenzied Front Runners: "for front runners ... when they become rushed"
        assertEquals(
            listOf("rushed Front Runner rivals ≥ 1"),
            textos("running_style_temptation_opponent_count_nige>=1"),
        )
        // Fervor and Rigor: "if you've activated at least 7 green skills"
        assertEquals(listOf("green skills activated ≥ 7"), textos("activate_count_param_skill>=7"))
        // Collaborative Graded Races: "Increase performance in collaborative graded races"
        assertEquals(listOf("in a dirt graded race"), textos("is_dirtgrade==1"))
        // Los mismos: la condición es que NO estén tentados
        assertEquals(listOf("not rushed"), textos("is_temptation==0"))
    }

    /* El español no está en los datos de GameTora: sus textos son de la interfaz.
       Se comprueba aparte de los cuatro idiomas del asset, con las mismas
       expresiones, que lea los mismos términos y no caiga al inglés. */
    @Test
    fun elEspanolTieneSuPropiaLectura() {
        // No Second Thoughts: "If there's no one in front of you at the second furlong"
        assertEquals(listOf("furlong: 2.º"), textos("furlong==1", "es"))
        // Inner Post Proficiency: "Increase performance in brackets 1–3"
        assertEquals(listOf("cajón de salida ≤ 3"), textos("post_number<=3", "es"))
        // Raise My Soul's Blade!: "if it's a G1 or similarly major race"
        assertEquals(listOf("categoría de la carrera: G1"), textos("grade==100", "es"))
        // Night Races ◎: "Increase performance at night"
        assertEquals(listOf("momento del día: noche"), textos("time==4", "es"))
        // Innate Experience: "when near the inner rail on the final corner"
        assertEquals(listOf("carril: interior"), textos("lane_type==0", "es"))
        // Determined Stroke: "in the top 40% for the entire first half of the race"
        assertEquals(listOf("mantenerse en el primer 40 %"), textos("order_rate_in40_continue==1", "es"))
        // Frenzied Front Runners: "for front runners ... when they become rushed"
        assertEquals(
            listOf("rivales punteras tentadas ≥ 1"),
            textos("running_style_temptation_opponent_count_nige>=1", "es"),
        )
        // Fervor and Rigor: "if you've activated at least 7 green skills"
        assertEquals(listOf("skills verdes activadas ≥ 7"), textos("activate_count_param_skill>=7", "es"))
        // Los mismos: la condición es que NO estén tentados
        assertEquals(listOf("no tentada"), textos("is_temptation==0", "es"))
        // Spring Runner ◎: el 5 es la temporada de sakura, que cuenta como primavera
        assertEquals(
            listOf("estación: Primavera", "o", "estación: Primavera (sakura)"),
            textos("season==1@season==5", "es"),
        )
    }

    /* Con el idioma en ja/ko/tw/es la frase no puede quedarse en inglés ni vacía.
       Se comprueba con las mismas claves y se fijan un par de formas para que un
       cambio de tabla no pase inadvertido. */
    @Test
    fun cadaIdiomaTieneSuPropiaLectura() {
        for (idioma in listOf("ja", "ko", "tw", "es")) {
            val terminos = textos("distance_type==3&corner==0", idioma)
            assertEquals("$idioma no traduce los dos términos", 2, terminos.size)
            assertTrue("$idioma deja un término vacío", terminos.none { it.isBlank() })
            assertTrue("$idioma se queda en inglés: $terminos", terminos.none { "distance:" in it || "corner" in it })
        }
        assertEquals(listOf("距離：中距離", "コーナー以外"), textos("distance_type==3&corner==0", "ja"))
        assertEquals(listOf("거리: 중거리", "코너 아님"), textos("distance_type==3&corner==0", "ko"))
        assertEquals(listOf("距離：中距離", "彎道以外"), textos("distance_type==3&corner==0", "tw"))
        assertEquals(listOf("distancia: Media", "no en una curva"), textos("distance_type==3&corner==0", "es"))
    }

    /* "!=" sobre un valor con nombre se lee como "distinto de" en cada idioma. */
    @Test
    fun elDistintoDeSeLeeEnCadaIdioma() {
        assertEquals(listOf("ground: not Turf"), textos("ground_type!=1"))
        assertEquals(listOf("馬場：芝以外"), textos("ground_type!=1", "ja"))
        assertEquals(listOf("場地：草地以外"), textos("ground_type!=1", "tw"))
        assertEquals(listOf("트랙: 잔디 아님"), textos("ground_type!=1", "ko"))
        assertEquals(listOf("superficie: no Césped"), textos("ground_type!=1", "es"))
    }
}
