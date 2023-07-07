package com.andyshon.tiktalk.di.component

import com.andyshon.tiktalk.di.module.PresentationModule
import com.andyshon.tiktalk.di.scope.PresentationScope
import com.andyshon.tiktalk.ui.MainActivity
import com.andyshon.tiktalk.ui.auth.chooseCountry.ChooseCountryActivity
import com.andyshon.tiktalk.ui.auth.createProfile.CreateProfileActivity
import com.andyshon.tiktalk.ui.auth.createProfile.addPhotos.ProfileAddPhotosActivity
import com.andyshon.tiktalk.ui.auth.createProfile.crop.CropImageActivity
import com.andyshon.tiktalk.ui.auth.signIn.SignInActivity
import com.andyshon.tiktalk.ui.auth.verification.CodeVerificationActivity
import com.andyshon.tiktalk.ui.chatSingle.ChatSingleActivity
import com.andyshon.tiktalk.ui.chatSingle.selectContacts.SelectFragment
import com.andyshon.tiktalk.ui.editProfile.EditProfileActivity
import com.andyshon.tiktalk.ui.editProfile.basicInfo.BasicInfoActivity
import com.andyshon.tiktalk.ui.editProfile.moreLanguages.MoreLanguagesActivity
import com.andyshon.tiktalk.ui.editProfile.userPageWatch.UserPageWatchActivity
import com.andyshon.tiktalk.ui.locker.FingerprintLockerActivity
import com.andyshon.tiktalk.ui.locker.PatternLockerActivity
import com.andyshon.tiktalk.ui.locker.PinLockerActivity
import com.andyshon.tiktalk.ui.messages.MessagesFragment
import com.andyshon.tiktalk.ui.matches.MatchesFragment
import com.andyshon.tiktalk.ui.matches.chat.MatchesChatFragment
import com.andyshon.tiktalk.ui.payments.PaymentSettingsActivity
import com.andyshon.tiktalk.ui.payments.PaymentSettingsPlusActivity
import com.andyshon.tiktalk.ui.zones.ZoneListFragment
import com.andyshon.tiktalk.ui.secretChats.SecretChatsActivity
import com.andyshon.tiktalk.ui.selectContact.SelectContactActivity
import com.andyshon.tiktalk.ui.settings.SettingsActivity
import com.andyshon.tiktalk.ui.settings.pushNotifications.PushNotificationsActivity
import com.andyshon.tiktalk.ui.splash.SplashActivity
import com.andyshon.tiktalk.ui.calls.video.VideoCallActivity
import com.andyshon.tiktalk.ui.viewContact.ViewContactActivity
import com.andyshon.tiktalk.ui.viewContact.selectContact.ViewContactSelectContactActivity
import com.andyshon.tiktalk.ui.calls.voice.VoiceCallActivity
import com.andyshon.tiktalk.ui.zoneSingle.ZoneSingleActivity
import com.andyshon.tiktalk.ui.zoneSingle.privateRoom.ZonePrivateFragment
import com.andyshon.tiktalk.ui.zoneSingle.publicRoom.ZonePublicRoomFragment
import dagger.Subcomponent

@PresentationScope
@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {

    @Subcomponent.Builder
    interface Builder {

        fun presentationModule(module: PresentationModule): Builder
        fun build(): PresentationComponent

    }

    fun inject(chooseCountryActivity: ChooseCountryActivity)
    fun inject(profileAddPhotosActivity: ProfileAddPhotosActivity)
    fun inject(signInActivity: SignInActivity)
    fun inject(codeVerificationActivity: CodeVerificationActivity)
    fun inject(splashActivity: SplashActivity)
    fun inject(createProfileActivity: CreateProfileActivity)
    fun inject(cropImageActivity: CropImageActivity)
    fun inject(chatSingleActivity: ChatSingleActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(selectContactActivity: SelectContactActivity)
    fun inject(secretChatsActivity: SecretChatsActivity)
    fun inject(zoneListFragment: ZoneListFragment)
    fun inject(zoneSingleActivity: ZoneSingleActivity)
    fun inject(zonePublicRoomFragment: ZonePublicRoomFragment)
    fun inject(zonePrivateFragment: ZonePrivateFragment)
    fun inject(viewContactActivity: ViewContactActivity)
    fun inject(matchesFragment: MatchesFragment)
    fun inject(viewContactSelectContactActivity: ViewContactSelectContactActivity)
    fun inject(messagesFragment: MessagesFragment)
    fun inject(matchesChatFragment: MatchesChatFragment)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(editProfileActivity: EditProfileActivity)
    fun inject(basicInfoActivity: BasicInfoActivity)
    fun inject(userPageWatchActivity: UserPageWatchActivity)
    fun inject(pushNotificationsActivity: PushNotificationsActivity)
    fun inject(moreLanguagesActivity: MoreLanguagesActivity)
    fun inject(selectFragment: SelectFragment)
    fun inject(paymentSettingsActivity: PaymentSettingsActivity)
    fun inject(paymentSettingsPlusActivity: PaymentSettingsPlusActivity)
    fun inject(patternLockerActivity: PatternLockerActivity)
    fun inject(fingerprintLockerActivity: FingerprintLockerActivity)
    fun inject(pinLockerActivity: PinLockerActivity)
    fun inject(voiceCallActivity: VoiceCallActivity)
    fun inject(videoCallActivity: VideoCallActivity)

}