package zelgius.com.view_helper_extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import androidx.viewbinding.ViewBinding


val ViewBinding.context
    get() = this.root.context!!


var TextInputLayout.text
    get() = editText?.text?.toString()
    set(value) = editText!!.setText(value)

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm = (Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}


fun Fragment.hideKeyboard() {
    // Check if no view has focus:
    val view = this.requireActivity().currentFocus
    view?.let { v ->
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

fun Fragment.snackBar(text: String, actionText: String = "", length: Int = Snackbar.LENGTH_SHORT, v: View? = null, action: (() -> Unit)? = null) {
    Snackbar.make(v?:view!!, text, length).apply {
        if (action != null) {
            this.setAction(actionText) { action() }
        }
    }.show()
}

fun Fragment.snackBar(@StringRes text: Int, actionText: String = "", length: Int = Snackbar.LENGTH_SHORT, v: View? = null, action: (() -> Unit)? = null) {
    Snackbar.make(v?:view!!, text, length).apply {
        if (action != null) {
            this.setAction(actionText) { action() }
        }
    }.show()
}

fun Int.applyOn(vararg views: View) {
    if (!listOf(View.VISIBLE, View.GONE, View.INVISIBLE).contains(this))
        error("Cannot apply visibility. Must be [View.VISIBLE, View.GONE, View.INVISIBLE]")

    views.forEach {
        it.visibility = this
    }
}
