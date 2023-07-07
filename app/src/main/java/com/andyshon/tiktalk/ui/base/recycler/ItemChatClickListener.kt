package com.andyshon.tiktalk.ui.base.recycler

import android.view.View

interface ItemChatClickListener<in M> {
    fun onItemClick(view: View, pos: Int, item: M)
    fun onItemLongClick(view: View, pos: Int, item: M, plusOrMinus: Boolean)
}