package com.andyshon.tiktalk.ui.zoneSingle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.UserPreview
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.item_zone_user.view.*
import kotlin.collections.ArrayList

class UsersDropdownAdapter(
    private var items: ArrayList<UserPreview>,
    private val itemClicksListener: ItemClickListener<UserPreview>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ChatsViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            itemClicksListener.onItemClick(it, position, items[position])
        }
        return holder
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ChatsViewHolder).bind(items[position])
    }


    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_zone_user, parent, false)
                return ChatsViewHolder(v)
            }
        }

        fun bind(user: UserPreview) {
            if (user.photo.isNotEmpty()) {
                itemView.userImage.loadRoundCornersImage(
                    radius = itemView.context.resources.getDimensionPixelSize(R.dimen.radius_10),
                    url = user.photo
                )
            }
            else {
                val bmp = itemView.context.resources.getDrawable(R.drawable.ic_tik_talk_logo)
                itemView.userImage.setImageBitmap(null)
                itemView.userImage.setImageDrawable(bmp)
            }
            itemView.tvUserName.text = user.name
        }
    }
}