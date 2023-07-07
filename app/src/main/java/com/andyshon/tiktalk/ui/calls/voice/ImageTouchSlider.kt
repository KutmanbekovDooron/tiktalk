package com.andyshon.tiktalk.ui.calls.voice

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import com.andyshon.tiktalk.R

class ImageTouchSlider(context: Context, attributeSet: AttributeSet): RelativeLayout(context, attributeSet), View.OnTouchListener {

    private var mImage: ImageView? = null
    private var mScreenWidthInPixel: Int = 0
    private var mScreenWidthInDp: Int = 0
    private var mDensity: Float = 0f

    private val mPaddingInDp: Int = 15

    private var mLengthOfSlider: Int = 0

    interface OnImageSliderChangedListener {
        fun onChanged()
    }

    private var mOnImageSliderChangedListener: OnImageSliderChangedListener? = null

    init {
        createView(context)
    }

    private fun createView(context: Context) {
        val inflater = context.getSystemService (Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.image_touch_slider, this, true)

        mImage = findViewById(R.id.slider)
        mImage?.setOnTouchListener(this)

        val manager = context.getSystemService (Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        mDensity = resources.displayMetrics.density
        val dpWidth = outMetrics. widthPixels / mDensity
        mScreenWidthInPixel = outMetrics.widthPixels
        mScreenWidthInDp = ((mScreenWidthInPixel / mDensity).toInt())

        mLengthOfSlider = (mScreenWidthInDp - mPaddingInDp * 2)
    }


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val layoutParams = v.layoutParams as LayoutParams
        val width = v.width
        val height = v.height
        val xPos = event.rawX
        val yPos = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // You can add some clicked reaction here.
            }
            MotionEvent.ACTION_MOVE -> {
                /*if (xPos < (mScreenWidthInPixel - width - mPaddingInDp * mDensity) && xPos > mPaddingInDp * mDensity) {
                    mOnImageSliderChangedListener?.onChanged()
                    layoutParams.leftMargin = (xPos - width / 2).toInt()
                    mImage?.layoutParams = layoutParams
                }*/


//                Timber.e("height = $height, yPos = $yPos")

//                val layoutParams = view.getLayoutParams() as RelativeLayout.LayoutParams
//                layoutParams.leftMargin = View.X - _xDelta
//                val layoutParams = mImage?.layoutParams as RelativeLayout.LayoutParams
//                layoutParams.bottomMargin = (yPos - height / 2).toInt()//View.Y - _yDelta
//                layoutParams.rightMargin = -250
//                layoutParams.bottomMargin = -250
//                layoutParams.leftMargin = (xPos - width / 2).toInt()
//                layoutParams.bottomMargin +=100


//                layoutParams.bottomMargin = (yPos - height/ 20).toInt()//200
//                mImage?.layoutParams = layoutParams
//
//                invalidate()


                if (/*xPos*/yPos < (mScreenWidthInPixel - /*width*/height - mPaddingInDp * mDensity) && /*xPos*/yPos > mPaddingInDp * mDensity) {
                    mOnImageSliderChangedListener?.onChanged()
//                    layoutParams.leftMargin = (xPos - width / 2).toInt()
//                    layoutParams.topMargin = (/*xPos*/yPos - /*width*/height / 2).toInt()
//                    mImage?.layoutParams = layoutParams
                }
            }
            MotionEvent.ACTION_UP -> {

            }
        }

        return true
    }

    fun setOnImageSliderChangedListener(listener: OnImageSliderChangedListener) {
        mOnImageSliderChangedListener = listener
    }

}