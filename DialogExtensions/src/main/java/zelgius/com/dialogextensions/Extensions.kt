package zelgius.com.dialogextensions

import androidx.appcompat.app.AlertDialog


fun AlertDialog.setListeners(positiveListener: (() -> Boolean)? = null, negativeListener: (() -> Boolean)? = null): AlertDialog {

    setOnShowListener {
        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (positiveListener == null) dismiss()
            else if (positiveListener()) dismiss()
        }

        getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            if (negativeListener == null) dismiss()
            else if (negativeListener()) dismiss()
        }
    }

    return this
}