package com.zelgius.livedataextensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

data class TaMere(val dummy: String)

inline fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, crossinline work: (T) -> Unit) {
        observe(lifecycleOwner, object  : Observer<T> {
            override fun onChanged(t: T) {
                work(t)
                removeObserver(this)
            }
        })
}


inline fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}
