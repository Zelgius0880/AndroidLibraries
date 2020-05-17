package com.zelgius.livedataextensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


/**
 * Observe only once the live data.
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
            work(t)
            removeObserver(this)
        }
    })
}


/**
 * Helper to avoid to create an [Observer] object and can be witten like .observe(this){ ... work ... }. Visually prettier
 */
fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}