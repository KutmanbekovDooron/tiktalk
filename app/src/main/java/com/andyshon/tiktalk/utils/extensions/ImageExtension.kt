package com.andyshon.tiktalk.utils.extensions

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.andyshon.tiktalk.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import org.jetbrains.anko.longToast

fun ImageView.loadImage(url: String) {
    Glide.with(this).load(url).into(this)
}
fun ImageView.loadImage(@DrawableRes resId: Int) {
    Glide.with(this).load(resId).into(this)
}

fun ImageView.loadRoundCornersImage(
    url: String,
    radius: Int = context.resources.getDimensionPixelSize(R.dimen.radius_0),
    margin: Int = 0,
    cornerType: RoundedCornersTransformation.CornerType = RoundedCornersTransformation.CornerType.ALL
) {
    val transform = RequestOptions().centerCrop()
        .transforms(CenterCrop(), RoundedCornersTransformation(radius, margin, cornerType))
    Glide.with(this).load(url).apply(transform).into(this)//.onLoadFailed(this.context.getDrawable(R.drawable.ic_profile_placeholder))
    this.show()
}

fun ImageView.loadRoundCornersImage(
    uri: Uri,
    radius: Int = context.resources.getDimensionPixelSize(R.dimen.radius_0),
    margin: Int = 0,
    onError: Int = 0,
    cornerType: RoundedCornersTransformation.CornerType = RoundedCornersTransformation.CornerType.ALL
) {
    val transform = RequestOptions().centerCrop()
        .transforms(CenterCrop(), RoundedCornersTransformation(radius, margin, cornerType))
    Glide.with(this).load(uri).error(onError).apply(transform).into(this)
    this.show()
}

fun ImageView.loadRoundCornersImage(
    @DrawableRes resId: Int,
    radius: Int = 45,
    margin: Int = 0,
    cornerType: RoundedCornersTransformation.CornerType = RoundedCornersTransformation.CornerType.ALL
) {
    val transform = RequestOptions().centerCrop()
        .transforms(CenterCrop(), RoundedCornersTransformation(radius, margin, cornerType))
    Glide.with(this).load(resId).apply(transform).into(this)
    this.show()
}

fun ImageView.loadRoundCornersImageWithFallback(
    url: String,
    radius: Int = context.resources.getDimensionPixelSize(R.dimen.radius_100),
    margin: Int = 0,
    cornerType: RoundedCornersTransformation.CornerType = RoundedCornersTransformation.CornerType.ALL
) {
    val transform = RequestOptions().centerCrop()
        .transforms(CenterCrop(), RoundedCornersTransformation(radius, margin, cornerType))
    Glide.with(this.context)
        .load(url)
        .apply(transform)
        .listener(object: RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
//                this@loadRoundCornersImageWithFallback.context.longToast("onLoadFailed, ${e?.causes}")
                Handler().post {
                    this@loadRoundCornersImageWithFallback.loadRoundCornersImage(
                        R.drawable.ic_profile_placeholder,
                        radius = this@loadRoundCornersImageWithFallback.context.resources.getDimensionPixelSize(R.dimen.radius_100)
                    )
                }
                return true
            }
            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
//                this@loadRoundCornersImageWithFallback.context.longToast("onResourceReady")
                this@loadRoundCornersImageWithFallback.setImageDrawable(resource)
                this@loadRoundCornersImageWithFallback.show()
                return true
            }
        })
        .into(this)
}