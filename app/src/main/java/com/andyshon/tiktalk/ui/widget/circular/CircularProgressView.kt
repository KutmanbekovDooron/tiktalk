package com.andyshon.tiktalk.ui.widget.circular

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.andyshon.tiktalk.R

class CircularProgressView : View {

    private var drawable: CircularProgressDrawable? = null
    private var heightAccent = false

    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        var color = Color.BLACK
        var borderWidth = -1

        if (attrs != null) {
            val array = context.obtainStyledAttributes(
                attrs,
                R.styleable.CircularProgressView
            )
            try {
                color = array.getColor(
                    R.styleable.CircularProgressView_cpv_progressColor,
                    color
                )
                borderWidth = array.getDimensionPixelSize(
                    R.styleable.CircularProgressView_cpv_border_width,
                    context.resources.getDimensionPixelSize(R.dimen.base_progress_border_width)
                )
                heightAccent = array.getBoolean(
                    R.styleable.CircularProgressView_cpv_height_accent,
                    heightAccent
                )
            } finally {
                array.recycle()
            }
        }

        drawable = CircularProgressDrawable(color, borderWidth.toFloat())
        drawable!!.callback = this

        if (visibility == View.VISIBLE) {
            drawable!!.start()
        }
    }

    fun setProgressColor(@ColorInt color: Int) {
        if (drawable != null) {
            drawable!!.setColor(color)
        }
    }

    fun setBorderWidth(borderWidth: Int) {
        if (drawable != null) {
            drawable!!.setStrokeWidth(borderWidth.toFloat())
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (drawable == null) {
            return
        }
        if (visibility == View.VISIBLE) {
            drawable!!.start()
        } else {
            drawable!!.stop()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (heightAccent) {
            drawable!!.setBounds(paddingLeft, paddingTop, h - paddingRight, h - paddingBottom)
        } else {
            drawable!!.setBounds(paddingLeft, paddingTop, w - paddingRight, w - paddingBottom)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawable!!.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === drawable || super.verifyDrawable(who)
    }
}