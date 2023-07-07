package com.andyshon.tiktalk.ui.zones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.item_zone_list.view.*
import kotlin.collections.ArrayList

class ZoneListAdapter(
    private var zones: ArrayList<PlacesResult>,
    private val itemClicksListener: ItemClickListener<PlacesResult>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ChatsViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            itemClicksListener.onItemClick(it, position, zones[position])
        }
        return holder
    }

    override fun getItemCount(): Int = zones.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ChatsViewHolder).bind(zones[position])
    }

    fun updateUsersCount(pos: Int, count: Int) {
        if (zones[pos].usersCount != count) {
            zones[pos].usersCount = count
            notifyItemChanged(pos)
        }
    }


    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_zone_list, parent, false)
                return ChatsViewHolder(v)
            }
        }

        fun bind(zone: PlacesResult) {
            if (zone.icon.isNotEmpty()) {
                itemView.imageZone.loadRoundCornersImage(
                    radius = itemView.context.resources.getDimensionPixelSize(R.dimen.radius_8),
                    url = zone.icon
                )
            }
            else {
                val bmp = itemView.context.resources.getDrawable(R.drawable.ic_tik_talk_logo)
                itemView.imageZone.setImageBitmap(null)
                itemView.imageZone.setImageDrawable(bmp)
            }
            itemView.tvZoneName.text = zone.name
            itemView.tvUsersCount.text = "Users: ".plus(zone.usersCount.toString())
        }
    }
}