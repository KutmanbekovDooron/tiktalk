package com.andyshon.tiktalk.data.model

import com.andyshon.tiktalk.data.network.error.ApiException
import com.google.gson.JsonParser
import io.reactivex.*
import retrofit2.HttpException
import timber.log.Timber

abstract class BaseModel {
    companion object {

        private const val FIELD_ERRORS = "error"
        private const val FIELD_MESSAGE = "text"

    }

    protected fun <T> Single<T>.checkApiErrorSingle(): Single<T> =
        onErrorResumeNext { throwable ->
            parseHttpExceptionSingle(throwable)
        }

    protected fun Completable.checkApiErrorCompletable(): Completable =
        onErrorResumeNext { throwable ->
            parseHttpExceptionCompletable(throwable)
        }

    protected fun <T> Observable<T>.checkApiErrorObservable(): Observable<T> =
        doOnError { throwable ->
            parseHttpExceptionObservable(throwable)
        }

    private fun parseHttpExceptionObservable(throwable: Throwable): Observable<Any> {
        if (throwable !is HttpException) {
            return Observable.error(throwable)
        }
        val error = getError(throwable)
        if (error != null) {
            return Observable.error(ApiException(error))
        }

        return Observable.error(throwable)
    }

    private fun <T> parseHttpExceptionSingle(throwable: Throwable): Single<T> {
        Timber.e("throwable = $throwable")
        if (throwable is ApiException) {
            return Single.error(throwable)
        }
        if (throwable !is HttpException) {
            return Single.error(throwable)
        }
        val error = getError(throwable)
        Timber.e("error = $error")
        if (error != null) {
            return Single.error(ApiException(error))
        }

        return Single.error(throwable)
    }

    private fun parseHttpExceptionCompletable(throwable: Throwable): Completable {
        if (throwable !is HttpException) {
            return Completable.error(throwable)
        }
        val error = getError(throwable)
        if (error != null) {
            return Completable.error(ApiException(error))
        }
        return Completable.error(throwable)
    }

    private fun getError(throwable: Throwable): String? {
        val parser = JsonParser()
        val body = parser.parse((throwable as HttpException).response().errorBody()?.string()).asJsonObject
        if (body.has(FIELD_ERRORS)) {
//            val messageBody = body.get(FIELD_ERRORS).asJsonObject
//            return messageBody.get(FIELD_MESSAGE).asString
            val messageBody = body.get(FIELD_ERRORS).asString
            return messageBody
        }
        return null
    }
}