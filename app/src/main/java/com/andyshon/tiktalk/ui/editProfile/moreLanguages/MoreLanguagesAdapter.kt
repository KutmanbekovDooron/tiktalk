package com.andyshon.tiktalk.ui.editProfile.moreLanguages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import kotlinx.android.synthetic.main.item_language.view.*
import kotlin.collections.ArrayList

class MoreLanguagesAdapter(
    private var items: ArrayList<MoreLanguagesItem>,
    private val itemClickListener: ItemClickListener<MoreLanguagesItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ViewHolder.create(parent)
        holder.itemView.checkbox.setOnClickListener {
            itemClickListener.onItemClick(it, holder.adapterPosition, items[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun selectItem(pos: Int) {
        items[pos].checked = true
    }

    fun unselectItem(pos: Int) {
        items[pos].checked = false
    }

    fun getSelectedItems(): ArrayList<MoreLanguagesItem> {
        val list = arrayListOf<MoreLanguagesItem>()
        items.forEach {
            if (it.checked) list.add(it)
        }
        return list
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_language, parent, false)
                return ViewHolder(v)
            }
        }

        fun bind(height: MoreLanguagesItem) {
            itemView.checkbox.text = height.value
            itemView.checkbox.isChecked = height.checked
        }
    }
}