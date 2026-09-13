/* Navegación por hash: 5 tabs + overlays archivados (grupos, ranking,
   ranking-padres). El botón atrás del navegador desapila como en Android. */

export type NombreOverlay = 'grupos' | 'ranking' | 'ranking-padres'

export function irA(pagina: number): void {
  window.location.hash = `#/tab/${pagina}`
}

export function irAOverlay(nombre: NombreOverlay): void {
  window.location.hash = `#/${nombre}`
}

export function volver(): void {
  window.history.back()
}
