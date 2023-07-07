package com.andyshon.tiktalk.ui.zoneSingle.publicRoom

import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.ui.chatSingle.entity.*
import com.twilio.chat.*
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import ChatCallbackListener
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import com.andyshon.tiktalk.utils.extensions.getTimeForMessageDateClip
import org.jetbrains.anko.longToast
import ChatStatusListener
import com.andyshon.tiktalk.utils.extensions.moreThan24Hours
import java.util.*
import kotlin.collections.ArrayList

private const val BATCH_SIZE = 500

class ZonePublicRoomPresenter @Inject constructor() : BasePresenter<ZonePublicRoomContract.View>(), ChannelListener {

    var chats: ArrayList<CommonMessageObject> = arrayListOf()

    var channelSid = ""
    var channel: Channel? = null
    var checkedItems = 0

    lateinit var currentSelectedMessage: CommonMessageObject


    private var replyJson: JSONObject? = null
    fun buildReplyJson(name: String, text: String) {
        replyJson = JSONObject()
        replyJson?.put("replyName", name)
        replyJson?.put("replyText", text)
    }
    fun resetReplyJson() {
        replyJson = null
    }

    fun removeListener() {
        channel?.removeListener(this@ZonePublicRoomPresenter)
    }

    fun typing() {
        channel?.typing()
    }

    fun getUsernameByEmail(email: String): String {
        val attrs = channel?.attributes
        Timber.e("attrs object 1 = $attrs")
        if (attrs != null && attrs.has("usersIds")) {
            val usersEmails = attrs.getJSONArray("usersEmails")
            val usersNames = attrs.getJSONArray("usersNames")

            for(i in 0 until usersEmails.length()) {
                val email1 = usersEmails.getString(i)
                if (email == email1) {
                    return usersNames.getString(i)
                }
            }
        }
        return ""
    }

    fun copyToClipboard(context: Context) {
        val label = if (checkedItems == 1) "Message copied"
        else "Copied $checkedItems messages"

        val listSelections = arrayListOf<String>()
        chats.forEach {
            if (it.isChecked) {
                val attrs = it.message.attributes
                Timber.e("atr = $attrs")
                val userName = attrs.getString("userName")
                val timeStamp = getTimeForMessageDateClip(it.message.dateCreatedAsDate.toString())
                val text = timeStamp.plus("\n")
                    .plus("$userName: ")
                    .plus(it.message.messageBody.trim())
                listSelections.add(text)
            }
        }
        val sb = StringBuilder()
        listSelections.forEach {
            sb.append(it.plus("\n"))
        }
        Timber.e("In Clipboard: $sb")


        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label, sb.toString())
        clipboard?.primaryClip = clip
        context.longToast(label)
        view?.onCopied()
    }

    fun deleteSelectedMessages() {
        for (i in (0..chats.lastIndex).reversed()) {
            if (chats[i].isChecked) {
                if (chats[i].message.hasMedia()) {
                    val file = File(getActivityContext().cacheDir, chats[i].message.media.sid)
                    if (file.exists()) {
                        val deleted = file.delete()
                        Timber.e("File ${chats[i].message.media.sid} was deleted = $deleted")
                    }
                }

                channel?.messages?.removeMessage(chats[i].message, ChatStatusListener {
//                    adapter.notifyDataSetChanged()
                })
            }
        }
    }


    fun deleteOldMessages() {
        view?.hideRecycler(true)
        var counter = 0
        for (i in (0..chats.lastIndex).reversed()) {
            val msg = chats[i].message
            if (moreThan24Hours(Date(), msg.dateCreatedAsDate)) {
                counter++
                channel?.messages?.removeMessage(msg, ChatStatusListener {
                    Timber.e("deleted message: ${msg.messageBody}, counter = $counter")
                    counter--
                    if (counter == 0) {
                        view?.hideRecycler(false)
                        view?.onMessagesLoaded()
                    }
                })
            }
        }
        if (counter == 0) {
            Timber.e("show messages")
            view?.hideRecycler(false)
            view?.onMessagesLoaded()
        }
    }


    fun loadAndShowMessages() {
        TwilioSingleton.instance.chatClient?.channels?.getChannel(channelSid, object: CallbackListener<Channel> () {
            override fun onSuccess(ch: Channel?) {

                this@ZonePublicRoomPresenter.channel = ch
                channel?.addListener(this@ZonePublicRoomPresenter)

                chats.clear()

                val members = channel?.members?.membersList
                members?.forEach {
                    if (it.identity != UserMetadata.userEmail) {
                        it.lastConsumedMessageIndex?.let {
                            UserMetadata.lastCMI = it
                        }
                    }
                }

                val messages = channel?.messages
                messages?.getLastMessages(BATCH_SIZE, object: CallbackListener<List<Message>> () {
                    override fun onSuccess(list: List<Message>?) {
                        Timber.e("messages size = ${list?.size}")

                        if (list!!.isEmpty().not()) {
                            channel?.getUnconsumedMessagesCount(object: CallbackListener<Long>() {
                                override fun onSuccess(it: Long?) {
                                    Timber.e("getUnconsumedMessagesCount = $it")
                                    messages.setAllMessagesConsumedWithResult(ChatCallbackListener<Long> { unread ->
                                        Timber.e("$unread messages still unread")
                                    })

                                    var num = it!!
                                    list.forEach { msg ->
                                        Timber.e("Message: ${msg.author}, ${msg.messageBody}, hasMedia = ${msg.hasMedia()}, ${msg.messageIndex}")

                                        if (num > 0) {
                                            val pos = list.indexOf(msg)
                                            if (pos == (list.size-num).toInt()) {
                                                if (list[pos].author != UserMetadata.userEmail) {   // add horizon only if messages come from opponent
                                                    chats.add(UnConsumedHorizonObject(msg))
                                                }
                                                num = 0
                                            }
                                        }

                                        createTypeMessage(msg, list.indexOf(msg))
                                    }
//                                    view?.onMessagesLoaded()
                                    deleteOldMessages()
                                }

                                override fun onError(errorInfo: ErrorInfo?) {
                                    Timber.e("OnError 1 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                                }
                            })
                        }
                    }
                })
            }
            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("OnError 2 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
            }
        })
    }


    private fun createTypeMessage(msg: Message?, pos: Int=0) {
        Timber.e("createTypeMessage called")
        if (msg != null) {
            when {
                msg.attributes.has("replyName") -> chats.add(ReplyObject(msg = msg))
                msg.attributes.has("contactName") -> chats.add(ContactObject(msg = msg))
                else -> {
                    if (msg.hasMedia().not()) {
                        chats.add(MessageObject(msg = msg))
                    }
                    else {
                        Timber.e("Media type: ${msg.media.type}, msg.media.sid = ${msg.media.sid}, attributes = ${msg.attributes}")
                        // Media type: application/pdf

                        if (msg.media.type == Constants.Chat.Media.TYPE_VOICE) {
                            val p = Environment.getExternalStorageDirectory().absolutePath + "/" + msg.media.sid
                            val file = File(p)
                            var bytesArray = byteArrayOf()
                            if (file.exists() && file.length() != 0L) {
                                bytesArray = getByteArrayFromFile(file)
                            }
                            chats.add(VoiceObject(msg = msg, path = p, bytesArray = bytesArray, duration = 0, progress = 0f))
                        }
                        else if (msg.media.type == Constants.Chat.Media.TYPE_FILE) {
                            if (msg.attributes != null) {
                                if (msg.attributes.has("fileUri")) {
//                                    val uri = msg.attributes?.getString("fileUri") ?: "-"
                                    Timber.e("fileUri = ${msg.attributes?.getString("fileUri") ?: "-"}")
                                }
                            }
                            Timber.e("YEESSSS ${msg?.attributes}")
                            chats.add(FileObject(msg = msg))
                        }
                        else if (msg.media.type == Constants.Chat.Media.TYPE_MUSIC) {
                            if (msg.attributes != null) {
                                if (msg.attributes.has("musicName")) {
                                    val musicName = msg.attributes.getString("musicName") ?: "-"
                                    val musicTitle = msg.attributes.getString("musicTitle") ?: "-"
                                    val musicDuration = msg.attributes.getString("musicDuration") ?: "-"
                                    val musicUri = msg.attributes.getString("musicUri") ?: "-"
                                    val musicSizeInBytes = msg.attributes.getLong("musicSizeInBytes")

                                    Timber.e("MUSIC, musicUri = $musicUri")

                                    val projection = arrayOf(MediaStore.Audio.Media.DATA)
                                    val cursor = getActivityContext().contentResolver.query(Uri.parse(musicUri), projection, null, null, null)
                                    Timber.e("MUSIC, cursor = $cursor, ${cursor.columnCount}, ${cursor.count}")

                                    try {
                                        if (cursor.moveToFirst()) {
                                            Timber.e("MUSIC, cursor.moveToFirst()")
                                            val filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.DATA) ?: 0)
                                            Timber.e("filePath === $filePath")
                                            chats.add(MusicObject(filePath, musicName, musicTitle, musicDuration, musicSizeInBytes = musicSizeInBytes, msg = msg))
                                        }
                                        else {
                                            Timber.e("MUSIC, Else 1")
//                                                Timber.e("filePath === $filePath")
                                            chats.add(MusicObject("", musicName, musicTitle, musicDuration, musicSizeInBytes = musicSizeInBytes, msg = msg))
                                        }
                                    } catch (e: Exception) {
                                        Timber.e("Failed to upload media -> error: ${e.message}")
                                    } finally {
                                        cursor.close()
                                    }
                                }
                            }
                        }
                        else if (msg.media.type == Constants.Chat.Media.TYPE_VIDEO) {
                            if (msg.attributes != null) {
                                if (msg.attributes.has("videoName")) {
                                    val videoName = msg.attributes.getString("videoName") ?: "-"
                                    val videoTitle = msg.attributes.getString("videoTitle") ?: "-"
                                    val videoDuration = msg.attributes.getString("videoDuration") ?: "-"
                                    val videoUri = msg.attributes.getString("videoUri") ?: "-"
                                    val videoSizeInBytes = msg.attributes.getLong("videoSizeInBytes")

                                    Timber.e("VIDEO, videoUri = $videoUri")

                                    val projection = arrayOf(MediaStore.Video.Media.DATA)
                                    val cursor = getActivityContext().contentResolver.query(Uri.parse(videoUri), projection, null, null, null)
                                    Timber.e("VIDEO, cursor = $cursor, ${cursor.columnCount}, ${cursor.count}")

                                    try {
                                        if (cursor.moveToFirst()) {
                                            Timber.e("VIDEO, cursor.moveToFirst()")
                                            val filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.DATA) ?: 0)
                                            Timber.e("filePath === $filePath")
                                            chats.add(VideoObject(filePath, videoName, videoTitle, videoDuration, musicSizeInBytes = videoSizeInBytes, msg = msg))
                                        }
                                        else {
                                            Timber.e("VIDEO, Else 1")
                                            chats.add(VideoObject("", videoName, videoTitle, videoDuration, musicSizeInBytes = videoSizeInBytes, msg = msg))
                                        }
                                    } catch (e: Exception) {
                                        Timber.e("Failed to upload media -> error: ${e.message}")
                                    } finally {
                                        cursor.close()
                                    }
                                }
                            }
                        }
                        else {
                            val file = File(getActivityContext().cacheDir, msg.media.sid)
                            if (file.exists().not() || file.length() == 0L) {
                                chats.add(ImageObject(msg = msg, hideProgress = false))
                            }
                            else {
                                chats.add(ImageObject(msg = msg, hideProgress = true))
                            }
                        }


                        if (msg.media.type == Constants.Chat.Media.TYPE_VOICE ||
                            msg.media.type == Constants.Chat.Media.TYPE_IMAGE ||
                            msg.media.type == Constants.Chat.Media.TYPE_CAMERA) {

                            val file = File(getActivityContext().cacheDir, msg.media.sid)
                            Timber.e("is file exist = $file, ${file.exists()}, length = ${file.length()}")

                            if (file.exists().not() || file.length() == 0L) {
                                val outStream = FileOutputStream(file)
                                msg.media.download(outStream, object: StatusListener() {
                                    override fun onSuccess() {}
                                    override fun onError(errorInfo: ErrorInfo?) { Timber.e("download, onError = $errorInfo") }
                                }, object: ProgressListener() {
                                    override fun onStarted() {}
                                    override fun onProgress(p0: Long) { Timber.e("onProgress, $p0") }
                                    override fun onCompleted(p0: String?) {
                                        Timber.e("onCompleted, pos = $pos, p0 = $p0")

                                        val p = Environment.getExternalStorageDirectory().absolutePath + "/" + msg.media.sid
                                        val file2 = File(p)
                                        var bytesArray = byteArrayOf()
                                        if (file2.exists() && file2.length() != 0L) {
                                            bytesArray = getByteArrayFromFile(file2)
                                        }
                                        if (msg.media.type == Constants.Chat.Media.TYPE_VOICE) {
                                            val voice = VoiceObject(msg = msg, path = p, bytesArray = bytesArray, duration = 0, progress = 0f)
                                            chats.set(/*pos*/chats.size-1, voice)
                                        }
                                        else {
                                            val obj = ImageObject(msg = msg, hideProgress = true)
                                            chats.set(/*pos*/chats.size-1, obj)
                                        }


                                        view?.notifyItem(/*pos*/chats.size-1)
                                    }
                                })
                            }
                        }
                    }
                }
            }
//            view?.onMessageAdded(msg)
        }
    }

    /**
     * Fetch bytesArray for voice waveform
     * */
    private fun getByteArrayFromFile(file: File): ByteArray {
        val bytesArray = ByteArray(file.length().toInt())   //init array with file length
        val fis = FileInputStream(file)
        fis.read(bytesArray)    //read file into bytes[]
        fis.close()
        return bytesArray
    }







    fun sendTextMessage(text: String) {
        val options = Message.options()
        options.withBody(text)
        Timber.e("replyJson $replyJson")
        if (replyJson != null) {
            replyJson?.put("userId", UserMetadata.userId)
            replyJson?.put("userName", UserMetadata.userName)
            replyJson?.put("userEmail", UserMetadata.userEmail)
            replyJson?.put("userPhoto", UserMetadata.photos.first().url)
            replyJson?.put("userPhone", UserMetadata.userPhone)
            options.withAttributes(replyJson)
        }
        else {
            val json = JSONObject()
            json.put("userId", UserMetadata.userId)
            json.put("userName", UserMetadata.userName)
            json.put("userEmail", UserMetadata.userEmail)
            json.put("userPhoto", UserMetadata.photos.first().url)
            json.put("userPhone", UserMetadata.userPhone)
            options.withAttributes(json)
        }


        channel?.messages?.sendMessage(options, object : CallbackListener<Message>() {
            override fun onSuccess(p0: Message?) {
                Timber.e("sendMessage, onSuccess: ${p0?.messageBody}, author = ${p0?.author}, ${p0?.channel?.friendlyName}, ${p0?.channelSid}, ${p0?.dateCreated}, ${p0?.dateCreatedAsDate}")
            }
            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("sendMessage, onError: ${errorInfo?.status}, ${errorInfo?.message}, ${errorInfo?.code}")
            }
        })
    }



    override fun onMessageAdded(msg: Message?) {
        Timber.e("onMessageAdded, ${msg?.messageBody}, ${msg?.type}, ${msg?.media}, ${msg?.author}, ${msg?.sid}, ${msg?.messageBody}, ${msg?.messageIndex}")


        val members = channel?.members?.membersList
        Timber.e("onMessageAdded, Members = ${members?.size}")

        members?.forEach {
            Timber.e("onMessageAdded, Member = ${it.identity}, ${it.channel}, ${it.sid}, ${it.lastConsumedMessageIndex}, ${it.lastConsumptionTimestamp}")
            if (it.identity != UserMetadata.userEmail) {
                it.lastConsumedMessageIndex?.let {
                    UserMetadata.lastCMI = it
                }
            }
        }

        channel?.messages?.setAllMessagesConsumedWithResult(ChatCallbackListener<Long> { unread ->
            Timber.e("$unread messages still unreaddd!")
        })

        createTypeMessage(msg)
        view?.onMessageAdded(msg)
    }

    override fun onMessageDeleted(message: Message?) {
        Timber.e("onMessageDeleted ${message?.author}, ${message?.messageBody}")
        var pos = -1
        chats.forEach {
            if (it.message.sid == message?.sid) {
                pos = chats.indexOf(it)
                return@forEach
            }
        }
        if (pos != -1) {
            chats.removeAt(pos)
            view?.onMessageDeleted(pos)
        }
    }

    override fun onMessageUpdated(p0: Message?, p1: Message.UpdateReason?) {
        Timber.e("onMessageUpdated ${p0?.author}, ${p0?.messageBody}")
    }

    override fun onTypingStarted(p0: Channel?, member: Member?) {
        view?.onTypingStarted(member)
    }

    override fun onTypingEnded(p0: Channel?, member: Member?) {
        view?.onTypingEnded(member)
    }

    override fun onMemberAdded(p0: Member?) {
        Timber.e("onMemberAdded ${p0?.channel}, ${p0?.identity}")
    }

    override fun onMemberDeleted(p0: Member?) {
        Timber.e("onMemberDeleted ${p0?.channel}, ${p0?.identity}")
    }

    override fun onMemberUpdated(p0: Member?, reason: Member.UpdateReason?) {
        Timber.e("onMemberUpdated ${p0?.identity}, lastConsumedMessageIndex = ${p0?.lastConsumedMessageIndex},Member.UpdateReason = ${reason?.value}")
        p0?.let {
            if (it.identity != UserMetadata.userEmail) {
                if (reason == Member.UpdateReason.LAST_CONSUMED_MESSAGE_INDEX) {
                    it.lastConsumedMessageIndex?.let {
                        UserMetadata.lastCMI = it
                    }
                    view?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onSynchronizationChanged(p0: Channel?) {
        Timber.e("onSynchronizationChanged ${p0?.friendlyName}")
    }
}