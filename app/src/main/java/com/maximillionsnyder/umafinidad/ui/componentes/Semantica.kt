package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

fun Modifier.headingSemantica(): Modifier = semantics { heading() }
