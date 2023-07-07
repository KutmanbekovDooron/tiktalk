package com.andyshon.tiktalk.ui.editProfile

import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.*
import io.reactivex.rxkotlin.addTo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class EditProfilePresenter @Inject constructor(
    private val model: SettingsModel,
    private val prefs: PreferenceManager
) : BasePresenter<EditProfileContract.View>() {

    private fun updateProfile(request: UserProfileRequest) {
        model.updateUserProfile(request)
            .compose(applyProgressSingle())
            .subscribe({
                view?.updated()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun deletePhotos(pos: Int) {
        val request = UserProfileRequest(deleteImages = arrayListOf(UserMetadata.photos[pos].id))
        updateProfile(request)
    }

    fun addPhotos() {
        val photosMP = arrayListOf<MultipartBody.Part>()

        UserMetadata.photos.forEach {
            if (it.url.isNotEmpty() && it.url.contains("amazonaws").not()) {
                val file = File(it.url)
                if (file.exists()) {
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("add_images[]", file.name, requestFile)
                    photosMP.add(body)
                }
            }
        }

        model.updateUserProfilePhotos(photosMP)
            .compose(applyProgressSingle())
            .subscribe({
                Timber.e("Update photos success! ${it.message()}")
                val user = it.body()
                user?.let {
                    prefs.putObject(Preference.KEY_USER_MAIN_PHOTO, user.images.first().url, String::class.java)
                    UserMetadata.photos = user.images
                }
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun education(education: String) {
        view?.let { view ->
                val request = UserProfileRequest(education = education)
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_EDUCATION, education, String::class.java)
                view.setEducation(education)
        }
    }

    fun work(work: String) {
        view?.let { view ->
            val request = UserProfileRequest(work = work)
            updateProfile(request)

            prefs.putObject(Preference.KEY_USER_WORK, work, String::class.java)
            view.setWork(work)
        }
    }

    fun about(about: String) {
        view?.let { view ->
            val request = UserProfileRequest(aboutYou = about)
            updateProfile(request)

            prefs.putObject(Preference.KEY_USER_ABOUT, about, String::class.java)
            view.setAbout(about)
        }
    }

    fun relationship(status: String) {
        view?.let { view ->
            showRelationshipDialog(view.getActivityContext(), status) { relationshipStatus ->
                val request = UserProfileRequest(relationship = relationshipStatus.toLowerCase().replace(" ", "_"))
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_RELATIONSHIP, relationshipStatus, String::class.java)
                view.setRelationship(relationshipStatus)
            }
        }
    }

    fun sexuality(status: String) {
        view?.let { view ->
            showSexualityDialog(view.getActivityContext(), status) { sexualityStatus ->
                val request = UserProfileRequest(sexuality = sexualityStatus.toLowerCase().replace(" ", "_"))
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_SEXUALITY, sexualityStatus, String::class.java)
                view.setSexuality(sexualityStatus)
            }
        }
    }

    fun height(curHeight: String) {
        view?.let { view ->
            showHeightDialog(view.getActivityContext(), curHeight) { heightStatus ->
                val request = UserProfileRequest(height = heightStatus.toString().toLowerCase())
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_HEIGHT, heightStatus.toString().plus(" cm"), String::class.java)
                view.setHeight(heightStatus)
            }
        }
    }

    fun living(status: String) {
        view?.let { view ->
            showLivingDialog(view.getActivityContext(), status) { livingStatus ->
                val living = when (livingStatus) {
                    getActivityContext() string R.string.living_popup_by_myself -> "by_myself"
                    getActivityContext() string R.string.living_popup_student_residence -> "student_residence"
                    getActivityContext() string R.string.living_popup_with_parents -> "with_parents"
                    getActivityContext() string R.string.living_popup_with_partner -> "with_partner"
                    getActivityContext() string R.string.living_popup_with_housemate -> "with_housemate"
                    else -> "no_answer"
                }
                val request = UserProfileRequest(living = living)
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_LIVING, livingStatus, String::class.java)
                view.setLiving(livingStatus)
            }
        }
    }

    fun children(status: String) {
        view?.let { view ->
            showChildrenDialog(view.getActivityContext(), status) { childrenStatus ->
                val children = when (childrenStatus) {
                    getActivityContext() string R.string.children_popup_grown_up -> "grown_up"
                    getActivityContext() string R.string.children_popup_already_have -> "already_have"
                    getActivityContext() string R.string.children_popup_never -> "never"
                    getActivityContext() string R.string.children_popup_someday -> "someday"
                    else -> "no_answer"
                }
                val request = UserProfileRequest(children = children)//childrenStatus.toLowerCase().replace(" ", "_"))
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_CHILDREN, childrenStatus, String::class.java)
                view.setChildren(childrenStatus)
            }
        }
    }

    fun smoking(status: String) {
        view?.let { view ->
            showSmokingDialog(view.getActivityContext(), status) { smokingStatus ->
                val smoke = when (smokingStatus) {
                    getActivityContext() string R.string.smoking_popup_heavy_smoker -> "heavy_smoker"
                    getActivityContext() string R.string.smoking_popup_hate_smoking -> "hate_smoking"
                    getActivityContext() string R.string.smoking_popup_dont_like -> "dont_like_it"
                    getActivityContext() string R.string.smoking_popup_social_smoker -> "social_smoker"
                    getActivityContext() string R.string.smoking_popup_smoke_occasionally -> "smoke_occasionally"
                    else -> "no_answer"
                }
                val request = UserProfileRequest(smoking = smoke)//smokingStatus.toLowerCase().replace(" ", "_"))
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_SMOKING, smokingStatus, String::class.java)
                view.setSmoking(smokingStatus)
            }
        }
    }

    fun drinking(status: String) {
        view?.let { view ->
            showDrinkingDialog(view.getActivityContext(), status) { drinkingStatus ->
                val drink = when (drinkingStatus) {
                    getActivityContext() string R.string.drinking_popup_drink_socially -> "drink_socially"
                    getActivityContext() string R.string.drinking_popup_drink_socially -> "drink_socially"
                    getActivityContext() string R.string.drinking_popup_dont_drink -> "dont_drink"
                    getActivityContext() string R.string.drinking_popup_against_drinking -> "against_drinking"
                    getActivityContext() string R.string.drinking_popup_drink_a_lot -> "drink_a_lot"
                    else -> "no_answer"
                }
                val request = UserProfileRequest(drinking = drink)
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_DRINKING, drinkingStatus, String::class.java)
                view.setDrinking(drinkingStatus)
            }
        }
    }

    fun zodiac(status: String) {
        view?.let { view ->
            showZodiacDialog(view.getActivityContext(), status) { zodiacStatus ->
                val zodiac = if (zodiacStatus == getActivityContext() string R.string.zodiac_no_answer) "no_answer" else zodiacStatus
                val request = UserProfileRequest(zodiac = zodiac.toLowerCase())
                updateProfile(request)

                prefs.putObject(Preference.KEY_USER_ZODIAC, zodiacStatus, String::class.java)
                view.setZodiac(zodiacStatus)
            }
        }
    }
}