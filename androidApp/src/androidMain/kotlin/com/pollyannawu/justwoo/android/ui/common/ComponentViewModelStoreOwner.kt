package com.pollyannawu.justwoo.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate

class ComponentViewModelStoreOwner(context: ComponentContext) : ViewModelStoreOwner {

    private val holder = context.instanceKeeper.getOrCreate(::Holder)

    override val viewModelStore: ViewModelStore get() = holder.store

    private class Holder : InstanceKeeper.Instance {
        val store = ViewModelStore()
        override fun onDestroy() { store.clear() }
    }
}

private class ViewModelStoreHolder : InstanceKeeper.Instance {
    val store = ViewModelStore()
    override fun onDestroy() = store.clear()
}

@Composable
fun componentViewModelStoreOwner(context: ComponentContext): ViewModelStoreOwner {
    val holder = remember(context) { context.instanceKeeper.getOrCreate { ViewModelStoreHolder() } }
    return remember(holder) { object : ViewModelStoreOwner { override val viewModelStore = holder.store } }
}
