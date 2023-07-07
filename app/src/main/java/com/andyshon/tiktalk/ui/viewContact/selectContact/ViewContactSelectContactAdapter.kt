package com.andyshon.tiktalk.ui.viewContact.selectContact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.MobileContact
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import kotlinx.android.synthetic.main.item_select_contact.view.*
import timber.log.Timber
import kotlin.collections.ArrayList

class ViewContactSelectContactAdapter(
    private var items: ArrayList<MobileContact>,
    private val itemClicksListener: ItemClickListener<MobileContact>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var tempRealItems = arrayListOf<MobileContact>()
    init {
        tempRealItems.addAll(items)
    }

    var names = arrayListOf<String>()
    private var selectedMode = false

    fun search(query: String) {
        Timber.e("Search query = $query, size all = ${tempRealItems.size}")
        val list = arrayListOf<MobileContact>()
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

    fun selectedModeOff() {
        items.forEach {
            it.isChecked = false
        }
        names.clear()
        selectedMode = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (items[position].isChecked.not()) {
                names.add(items[position].name)
            }
            else {
                if (names.contains(items[position].name)) {
                    val element = items[position].name
                    names.remove(element)
                }
            }
            items[position].isChecked = items[position].isChecked.not()
            itemClicksListener.onItemClick(it, position, items[position])
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_select_contact, parent, false)
                return ViewHolder(v)
            }
        }

        fun bind(contact: MobileContact) {
//            itemView.ivAvatar.load(itemView.context, contact.photo, callback = { }, failed = { })
            /*if (contact.photo != null) {
                itemView.ivAvatar.setImageBitmap(contact.photo)
            }
            else {
                val bmp = itemView.context.resources.getDrawable(R.drawable.ic_tik_talk_logo)
                itemView.ivAvatar.setImageBitmap(null)
                itemView.ivAvatar.setImageDrawable(bmp)
            }*/
            /*if (user.images.isNotEmpty()) {
                itemView.ivAvatar.loadRoundCornersImage(user.images.first().url)
            }*/
            itemView.tvContactName.text = contact.name


            if (contact.isChecked) {
                itemView.layoutChatsListItemRoot.setBackgroundColor(itemView.resources.getColor(R.color.colorLongClickBg))
            }
            else {
                itemView.layoutChatsListItemRoot.setBackgroundColor(itemView.resources.getColor(R.color.colorTransparent))
            }
        }
    }
}