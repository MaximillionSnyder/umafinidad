/* Helper para cálculos asíncronos (worker) con estado reactivo. */

export class Async<T> {
  datos = $state.raw<T | null>(null)
  cargando = $state(true)
  error = $state(false)
  private fn: () => Promise<T>
  private version = 0

  constructor(fn: () => Promise<T>) {
    this.fn = fn
  }

  async ejecutar(): Promise<void> {
    const version = ++this.version
    this.cargando = true
    this.error = false
    try {
      const valor = await this.fn()
      if (version !== this.version) return
      this.datos = valor
    } catch {
      if (version !== this.version) return
      this.error = true
    } finally {
      if (version === this.version) this.cargando = false
    }
  }
}
