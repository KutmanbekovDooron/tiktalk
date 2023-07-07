package com.andyshon.tiktalk

object Constants {

    /** Key into an Intent's extras data that points to a [Channel] object.  */
    val EXTRA_CHANNEL = "com.twilio.chat.Channel"
    /** Key into an Intent's extras data that contains Channel SID.  */
    val EXTRA_CHANNEL_SID = "C_SID"

    val EXTRA_CHANNEL_OPPONENT_NAME = "c_opponent_name"
    val EXTRA_CHANNEL_OPPONENT_PHOTO = "c_opponent_photo"
    val EXTRA_CHANNEL_OPPONENT_PHONE = "c_opponent_phone"

    val EXTRA_CHANNEL_NAME = "extra_name"
    val EXTRA_CHANNEL_PHOTO = "extra_photo"
    val EXTRA_CHANNEL_PHONE = "extra_phone"

    object Chat {
        const val TIP_1 = "you are hot!"
        const val TIP_2 = "What's up?"
        const val TIP_3 = "Hi, cutie girl"

        object Media {
            const val TYPE_VOICE = "media_type_voice"
            const val TYPE_FILE = "media_type_file"
            const val TYPE_CAMERA = "media_type_camera"
            const val TYPE_IMAGE = "media_type_image"
            const val TYPE_VIDEO = "media_type_video"
            const val TYPE_MUSIC = "media_type_music"
        }
    }

    object ReportTypes {
        const val BAD_OFFLINE_BEHAVIOR = "bad_offline_behavior"
        const val INAPPROPRIATE_PROFILE = "inappropriate_profile"
        const val INAPPROPRIATE_MESSAGES = "inappropriate_messages"
        const val STOLEN_PHOTO = "stolen_photo"
        const val SCAMMER = "scammer"
        const val OTHER = "other"
    }

    object MatchTypes {
        const val LIKE = "like"
        const val DISLIKE = "dislike"
        const val SUPERLIKE = "superlike"
    }

    object Locker {
        const val PATTERN = "pattern"
        const val PIN = "pin"
        const val FINGERPRINT = "fingerprint"
    }

}