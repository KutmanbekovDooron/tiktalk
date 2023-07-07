package com.andyshon.tiktalk.ui.matches

import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.entity.ChatUser
import com.andyshon.tiktalk.data.model.match.MatchModel
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.showReportUserDialog
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class MatchesPresenter @Inject constructor(private val matchModel: MatchModel) : BasePresenter<MatchesContract.View>() {

    val matchUsers = arrayListOf<ChatUser>()
    var currentUserPointer = 0

    fun getMatchUsers() {
        matchModel.getMatchUsers()
            .compose(applyProgressSingle())
            .subscribe({
                matchUsers.clear()
                if (it.message != null) {
                    view?.noOneNewAroundYou()
                }
                else {
                    Timber.e("Match users size = ${it.users.size}")
                    if (it.users.isNotEmpty()) {
                        currentUserPointer = 0
                        matchUsers.addAll(it.users)
                        view?.onMatchUsersLoaded()
                    }
                }
            }, {
                Timber.e("Error = ${it.message}")
                view?.showMessage(it.message?:"error.")
            })
            .addTo(destroyDisposable)
    }

    fun makeAction(type: String) {
        matchModel.matchAction(matchUsers[currentUserPointer].id, type)
            .compose(applyProgressSingle())
            .subscribe({
                if (currentUserPointer < matchUsers.size-1) {
                    currentUserPointer++
                }
                if (it.user == null) {
                    Timber.e("VIEWWWW = $view, type = $type")
                    when(type) {
                        Constants.MatchTypes.LIKE -> view?.onLiked()
                        Constants.MatchTypes.DISLIKE -> view?.onDisliked()
                        Constants.MatchTypes.SUPERLIKE -> view?.onSuperliked()
                    }
                }
                else {
                    Timber.e("Match message user name = ${it.user.name}")
                    view?.onMatched(it.user)
                }
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun report() {
        view?.let {
            showReportUserDialog(it.getActivityContext()) { reportType ->
                matchModel.report(matchUsers[currentUserPointer].id, reportType)
                    .compose(applyProgressSingle())
                    .subscribe({
                        if (currentUserPointer < matchUsers.size-1) {
                            currentUserPointer++
                        }
                        view?.onReported()
                    }, {
                        Timber.e("Error = ${it.message}")
                    })
                    .addTo(destroyDisposable)
            }
        }
    }

}