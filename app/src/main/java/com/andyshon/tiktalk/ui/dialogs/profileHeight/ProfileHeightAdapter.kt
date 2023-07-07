package com.andyshon.tiktalk.ui.dialogs.profileHeight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import kotlinx.android.synthetic.main.item_profile_height.view.*
import kotlin.collections.ArrayList

class ProfileHeightAdapter(
    private var items: ArrayList<HeightItem>,
    private val itemClickListener: ItemClickListener<HeightItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ViewHolder.create(parent)
        holder.itemView.radioButton.setOnClickListener {
            itemClickListener.onItemClick(it, holder.adapterPosition, items[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun selectItem(pos: Int) {
        items.forEach { it.checked = false }
        items[pos].checked = true
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_profile_height, parent, false)
                return ViewHolder(v)
            }
        }

        fun bind(height: HeightItem) {
            itemView.radioButton.text = height.value.toString().plus(" cm")
            itemView.radioButton.isChecked = height.checked
        }
    }
}