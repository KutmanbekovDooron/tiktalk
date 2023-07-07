package com.andyshon.tiktalk.ui.zoneSingle

import com.andyshon.tiktalk.data.entity.ChannelModel

interface ZoneSingleListener {
    fun openSingleChat(item: ChannelModel)
    fun setToolbarStateTapped()
    fun setToolbarStateSimple()
    fun setToolbartNumberCounter(n:Int)
}