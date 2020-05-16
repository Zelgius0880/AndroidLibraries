package com.zelgius.livedataextensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

data class TaMere(val dummy: String)

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner,  work: (T) -> Unit) {
        observe(lifecycleOwner, object  : Observer<T> {
            override fun onChanged(t: T) {
                work(t)
                removeObserver(this)
            }
        })
}


fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}
