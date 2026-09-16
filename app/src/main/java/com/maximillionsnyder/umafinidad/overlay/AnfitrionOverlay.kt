package com.maximillionsnyder.umafinidad.overlay

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/* Compose necesita dueños de ciclo de vida aunque no haya Activity. */
internal class AnfitrionOverlay : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val registro = LifecycleRegistry(this)
    private val controladorGuardado = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = registro
    override val viewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry get() = controladorGuardado.savedStateRegistry

    fun crear() {
        controladorGuardado.performRestore(null)
        registro.currentState = Lifecycle.State.CREATED
        registro.currentState = Lifecycle.State.RESUMED
    }

    fun destruir() {
        registro.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}
