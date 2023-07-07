package com.andyshon.tiktalk.data.model.settings

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.data.network.request.FirebaseTokenRequest
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import com.andyshon.tiktalk.data.network.request.UserSettingsRequest
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class SettingsModel @Inject constructor(private val service: SettingsService): BaseModel() {

    fun updateSettings(userSettingsRequest: UserSettingsRequest): Single<User> {
        return service.updateSettings(userSettingsRequest)
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun updateUserProfile(userProfileRequest: UserProfileRequest): Single<User> {
        return service.updateProfile(userProfileRequest)
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun updateUserProfilePhotos(images: List<MultipartBody.Part>): Single<Response<User>> {
        return service.updateProfilePhotos(images)
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun resetDislikes(): Completable {
        return service.resetDislikes()
            .checkApiErrorCompletable()
            .applySchedulers()
    }

    fun getUser(id: Int): Single<User> {
        return service.getUser(id)
            .map { it.user }
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun updateFirebaseToken(token: String): Single<FirebaseTokenRequest> {
        return service.updateFirebaseToken(FirebaseTokenRequest(token))
            .checkApiErrorSingle()
            .applySchedulers()
    }
}