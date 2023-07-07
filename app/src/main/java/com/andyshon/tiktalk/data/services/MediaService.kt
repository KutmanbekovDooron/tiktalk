package com.andyshon.tiktalk.data.services

import ChatCallbackListener
import ChatStatusListener
import android.app.IntentService
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.MediaSidDownloadFinishedEvent
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.data.entity.Media
import com.twilio.chat.Channel
import com.twilio.chat.Message
import com.twilio.chat.ProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.anko.longToast
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class MediaService : IntentService(MediaService::class.java.simpleName) {

    companion object {
        var rxEventBus: RxEventBus? = null
        const val EXTRA_MEDIA_URI = "com.twilio.demo.chat.media_uri"
        const val EXTRA_MEDIA_URI_TO_DELETE = "com.twilio.demo.chat.media_uri_to_delete"
        const val EXTRA_MEDIA_FILE_NAME = "com.twilio.demo.chat.media_file_name"
        const val EXTRA_CHANNEL = "com.twilio.demo.chat.media_channel"
        const val EXTRA_MESSAGE_INDEX = "com.twilio.demo.chat.message_index"
        const val EXTRA_UPLOAD_DATA_TYPE = "com.twilio.demo.chat.message_upload_data_type"

//        const val TYPE_FILE = "message_type_file"
//        const val TYPE_IMAGE = "message_type_image"
//        const val TYPE_MUSIC = "message_type_music"
//        const val TYPE_VIDEO = "message_type_video"

        const val EXTRA_ACTION = "com.twilio.demo.chat.media.action"
        const val EXTRA_ACTION_UPLOAD = "com.twilio.demo.chat.media.action_upload"
        const val EXTRA_ACTION_DOWNLOAD = "com.twilio.demo.chat.media.action_download"
    }


    private val coroutineContext = newSingleThreadContext(MediaService::class.java.simpleName)

    override fun onHandleIntent(intent: Intent?) {
        val action = intent?.getStringExtra(EXTRA_ACTION)

        when (action) {
            EXTRA_ACTION_UPLOAD -> upload(intent)
            EXTRA_ACTION_DOWNLOAD -> download(intent) {
                rxEventBus?.post(MediaSidDownloadFinishedEvent(it))
            }
        }
    }

    private fun upload(intent: Intent) {
        val uriString = intent.getStringExtra(EXTRA_MEDIA_URI) ?: throw NullPointerException("Media URI not provided")
        val channel = intent.getParcelableExtra<Channel>(EXTRA_CHANNEL) ?: throw NullPointerException("Channel is not provided")
        val uriToDelete = intent.getStringExtra(EXTRA_MEDIA_URI_TO_DELETE) ?: "-"
        val fileName = intent.getStringExtra(EXTRA_MEDIA_FILE_NAME) ?: ""
        val dataType = intent.getStringExtra(EXTRA_UPLOAD_DATA_TYPE) ?: ""

        Timber.e("Upload, fileName = $fileName")
        if (fileName.isNotEmpty()) Timber.e("-> Upload file")
        else Timber.e("-> Upload other media (camera,gallery,voice)")

        GlobalScope.launch(coroutineContext) {
            val deferred = CompletableDeferred<Unit>()

            val uri = Uri.parse(uriString)
            Timber.e("upload, uri = $uri")  //  content://media/external/images/media/26052

            val file = File(uriString)
            val uri2 = Uri.parse(file.absolutePath)
            Timber.e("is file exists = ${file.exists()}, uri1 = $uri, uri2 = $uri2")

            val cursor: Cursor?

            var proj: Array<String>? = null
            val name: String

            when(dataType) {
                Constants.Chat.Media.TYPE_IMAGE -> {
                    Timber.e("TYPE_ == TYPE_IMAGE")
                    val projection = arrayOf(
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.SIZE
                    )

                    cursor = contentResolver.query(uri, projection, null, null, null)!!

                    val filePath: String
                    try {
                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA) ?: 0)
                            Timber.e("data = $filePath")
                            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE) ?: 0)
                            Timber.e("title  = $title")
                            val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME) ?: 0)
                            Timber.e("name = $name")
                            var fileSizeInBytes = 0L
                            try {
                                val size =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE) ?: 0).toLong()
                                Timber.e("size = $size")
                                fileSizeInBytes = size
                            } catch (e:Exception) {
                                Timber.e("FUCK!")
                            }
                            //  Failed to upload media -> error: cursor.getString(cursor?….Images.Media.SIZE) ?: 0) must not be null
                            Timber.e("Name file path = $filePath")

                            //  Failed to upload media -> error: Can't access /data/data/com.andyshon.tiktalk/cache/ME6a89ab7549df8fba7997fe48513f729a

//                             size
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            Timber.e("Audio fileSizeInBytes = $fileSizeInBytes, fileSizeInKB = $fileSizeInKB, fileSizeInMB = $fileSizeInMB")
                            if (fileSizeInMB > 25) {
                                longToast("Can't Upload. Sorry file size is large.")
                            } else {

                                Timber.e("Name = $name, filePath = $filePath")
                                val type = Constants.Chat.Media.TYPE_IMAGE//contentResolver.getType(uri)
                                val stream = contentResolver.openInputStream(uri)!!

                                val media = Media(filePath, type, stream)

                                val options = Message.options()
                                    .withMediaFileName(media.name)
                                    .withMedia(media.stream, media.type)
                                    .withMediaProgressListener(object : ProgressListener() {
                                        override fun onStarted() = Timber.e("Start media upload")
                                        override fun onProgress(bytes: Long) = Timber.e("Media upload progress - bytes done: ${bytes}")
                                        override fun onCompleted(mediaSid: String) = Timber.e("Media upload completed")
                                    })

                                val json = JSONObject()
                                json.put("fileUri", uri.toString())
                                json.put("fileName", /*fileName*/name)
                                addUserData(json)
                                options.withAttributes(json)

                                TwilioSingleton.instance.chatClient?.channels?.getChannel(channel.sid, ChatCallbackListener<Channel> {
                                    it.messages.sendMessage(options, ChatCallbackListener<Message> {
                                        Timber.e("Media message sent - sid: ${it.sid}, type: ${it.type}")
                                        deferred.complete(Unit)
                                    })
                                })

                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("Failed to upload media -> error: ${e.message}")
                    } finally {
                        cursor.close()
                    }
                }
                Constants.Chat.Media.TYPE_FILE, Constants.Chat.Media.TYPE_CAMERA -> {
                    Timber.e("TYPE_ == TYPE_FILE")
                    if (fileName.isNotEmpty()) {
                        proj = arrayOf(MediaStore.Images.Media.DATA)
//                        proj = arrayOf(MediaStore.Files.FileColumns.DATA)
                    }

                    cursor = contentResolver.query(uri, proj, null, null, null)!!


                    try {
                        if (cursor.moveToFirst()) {
                            name = if (fileName.isNotEmpty()) {
                                cursor.getString(cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) ?: 0)
                            } else {
                                cursor.getString(cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0)
                            }
                            Timber.e("Name = $name")
                            val type = dataType//contentResolver.getType(uri)
                            val stream = contentResolver.openInputStream(uri)!!

                            val media = Media(name, type, stream)

                            val options = Message.options()
                                .withMediaFileName(media.name)
                                .withMedia(media.stream, media.type)
                                .withMediaProgressListener(object : ProgressListener() {
                                    override fun onStarted() = Timber.e("Start media upload")
                                    override fun onProgress(bytes: Long) = Timber.e("Media upload progress - bytes done: ${bytes}")
                                    override fun onCompleted(mediaSid: String) = Timber.e("Media upload completed")
                                })

                            if (fileName.isNotEmpty()) {
                                val json = JSONObject()
                                json.put("fileUri", uri.toString())
                                json.put("fileName", fileName)
                                addUserData(json)
                                options.withAttributes(json)
                            }

                            TwilioSingleton.instance.chatClient?.channels?.getChannel(channel.sid, ChatCallbackListener<Channel> {
                                it.messages.sendMessage(options, ChatCallbackListener<Message> {
                                    Timber.e("Media message sent - sid: ${it.sid}, type: ${it.type}")
                                    val file2 = File(cacheDir, uriToDelete)
                                    Timber.e("File222 = $file2, ${file2.exists()}")
                                    if (file2.exists()) {
                                        val deleted = file2.delete()
                                        Timber.e("is deleted = $deleted")
                                    }
                                    deferred.complete(Unit)
                                })
                            })

                        }
                    } catch (e: Exception) {
                        Timber.e("Failed to upload media -> error: ${e.message}")
                        deferred.completeExceptionally(e)
                    } finally {
                        cursor.close()
                    }
                }
                Constants.Chat.Media.TYPE_MUSIC -> {
                    val projection = arrayOf(
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE
                    )

                    cursor = contentResolver.query(uri, projection, null, null, null)!!

                    var filePath: String
                    try {
                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.DATA) ?: 0)
                            Timber.e("data = $filePath")
                            val title = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.TITLE) ?: 0)
                            Timber.e("title  = $title")
                            val name = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME) ?: 0)
                            Timber.e("name = $name")
                            val album = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM) ?: 0)
                            Timber.e("album = $album")
                            val milliseconds = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.DURATION) ?: 0).toLong()
                            Timber.e("duration = $milliseconds")
                            val hours = (milliseconds / (1000 * 60 * 60))
                            val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
                            val seconds = (milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
                            Timber.e("Audio duration 1 = hours = $hours, minutes = $minutes, seconds = $seconds")

                            val duration = if (hours == 0L)
                                minutes.toString().plus(":").plus(seconds)
                            else
                                hours.toString().plus(":").plus(minutes).plus(":").plus(seconds)
                            Timber.e("Music duration = $duration")

                            filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST) ?: 0)
                            Timber.e("artist = $filePath")
                            val size = cursor.getString(cursor?.getColumnIndex(MediaStore.Audio.Media.SIZE) ?: 0).toLong()
                            Timber.e("size = $size")
                            Timber.e("Name file path = $filePath")

                            val fileSizeInBytes = size
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            Timber.e("Audio fileSizeInBytes = $fileSizeInBytes, fileSizeInKB = $fileSizeInKB, fileSizeInMB = $fileSizeInMB")
                            if (fileSizeInMB > 25) {
                                longToast("Can't Upload. Sorry file size is large.")
                            } else {

                                Timber.e("Name = $name")
                                val type = Constants.Chat.Media.TYPE_MUSIC//contentResolver.getType(uri)
                                val stream = contentResolver.openInputStream(uri)!!

                                val media = Media(name, type, stream)

                                val options = Message.options()
                                    .withMediaFileName(media.name)
                                    .withMedia(media.stream, media.type)
                                    .withMediaProgressListener(object : ProgressListener() {
                                        override fun onStarted() = Timber.e("Start media upload")
                                        override fun onProgress(bytes: Long) = Timber.e("Media upload progress - bytes done: ${bytes}")
                                        override fun onCompleted(mediaSid: String) = Timber.e("Media upload completed")
                                    })

                                val json = JSONObject()
                                json.put("musicUri", uri.toString())
                                json.put("musicSizeInBytes", fileSizeInBytes)
                                json.put("musicName", name)
                                json.put("musicTitle", title)
                                json.put("musicDuration", duration)
                                addUserData(json)
                                options.withAttributes(json)

                                TwilioSingleton.instance.chatClient?.channels?.getChannel(channel.sid, ChatCallbackListener<Channel> {
                                    it.messages.sendMessage(options, ChatCallbackListener<Message> {
                                        Timber.e("Media message sent - sid: ${it.sid}, type: ${it.type}")
                                        deferred.complete(Unit)
                                    })
                                })

                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("Failed to upload media -> error: ${e.message}")
                    } finally {
                        cursor.close()
                    }
                }
                Constants.Chat.Media.TYPE_VIDEO -> {
                    val projection = arrayOf(
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.TITLE,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.ALBUM,
                        MediaStore.Video.Media.ARTIST,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE
                    )
                    cursor = contentResolver.query(uri, projection, null, null, null)!!

                    var filePath: String
                    try {
                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.DATA) ?: 0)
                            Timber.e("data = $filePath")
                            var title = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.TITLE) ?: 0)
                            Timber.e("title 1 = $title")
                            if (title.contains(".")) {
                                title = title.split(".").first()
                            }
                            Timber.e("title 2 = $title")
                            val name = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME) ?: 0)
                            Timber.e("name = $name")
                            val album = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.ALBUM) ?: 0)
                            Timber.e("album = $album")
                            val milliseconds = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.DURATION) ?: 0).toLong()
                            Timber.e("duration = $milliseconds")
                            val hours = (milliseconds / (1000 * 60 * 60))
                            val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
                            val seconds = (milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000
                            Timber.e("Video duration 1 = hours = $hours, minutes = $minutes, seconds = $seconds")

                            val secondsFormatted = if (seconds < 10) "0".plus(seconds) else seconds.toString()

                            val duration = if (hours == 0L)
                                minutes.toString().plus(":").plus(secondsFormatted)
                            else if (hours == 0L && minutes == 0L)
                                "0:".plus(secondsFormatted)
                            else
                                hours.toString().plus(":").plus(minutes).plus(":").plus(secondsFormatted)
                            Timber.e("Video duration = $duration")

                            filePath = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.ARTIST) ?: 0)
                            Timber.e("artist = $filePath")
                            val size = cursor.getString(cursor?.getColumnIndex(MediaStore.Video.Media.SIZE) ?: 0).toLong()
                            Timber.e("size = $size")
                            Timber.e("Name file path = $filePath")

                            val fileSizeInBytes = size
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            Timber.e("Audio fileSizeInBytes = $fileSizeInBytes, fileSizeInKB = $fileSizeInKB, fileSizeInMB = $fileSizeInMB")
                            if (fileSizeInMB > 25) {
                                applicationContext.longToast("Can't Upload. Sorry file size is large.")
                            } else {

                                Timber.e("Name = $name")
                                val type = Constants.Chat.Media.TYPE_VIDEO//contentResolver.getType(uri)
                                val stream = contentResolver.openInputStream(uri)!!

                                val media = Media(name, type, stream)

                                val options = Message.options()
                                    .withMediaFileName(media.name)
                                    .withMedia(media.stream, media.type)
                                    .withMediaProgressListener(object : ProgressListener() {
                                        override fun onStarted() = Timber.e("Start media upload")
                                        override fun onProgress(bytes: Long) = Timber.e("Media upload progress - bytes done: ${bytes}")
                                        override fun onCompleted(mediaSid: String) = Timber.e("Media upload completed")
                                    })

                                val json = JSONObject()
                                json.put("videoUri", uri.toString())
                                json.put("videoSizeInBytes", fileSizeInBytes)
                                json.put("videoName", name)
                                json.put("videoTitle", title)
                                json.put("videoDuration", duration)
                                addUserData(json)
                                options.withAttributes(json)

                                TwilioSingleton.instance.chatClient?.channels?.getChannel(channel.sid, ChatCallbackListener<Channel> {
                                    it.messages.sendMessage(options, ChatCallbackListener<Message> {
                                        Timber.e("Media message sent - sid: ${it.sid}, type: ${it.type}")
                                        deferred.complete(Unit)
                                    })
                                })

                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("Failed to upload media -> error: ${e.message}")
                    } finally {
                        cursor.close()
                    }
                }
            }



            deferred.await()
        }
    }

    // actually useful in public chats
    private fun addUserData(json: JSONObject) {
        json.put("userId", UserMetadata.userId)
        json.put("userName", UserMetadata.userName)
        json.put("userEmail", UserMetadata.userEmail)
        json.put("userPhoto", UserMetadata.photos.first().url)
        json.put("userPhone", UserMetadata.userPhone)
    }


    private fun download(intent: Intent, done: (mediaSid: String) -> Unit) {
        val channel = intent.getParcelableExtra<Channel>(EXTRA_CHANNEL) ?: throw NullPointerException("Channel is not provided")
        val messageIndex = intent.getLongExtra(EXTRA_MESSAGE_INDEX, -1L)

        GlobalScope.launch(coroutineContext) {
            val deferred = CompletableDeferred<String>()

            channel.messages.getMessageByIndex(messageIndex, ChatCallbackListener<Message> { message ->
                val media = message.media ?: return@ChatCallbackListener

                Timber.e("Media received - sid: ${media.sid}, name: ${media.fileName}, type: ${media.type}, size: ${media.size}")

                try {
                    val f = Environment.getExternalStorageDirectory().absolutePath + "/" + media.sid
                    Timber.e("download, f = $f")
                    val f2 = File(f)
                    val outStream = FileOutputStream(f2/*File(cacheDir, media.sid)*/)

                    media.download(outStream, ChatStatusListener { Timber.e("Download completed") }, object : ProgressListener() {
                        override fun onStarted() = Timber.e("Start media download")
                        override fun onProgress(bytes: Long) = Timber.e("Media download progress - bytes done: ${bytes}")
                        override fun onCompleted(mediaSid: String) {
                            Timber.e("Media download completed, mediaSid = $mediaSid")
                            deferred.complete(mediaSid)
                            done.invoke(mediaSid)
                        }
                    })

                } catch (e: Exception) {
                    Timber.e("Failed to download media - error: ${e.message}")
                    deferred.completeExceptionally(e)
                }
            })

            deferred.await()
        }
    }
}
