/* Avatar pixel-art con fallback de iniciales + gradiente HSL.
   Espejo de ui/componentes/Componentes.kt (Avatar) y AvatarResources.kt. */

import { useState } from 'react'
import { useAppStore } from '../state/store'
import { EstiloAvatar } from '../data/prefs'
import { gradienteDeAvatar, inicialesDe } from '../theme/theme'

const CARPETA: Record<EstiloAvatar, string> = {
  [EstiloAvatar.COLOR]: 'color',
  [EstiloAvatar.GRISES]: 'bw5',
  [EstiloAvatar.MONOCROMO]: 'bw1',
}

function avatarUrl(charId: number, estilo: EstiloAvatar): string {
  return `${import.meta.env.BASE_URL}avatars/${CARPETA[estilo]}/${charId}.png`
}

interface AvatarProps {
  id: number
  nombre: string
  tamano?: number
  className?: string
}

export function Avatar({ id, nombre, tamano = 40, className }: AvatarProps) {
  const { estiloAvatar } = useAppStore()
  const [error, setError] = useState(false)

  const estilo: React.CSSProperties = {
    width: tamano,
    height: tamano,
    borderRadius: '50%',
    flexShrink: 0,
  }

  if (error) {
    return (
      <span
        aria-hidden="true"
        className={className}
        style={{
          ...estilo,
          background: gradienteDeAvatar(id),
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontWeight: 700,
          fontSize: Math.max(10, Math.round(tamano * 0.3)),
        }}
      >
        {inicialesDe(nombre)}
      </span>
    )
  }

  return (
    <img
      src={avatarUrl(id, estiloAvatar)}
      alt=""
      aria-hidden="true"
      className={className}
      style={{ ...estilo, objectFit: 'cover' }}
      onError={() => setError(true)}
      loading="lazy"
    />
  )
}
