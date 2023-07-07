package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

import android.view.View
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.Image
import com.andyshon.tiktalk.data.entity.PhotoHolderMetadata
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.item_profile_photo.view.*

class PhotoHolder(val view: View, adapter: SRAdapter<Image>): SRAdapter.SRViewHolder<Image>(view,adapter) {
    override fun bindHolder(photo: Image) {
        view.apply {
            if (photo.url.isNotEmpty()) {
                ivPhoto.loadRoundCornersImage(
                    radius = itemView.context.resources.getDimensionPixelSize(R.dimen.radius_100),
                    url = photo.url
                )
                btnRemovePhoto.show(); ivRemove.show(); btnSmallAddPhoto.hide()
            }
            else {
                if (PhotoHolderMetadata.edit.not()) {
                    ivPhoto.background = itemView.context drawable R.drawable.bg_round_white
                } else {
                    ivPhoto.background = itemView.context drawable R.drawable.bg_round_gray_light
                }
                btnRemovePhoto.hide()
                ivRemove.hide()
                btnSmallAddPhoto.show()
            }

            itemView.setOnClickListener {
                adapter.onItemClick(photo, 0)
            }
            itemView.btnRemovePhoto.setOnClickListener {
                adapter.onItemClick(photo, 1)
            }
        }
    }
}