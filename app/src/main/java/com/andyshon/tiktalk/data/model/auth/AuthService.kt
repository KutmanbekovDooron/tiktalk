package com.andyshon.tiktalk.data.model.auth

import com.andyshon.tiktalk.data.network.request.FriendsRequest
import com.andyshon.tiktalk.data.network.request.PhoneVerifyRequest
import com.andyshon.tiktalk.data.network.response.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Multipart

interface AuthService {

    @GET("/v1/phone_verification/verify_code")
    fun sendSMS(@Query("code") code: String): Single<Response<VerifyCodeResponse>>

    @POST("/v1/phone_verification/send_code")
    fun verifyPhone(@Body phoneVerifyRequest: PhoneVerifyRequest): Single<VerificationCodeResponse>

    @GET("/v1/users/status_with_email")
    fun getEmailStatus(@Query("email") email: String): Single<Any>

    @POST("/v1/users/twilio_user")
    fun getTwilioUser(): Single<TwilioUserResponse>

    @POST("/v1/users/twilio_token")
    fun getTwilioTokenForUser(): Observable<TwilioTokenResponse>

    @POST("/v1/video_calls")
    fun getTwilioVideoTokenForUser(): Single<TwilioTokenResponse>

    @DELETE("/v1/users/sign_out")
    fun logout(): Completable

    @DELETE("/v1/users/delete_account")
    fun deleteAccount(): Completable

    @Multipart
    @POST("/v1/users/auth")
    fun register(
        @Part("email") email: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part("code_country") code_country: RequestBody,
        @Part("name") name: RequestBody,
        @Part("birth_date") birth_date: RequestBody,
        @Part("country") country: RequestBody,
        @Part("city") city: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Single<Response<GetUserResponse>>

    @POST("/v1/users/friends")
    fun getFriends(@Body friendsRequest: FriendsRequest): Single<UsersListResponse>
}