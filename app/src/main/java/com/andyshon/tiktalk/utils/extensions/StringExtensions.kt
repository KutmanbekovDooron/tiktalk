package com.andyshon.tiktalk.utils.extensions

import android.graphics.Color
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt

fun CharSequence?.isWrongLength(minLength: Int) =
    isNullOrBlank() || this!!.trim().length < minLength

inline fun Spannable?.addClickableSpannable(
    clickableText: String,
    @ColorInt color: Int,
    crossinline onClick: () -> Unit
): Spannable? {

    if (this.isNullOrBlank()) {
        return this
    }
    this!!

    val startPos = indexOf(clickableText)

    setSpan(
        ForegroundColorSpan(color),
        startPos, startPos + clickableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    setSpan(object : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.bgColor = Color.TRANSPARENT
        }

        override fun onClick(p0: View?) {
            onClick()
        }

    }, startPos, startPos + clickableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return this
}

fun Spannable?.addColorSpannable(
    clickableText: String,
    @ColorInt color: Int
): Spannable? {
    if (this.isNullOrBlank()) {
        return this
    }
    this!!
    val startPos = indexOf(clickableText)

    setSpan(
        ForegroundColorSpan(color),
        startPos, startPos + clickableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun String?.regexDots(): String? {
    if (this.isNullOrBlank()) {
        return this
    }

    val regex = "(\\d)(?=(\\d{3})+$)"

    val splittedNum = this?.split("\\.".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
    return if (splittedNum?.size == 2) {
        splittedNum[0].replace(regex.toRegex(), "$1.") + splittedNum[1]
    } else {
        this?.replace(regex.toRegex(), "$1.")
    }
}