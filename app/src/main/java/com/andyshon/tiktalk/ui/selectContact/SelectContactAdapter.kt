package com.andyshon.tiktalk.ui.selectContact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.item_select_contact.view.*
import timber.log.Timber
import kotlin.collections.ArrayList

class SelectContactAdapter(
    private var items: ArrayList<User>,
    private val itemClicksListener: ItemClickListener<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var tempRealItems = arrayListOf<User>()
    init {
        tempRealItems.addAll(items)
    }

    fun search(query: String) {
        Timber.e("Search query = $query, size all = ${tempRealItems.size}")
        val list = arrayListOf<User>()
        tempRealItems.forEach {
            if (it.name.contains(query, ignoreCase = true)) {
                list.add(it)
            }
        }
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun showFullList() {
        items.clear()
        items.addAll(tempRealItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            itemClicksListener.onItemClick(
                it,
                holder.adapterPosition,
                items[holder.adapterPosition]
            )
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?): ViewHolder {
                val v = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.item_select_contact, parent, false)
                return ViewHolder(v)
            }
        }

        fun bind(user: User) {
            itemView.tvContactName.text = user.name
            if (user.images.isNotEmpty()) {
                val url = user.images.firstOrNull()?.url ?: ""
                itemView.ivAvatar.loadRoundCornersImage(url)
            }
        }
    }
}