package com.andyshon.tiktalk.utils.extensions

import android.content.Context
import android.content.res.Resources
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

infix fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)
infix fun Context.dimen(@DimenRes dimenRes: Int) = resources.getDimensionPixelSize(dimenRes)
infix fun Context.string(@StringRes stringRes: Int) = resources.getString(stringRes)!!
fun Context.string(@StringRes stringRes: Int, args: Any) = resources.getString(stringRes, args)!!
fun Context.string(@StringRes stringRes: Int, args: Array<Any>) = resources.getString(stringRes, args)!!
infix fun Context.stringArray(@ArrayRes stringRes: Int): Array<out String> = resources.getStringArray(stringRes)!!
infix fun Context.drawable(@DrawableRes drawableRes: Int) = ContextCompat.getDrawable(this, drawableRes)!!

infix fun Resources.dimen(@DimenRes dimenRes: Int) = this.getDimensionPixelSize(dimenRes)
infix fun Resources.integer(@IntegerRes integerRes: Int) = this.getInteger(integerRes)
infix fun Resources.string(@StringRes stringRes: Int) = this.getString(stringRes)!!

infix fun Fragment.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this.context!!, colorRes)
infix fun Fragment.dimen(@DimenRes dimenRes: Int) = resources.getDimensionPixelSize(dimenRes)
infix fun Fragment.string(@StringRes stringRes: Int) = resources.getString(stringRes)!!
infix fun Fragment.drawable(@DrawableRes drawableRes: Int) = ContextCompat.getDrawable(this.context!!, drawableRes)!!