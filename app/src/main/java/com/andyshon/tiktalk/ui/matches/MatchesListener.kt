package com.andyshon.tiktalk.ui.matches

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.ui.base.BaseFragmentListener

interface MatchesListener: BaseFragmentListener {
    fun openItIsMatch(user: User)
    fun openShare(photo: String)
    fun openMatchesChat()
    fun closeMatchesChat()
    fun openSettings()
}