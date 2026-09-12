package com.maximillionsnyder.umafinidad.data

import com.maximillionsnyder.umafinidad.R

/* Estilo visual de los avatares pixel-art. */
enum class EstiloAvatar { COLOR, GRISES, MONOCROMO }

fun EstiloAvatar.tituloRes(): Int = when (this) {
    EstiloAvatar.COLOR -> R.string.avatar_color
    EstiloAvatar.GRISES -> R.string.avatar_grises
    EstiloAvatar.MONOCROMO -> R.string.avatar_monocromo
}

fun EstiloAvatar.descripcionRes(): Int = when (this) {
    EstiloAvatar.COLOR -> R.string.avatar_color_desc
    EstiloAvatar.GRISES -> R.string.avatar_grises_desc
    EstiloAvatar.MONOCROMO -> R.string.avatar_monocromo_desc
}
