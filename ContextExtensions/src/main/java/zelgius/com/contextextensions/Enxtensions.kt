package zelgius.com.contextextensions

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorRes
import kotlin.math.roundToInt


fun Context.getColor(@ColorRes color: Int, alpha: Float) =
    getColor(color).let{
        Color.argb(
            (Color.alpha(color) * alpha).roundToInt(),
            Color.red(it),
            Color.green(it),
            Color.blue(it))
    }

/**
 *
 * Get the value of dp to Pixel according to density of the screen
 *
 * @receiver Context
 * @param dp Float      the value in dp
 * @return the value of dp to Pixel according to density of the screen
 */
fun Context.dpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
