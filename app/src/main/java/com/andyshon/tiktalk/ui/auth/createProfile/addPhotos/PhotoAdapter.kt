package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

import android.content.Context
import android.util.Pair
import androidx.annotation.LayoutRes
import com.andyshon.tiktalk.data.entity.Image

class PhotoAdapter(context: Context, @LayoutRes val layoutResId: Int,
                   private val holderClass:Class<out SRAdapter.SRViewHolder<Image>>,
                   items: List<Image> = mutableListOf(),
                   private val ignoreExtraHolders:Boolean = false,
                   listener: SRListener<Image>): SRAdapter<Image>(context,items,listener) {

    override fun getHolderType(`object`: Image): Pair<Class<out SRAdapter.SRViewHolder<Image>>, Int> {
        return Pair(holderClass, layoutResId)
    }

    override fun addPreholder(preHolder: Pair<Class<out SRAdapter.SRViewHolder<Image>>, Int>?) {
        if (ignoreExtraHolders)return
        super.addPreholder(preHolder)
    }
}