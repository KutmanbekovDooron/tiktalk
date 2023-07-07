package com.andyshon.tiktalk.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.preference.Preference

fun EditText.fetchText(): String = this.text.toString().trim()

fun View.invisible() {
    this.visibility = View.INVISIBLE

}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun isOpenKeyboard(view: View) : Boolean {
    val r = Rect()
    view.getWindowVisibleDisplayFrame(r)
    val screenHeight = view.rootView.height
    val keypadHeight = screenHeight - r.bottom
    return keypadHeight > screenHeight * 0.15
}

fun EditText.showKeyboard() {
    val keyboard = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.showSoftInput(this, 0)
}

fun EditText.showKeyboard2() {
    postDelayed({
        val keyboard = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, 20)
}

fun EditText.showKeyboard3() {
    postDelayed({
        requestFocus()
        val keyboard = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, 20)
}

fun EditText.hideKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.hideKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

// Converts dip into its equivalent px

fun convertDipToPx(dip: Float, resources: Resources): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip,
        resources.displayMetrics
    ).toInt()
//    return (resources.displayMetrics.density * dip).toInt()
}

fun getCustomDialogWidthInPx(offsetInPercent: Double, activity: Activity): Int {
    val displayMetrics4 = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics4)

    val width_px = Resources.getSystem().displayMetrics.widthPixels
    val height_px = Resources.getSystem().displayMetrics.heightPixels

    val pixeldpi = Resources.getSystem().displayMetrics.densityDpi

    val mWidth = width_px - width_px * offsetInPercent

//    val width_dp = width_px / pixeldpi * 160
//    val height_dp = height_px / pixeldpi * 160

    return mWidth.toInt()
}

fun setupTheme(context: Context) {
    val sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
    val theme = sharedPref.getString(Preference.KEY_THEME, "default")?:"default"
    when (theme) {
        "default" -> context.setTheme(R.style.AppTheme_Default)
        else -> context.setTheme(R.style.AppTheme_Dark)
    }
}

fun getThemeColor(theme: Resources.Theme, attrColor: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)
    return typedValue.data
}