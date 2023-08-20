package com.andyshon.tiktalk.ui.selectContact

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.network.request.Friend
import com.andyshon.tiktalk.data.network.request.FriendsRequest
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SelectContactPresenter @Inject constructor(val model: AuthModel) :
    BasePresenter<SelectContactContract.View>() {

    var friends: ArrayList<User> = arrayListOf()

    fun getFriends(friendsReq: List<Friend>) {
        val friendsRequest = FriendsRequest(friendsReq)
        model.getFriends(friendsRequest)
            .compose(applyProgressSingle())
            .subscribe({
                Timber.e("Friends size = ${it.size}")
                friends.addAll(it)
                view?.showFriends()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

}