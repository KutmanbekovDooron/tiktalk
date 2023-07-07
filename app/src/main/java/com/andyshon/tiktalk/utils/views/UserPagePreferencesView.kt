package com.andyshon.tiktalk.utils.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.Gravity
import com.andyshon.tiktalk.R
import android.widget.TextView
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.andyshon.tiktalk.utils.extensions.color
import org.jetbrains.anko.attr
import org.jetbrains.anko.lines
import androidx.annotation.ColorInt
import android.util.TypedValue
import androidx.annotation.DrawableRes
import com.andyshon.tiktalk.utils.extensions.drawable
import org.jetbrains.anko.backgroundDrawable

class UserPagePreferencesView(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private var mRoot: LinearLayout? = null
    private var mImage: ImageView? = null
    private var mText: TextView? = null

    var isChecked: Boolean
        get() = field

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.UserPagePreferencesView, 0, 0)
        val titleText = a.getString(R.styleable.UserPagePreferencesView_titleText)
        val valueImage = a.getDrawable(R.styleable.UserPagePreferencesView_myImage)
        isChecked = a.getBoolean(R.styleable.UserPagePreferencesView_isChecked, false)
        a.recycle()

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_color_options, this, true)

        mRoot = getChildAt(0) as LinearLayout
        mImage = mRoot?.getChildAt(0) as ImageView
        mText = mRoot?.getChildAt(1) as TextView
        mText?.lines = 1

        mText?.text = titleText
        mImage?.setImageDrawable(valueImage)

        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorTextGreyDarkOrWhite, typedValue, true)
        @ColorInt val color = typedValue.data
        mImage?.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)

        if (isChecked) {
            select()
        }
        else {
            unselect()
        }
    }

    fun setText(s: String) {
        mText?.text = s
    }

    fun selectIf() {
        if (isChecked) {
            isChecked = false
            mText?.setTextColor(context color R.color.colorBlack)
            mRoot?.attr(R.attr.preferencesViewDisable)
            mImage?.setColorFilter(context color R.color.colorBlack)
        }
        else {
            isChecked = true
            mText?.setTextColor(context color R.color.colorWhite)
            mRoot?.attr(R.attr.preferencesViewEnable)
            mImage?.setColorFilter(context color R.color.colorWhite)
        }
    }

    /*private*/ fun select() {
        isChecked = true
//        mText?.attr(R.attr.colorTextGreyDarkOrWhite)
        mText?.setTextColor(context color R.color.colorWhite)
        mRoot?.attr(R.attr.preferencesViewEnable)
//        mImage?.attr(R.attr.colorTextGreyDarkOrWhite)
        mImage?.setColorFilter(context color R.color.colorWhite)
    }

    /*private */fun unselect() {

        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorTextGreyDarkOrWhite, typedValue, true)
        @ColorInt val color = typedValue.data
//        itemView.tvUserMessage.setTextColor(color)

        isChecked = false
        mText?.attr(R.attr.colorTextGreyDarkOrWhite)
        mRoot?.attr(R.attr.preferencesViewDisable)
        mImage?.attr(R.attr.colorTextGreyDarkOrWhite)
//        mImage?.backgroundTintList =(color)
    }
}