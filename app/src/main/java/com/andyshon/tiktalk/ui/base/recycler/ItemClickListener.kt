package com.andyshon.tiktalk.ui.base.recycler

import android.view.View

interface ItemClickListener<in M> {
    fun onItemClick(view: View, pos: Int, item: M)
}