package com.andyshon.tiktalk.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

fun ImageView.load(context: Context, url: String?, placeholderId: Int = -1, callback: (() -> Unit)? = null, failed: ()->Unit) {
    Glide.with(this)
        .asBitmap()
        .apply(RequestOptions().centerCrop())
        .load(url)
        .listener(object : RequestListener<Bitmap> {
            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                this@load.setImageBitmap(resource)
                callback?.invoke()
                return true
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                failed.invoke()
                return true
            }

        })
        .into(this@load)
}
