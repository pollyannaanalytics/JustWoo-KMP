package com.pollyannawu.justwoo.android.ui.common

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate

/**
 * Ties a [ViewModelStore] to a Decompose component's lifecycle via [InstanceKeeper].
 * The store — and all ViewModels it holds — is cleared when the component is destroyed.
 */
class ComponentViewModelStoreOwner(context: ComponentContext) : ViewModelStoreOwner {

    private val holder = context.instanceKeeper.getOrCreate(::Holder)

    override val viewModelStore: ViewModelStore get() = holder.store

    private class Holder : InstanceKeeper.Instance {
        val store = ViewModelStore()
        override fun onDestroy() { store.clear() }
    }
}
