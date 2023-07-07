package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

interface SRListener<in M> {
    fun onItemClick(pos: Int, item: M, type: Int)   //type: 1 - open, 2 - delete
}