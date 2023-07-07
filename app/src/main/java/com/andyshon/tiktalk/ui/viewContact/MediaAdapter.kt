package com.andyshon.tiktalk.ui.viewContact

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.load
import com.andyshon.tiktalk.utils.rx.RxViewClick
import kotlinx.android.synthetic.main.item_view_contact_media.view.*
import java.io.File

class MediaAdapter(
    private val items: List<String>,
    private val itemClicksListener: ItemClickListener<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ViewHolder.create(parent)
        with(holder.itemView) {
            RxViewClick.create(holder.itemView)
                .subscribe ({
                    itemClicksListener.onItemClick(it, holder.adapterPosition, items[holder.adapterPosition])
                }, Throwable::printStackTrace)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_view_contact_media, parent, false)
                return ViewHolder(v)
            }
        }

        fun bind(media: String) {
            if (media.isNotEmpty()) {
//                itemView.itemMedia.load(itemView.context, media, callback = {}, failed = {})
                itemView.itemMedia.setImageURI(Uri.fromFile(File(itemView.context.cacheDir, media)))

            }
        }
    }
}