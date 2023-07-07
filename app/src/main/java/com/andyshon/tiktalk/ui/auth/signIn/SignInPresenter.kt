package com.andyshon.tiktalk.ui.auth.signIn

import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.network.error.ApiException
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SignInPresenter @Inject constructor(private val model: AuthModel) : BasePresenter<SignInContract.View>() {

    fun sendSms(phone: String) {
        model.verifyPhone(phone)
            .compose(applyProgressSingle())
            .subscribe( {
                view?.onSmsSent()
            }, {
                Timber.e("Phone verify error: $it")
                when (it) {
                    is ApiException -> view?.onError()
                    else -> defaultErrorConsumer
                }
            })
            .addTo(destroyDisposable)
    }


    fun logout() {
        model.logout()
            .subscribe({
                Timber.e("Logout successful")
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }
}