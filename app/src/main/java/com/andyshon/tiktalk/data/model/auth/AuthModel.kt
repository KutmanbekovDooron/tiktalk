package com.andyshon.tiktalk.data.model.auth

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.data.network.request.FriendsRequest
import com.andyshon.tiktalk.data.network.request.PhoneVerifyRequest
import com.andyshon.tiktalk.data.network.request.RegisterRequest
import com.andyshon.tiktalk.data.network.response.*
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class AuthModel @Inject constructor(private val service: AuthService): BaseModel() {

    /**
     * should be full phone, example +38092132123
     */

    fun verifyPhone(phone: String): Single<VerificationCodeResponse> {
        return service.verifyPhone(PhoneVerifyRequest(phone))
            .checkApiErrorSingle()
            .applySchedulers()
    }


    fun sendSMS(code: String): Single<Response<VerifyCodeResponse>> {
        return service.sendSMS(code)
            .checkApiErrorSingle()
            .applySchedulers()
    }


    fun getEmailStatus(email: String): Single<Any> {
        return service.getEmailStatus(email)
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun getTwilioUser(): Single<TwilioUserResponse> {
        return service.getTwilioUser()
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun getTwilioToken(): Observable<TwilioTokenResponse> {
        return service.getTwilioTokenForUser()
            .checkApiErrorObservable()
            .applySchedulers()
    }

    fun getTwilioVideoToken(): Single<TwilioTokenResponse> {
        return service.getTwilioVideoTokenForUser()
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun logout(): Completable {
        return service.logout()
            .checkApiErrorCompletable()
            .applySchedulers()
    }

    fun deleteAccount(): Completable {
        return service.deleteAccount()
            .checkApiErrorCompletable()
            .applySchedulers()
    }

    fun register(images: ArrayList<MultipartBody.Part>, registerRequest: RegisterRequest): Single<Response<GetUserResponse>> {
        return service.register(
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.email),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.phoneNumber),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.codeCountry),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.name),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.birthDate),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.country),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.city),
            RequestBody.create(MediaType.parse("text/plain"), registerRequest.gender),
            images
        )
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun getFriends(friendsRequest: FriendsRequest): Single<ArrayList<User>> {
        return service.getFriends(friendsRequest)
            .map { it.users }
            .checkApiErrorSingle()
            .applySchedulers()
    }
}