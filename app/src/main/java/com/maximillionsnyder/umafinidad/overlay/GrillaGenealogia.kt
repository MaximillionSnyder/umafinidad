package com.maximillionsnyder.umafinidad.overlay

/* Filas del layout en dos columnas del panel de la burbuja.

   7 slots: [0] hijo, [1..2] padres, [3..4] abuelos del Padre 1 y [5..6]
   abuelos del Padre 2. El hijo va en una card grande y el resto de a dos por
   fila, agrupado por rama (árbol de pedigrí): la columna izquierda es la línea
   del Padre 1 (1, 3, 4) y la derecha la del Padre 2 (2, 5, 6). */
val FILAS_GENEALOGIA_POR_RAMA: List<List<Int>> = listOf(
    listOf(0),
    listOf(1, 2),
    listOf(3, 5),
    listOf(4, 6),
)
