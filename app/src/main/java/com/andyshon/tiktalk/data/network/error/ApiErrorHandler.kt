package com.andyshon.tiktalk.data.network.error

import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import io.reactivex.functions.Consumer
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {

    @JvmStatic
    inline fun throwableConsumer(crossinline callback: (Throwable?) -> BaseContract.View?): Consumer<Throwable> {
        return Consumer {
            if (isNetworkError(it)) {
                callback.invoke(it)?.showMessage(R.string.error_internet_connection)
            }
            callback.invoke(it)
        }
    }

    @JvmStatic
    fun isNetworkError(throwable: Throwable?): Boolean = throwable is SocketException
            || throwable is UnknownHostException
            || throwable is SocketTimeoutException
}