package com.andyshon.tiktalk.ui.secretChats

import android.content.Context

private const val SECRET_CHATS_SHARED_PREF = "SECRET_CHATS_SHARED_PREF"

class SecretChatsRepository(
    context: Context
) {
    private val sharedPreferences = context
        .getSharedPreferences(SECRET_CHATS_SHARED_PREF, Context.MODE_PRIVATE)


    fun addNewChatToSecret(sid: String) {
        sharedPreferences.edit().putString(sid, sid).apply()
    }

    fun removeChatInSecrets(sid: String) {
        sharedPreferences.edit().remove(sid).apply()
    }

    fun checkChatIsSecretBySid(sid: String) = sharedPreferences.getString(sid, null) != null
}