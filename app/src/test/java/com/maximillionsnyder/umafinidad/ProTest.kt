package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.FuncionPro
import com.maximillionsnyder.umafinidad.data.LicenciaPro
import com.maximillionsnyder.umafinidad.data.funcionDisponible
import com.maximillionsnyder.umafinidad.data.puedeGuardarArboles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/* Licencia Pro: los códigos válidos son los mismos que emite
   `scripts/generar-codigos-pro.mjs`. Si cambia el algoritmo en un lado, estos
   vectores fallan y obligan a actualizar el otro. */
class ProTest {

    @Test
    fun vectoresFijosValidos() {
        val validos = listOf(
            "UMA-AB2C-3D4E-ALWC",
            "UMA-2222-2222-TCQ9",
            "UMA-ZZZZ-ZZZZ-CUWP",
            "UMA-ABCD-EFGH-PSRB",
            /* Cuerpo sin 0/1/I/O: los ambiguos no existen en el alfabeto. */
            "UMA-9876-5432-C84U",
        )
        validos.forEach { assertTrue(it, LicenciaPro.esValida(it)) }
    }

    @Test
    fun firmaDocumentada() {
        assertEquals("ALWC", LicenciaPro.firmaDe("AB2C3D4E"))
        assertEquals("TCQ9", LicenciaPro.firmaDe("22222222"))
        assertEquals("PSRB", LicenciaPro.firmaDe("ABCDEFGH"))
    }

    @Test
    fun toleraMinusculasEspaciosYGuiones() {
        assertTrue(LicenciaPro.esValida("uma-ab2c-3d4e-alwc"))
        assertTrue(LicenciaPro.esValida("  UMA AB2C 3D4E ALWC  "))
        assertTrue(LicenciaPro.esValida("umaab2c3d4ealwc"))
    }

    @Test
    fun formateaEnBloques() {
        assertEquals("UMA-AB2C-3D4E-ALWC", LicenciaPro.formatear("umaab2c3d4ealwc"))
        assertEquals("UMA-AB2C-3D4E-ALWC", LicenciaPro.formatear("UMA-AB2C-3D4E-ALWC"))
        /* Si el largo no da, deja lo normalizado para mostrarlo como error. */
        assertEquals("UMAAB2C", LicenciaPro.formatear("uma-ab2c"))
    }

    @Test
    fun rechazaFirmaAlterada() {
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4E-5F6G"))
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4E-ALWD"))
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4F-ALWC"))
    }

    @Test
    fun rechazaLargoYPrefijo() {
        assertFalse(LicenciaPro.esValida(""))
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4E"))
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4E-ALWC-X"))
        assertFalse(LicenciaPro.esValida("XXX-AB2C-3D4E-ALWC"))
    }

    @Test
    fun rechazaCaracteresFueraDelAlfabeto() {
        /* O, 1 e I no existen en el alfabeto: se descartan al normalizar. */
        assertFalse(LicenciaPro.esValida("UMA-ABCO-3D4E-ALWC"))
        assertFalse(LicenciaPro.esValida("UMA-AB2C-3D4E-ALW1"))
    }

    @Test
    fun generarSiempreDaCodigosValidos() {
        val azar = Random(20260923)
        repeat(500) {
            val codigo = LicenciaPro.generar(azar)
            assertTrue(codigo, LicenciaPro.esValida(codigo))
            assertEquals(codigo, LicenciaPro.formatear(codigo))
            assertEquals(18, codigo.length)
        }
    }

    @Test
    fun generarNoSeRepite() {
        val azar = Random(7)
        val codigos = List(200) { LicenciaPro.generar(azar) }
        assertEquals(200, codigos.toSet().size)
    }

    @Test
    fun todasLasFuncionesProEstanBloqueadasSinLicencia() {
        FuncionPro.values().forEach { funcion ->
            assertTrue(funcion.requierePro)
            assertFalse(funcionDisponible(funcion, esPro = false))
            assertTrue(funcionDisponible(funcion, esPro = true))
        }
        assertFalse(puedeGuardarArboles(esPro = false))
        assertTrue(puedeGuardarArboles(esPro = true))
    }
}
