package com.andyshon.tiktalk.data.model.settings

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.network.request.FirebaseTokenRequest
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import com.andyshon.tiktalk.data.network.request.UserSettingsRequest
import com.andyshon.tiktalk.data.network.response.GetUserResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SettingsService {

    @GET("/v1/users/{id}")
    fun getUser(@Path("id") id: Int): Single<GetUserResponse>

    @DELETE("/v1/user_settings/reset_dislikes")
    fun resetDislikes(): Completable

    @PATCH("/v1/user_settings")
    fun updateSettings(@Body userSettingsRequest: UserSettingsRequest): Single<User>

    @PATCH("/v1/profiles")
    fun updateProfile(@Body userProfileRequest: UserProfileRequest): Single<User>

    @Multipart
    @PATCH("/v1/profiles")
    fun updateProfilePhotos(@Part add_images: List<MultipartBody.Part>): Single<Response<User>>

    @PATCH("/v1/users/firebase_token")
    fun updateFirebaseToken(@Body firebaseTokenRequest: FirebaseTokenRequest): Single<FirebaseTokenRequest>
}