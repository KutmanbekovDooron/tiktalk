package com.andyshon.tiktalk.ui.auth.chooseCountry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.phone.CountryEmojie
import com.andyshon.tiktalk.utils.rx.RxViewClick
import kotlinx.android.synthetic.main.item_country_code.view.*
import timber.log.Timber

class PhoneAdapter(
    private val list: List<CountryEmojie>,
    private val itemClickListener: ItemClickListener<CountryEmojie>
) : RecyclerView.Adapter<PhoneAdapter.GeneralInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralInfoViewHolder {
        val holder = GeneralInfoViewHolder.create(parent)
        with(holder.itemView) {
            RxViewClick.create(holder.itemView)
                .subscribe({
                    itemClickListener.onItemClick(it, holder.adapterPosition, list[holder.adapterPosition])
                }, Throwable::printStackTrace)
            return holder
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: GeneralInfoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class GeneralInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup?) : GeneralInfoViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_country_code, parent, false)
                return GeneralInfoViewHolder(v)
            }
        }

        fun bind(item: CountryEmojie) {
            Timber.e("CODE = ${item.code}")
            itemView.tvIconWithCode.text = item.isoCode.plus("   ").plus(item.country).plus(" (+").plus(item.code).plus(")")
        }
    }
}