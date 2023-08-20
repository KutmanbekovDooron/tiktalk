package com.andyshon.tiktalk.ui.chatSingle

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.chatSingle.chatMedia.ChatMediaActivity
import com.andyshon.tiktalk.ui.viewContact.ViewContactActivity
import com.andyshon.tiktalk.utils.extensions.*
import com.twilio.chat.*
import kotlinx.android.synthetic.main.activity_chat_single.*
import kotlinx.android.synthetic.main.app_toolbar_chat_single_match.*
import kotlinx.android.synthetic.main.layout_chat_single_empty.*
import kotlinx.android.synthetic.main.layout_chat_single_empty_tips.*
import kotlinx.android.synthetic.main.layout_text_message.*
import timber.log.Timber
import javax.inject.Inject
import kotlinx.android.synthetic.main.app_toolbar_chat_single_tap.*
import kotlinx.android.synthetic.main.layout_chat_single_message_actions.*
import kotlinx.android.synthetic.main.layout_chat_single_message_actions.actionBtnReplyRight
import kotlinx.android.synthetic.main.layout_chat_single_message_reply.*
import kotlinx.android.synthetic.main.layout_chat_single_typing.*
import org.jetbrains.anko.*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.andyshon.tiktalk.data.entity.CallToUser
import com.andyshon.tiktalk.data.services.MediaService
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.chatSingle.entity.*
import com.andyshon.tiktalk.ui.chatSingle.selectContacts.SelectFragment
import com.andyshon.tiktalk.ui.chatSingle.selectContacts.SelectListener
import com.andyshon.tiktalk.ui.dialogs.AttachFileDialog
import com.andyshon.tiktalk.ui.locker.FingerprintLockerActivity
import com.andyshon.tiktalk.ui.locker.PatternLockerActivity
import com.andyshon.tiktalk.ui.locker.PinLockerActivity
import com.andyshon.tiktalk.ui.selectContact.SelectContactActivity
import com.andyshon.tiktalk.ui.calls.video.VideoCallActivity
import com.andyshon.tiktalk.ui.calls.voice.VoiceCallActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.chat.Message
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.app_toolbar_search_single_chat.*
import kotlinx.android.synthetic.main.layout_text_message.btnAttachFile
import kotlinx.android.synthetic.main.layout_text_message.btnSendMessage
import kotlinx.android.synthetic.main.layout_voice_message.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val TOOLBAR_STATE_SIMPLE = 1
private const val TOOLBAR_STATE_TAPPED = 2
private const val CAMERA_REQUEST = 101
private const val GALLERY_REQUEST = 102
private const val FILE_REQUEST = 103
private const val MUSIC_REQUEST = 104
private const val VIDEO_REQUEST = 105
private const val RC_SELECT_CONTACT = 111

class ChatSingleActivity : BaseInjectActivity(), ChatSingleContract.View,
    AttachFileDialog.AttachFileClickListener, SelectListener {

    @Inject
    lateinit var presenter: ChatSinglePresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    @Inject
    lateinit var rxEventBus: RxEventBus

    private var adapter: ChatSingleAdapter? = null

    private var attachFileDialog = AttachFileDialog.newInstance()

    override fun onDestroy() {
        presenter.removeListener()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
//        createUI()
//        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
//        this.window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        setContentView(R.layout.activity_chat_single)

        if (intent != null) {
            presenter.isFromMatches = intent?.getBooleanExtra("isFromMatches", false) ?: false
            presenter.opponentName = intent.getStringExtra(Constants.EXTRA_CHANNEL_OPPONENT_NAME)!!
            presenter.opponentPhoto =
                intent.getStringExtra(Constants.EXTRA_CHANNEL_OPPONENT_PHOTO)!!
            presenter.opponentPhone =
                intent.getStringExtra(Constants.EXTRA_CHANNEL_OPPONENT_PHONE)!!
            presenter.channel = intent.getParcelableExtra<Channel>(Constants.EXTRA_CHANNEL)
            presenter.channelSid = intent.getStringExtra(Constants.EXTRA_CHANNEL_SID) ?: ""
            if (presenter.channel != null) {
                requestWritePermission {
                    presenter.loadAndShowMessages()
                }
            }
            initToolbar()
        }

        presenter.observe()

        initListeners()
        setupList()
    }

    private fun initToolbar() {
        if (presenter.isFromMatches) {
            setupMatchesToolbar()
        } else {
            setupToolbar()
        }
    }

    override fun onMessagesLoaded() {
        adapter?.notifyDataSetChanged()
        chatSingleRecyclerView.post {
        }
    }

    override fun onMessageAdded(message: Message?) {
        onEmptyMatchesLayout(/*false*/2)
        adapter?.notifyItemInserted(presenter.chats.size - 1)
        chatSingleRecyclerView.scrollToPosition(presenter.chats.size - 1)

        if (message != null) {
            if (message.hasMedia()) {
                if (message.media.type == Constants.Chat.Media.TYPE_VOICE || message.media.type == Constants.Chat.Media.TYPE_IMAGE) {
                    val file = File(cacheDir, message.media.sid)
                    Timber.e("is file exist === $file, ${file.exists()}")

                    if (file.exists().not() || file.length() == 0L) {
                        MediaService.rxEventBus = rxEventBus
                        startService<MediaService>(
                            MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_DOWNLOAD,
                            MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                            MediaService.EXTRA_MESSAGE_INDEX to message.messageIndex
                        )
                    }
                }
            }
        }
    }

    override fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    override fun notifyItem(pos: Int) {
        Timber.e("notifyItem pos = $pos")
        if (presenter.chats[pos] is ImageObject) {
            (presenter.chats[pos] as ImageObject).hideProgress = true
        }
        adapter?.notifyItemChanged(pos)
//        adapter?.notifyDataSetChanged()
//        adapter?.notifyItemChanged(/*pos*/presenter.chats.size-1)
    }

    override fun notifyItemDeleted(pos: Int) {
        adapter?.notifyItemRemoved(pos)
    }

    override fun onMessageDeleted(pos: Int) {
        adapter?.notifyItemRemoved(pos)
        adapter?.selectedModeOff()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }

    override fun onTypingStarted(member: Member?) {
        if (member != null) {
            val text = "${presenter.opponentName} is typing ..."
            typingIndicator.text = text
            layoutChatSingleTyping.show()
        }
    }

    override fun onTypingEnded(member: Member?) {
        typingIndicator.text = null
        layoutChatSingleTyping.hide()
    }

    override fun onCopied() {
        adapter?.selectedModeOff()
        adapter?.notifyDataSetChanged()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }

    private fun setupToolbar() {
        val root = toolbarChatSingle
        val toolbar = root.findViewById<Toolbar>(R.id.toolbarRoot)
        val toolbarUserName = root.findViewById<TextView>(R.id.toolbarUserName)
        val toolbarUserAvatar = root.findViewById<ImageView>(R.id.toolbarUserAvatar)
        val toolbarBtnBack = root.findViewById<ImageView>(R.id.toolbarBtnBack)
        val toolbarBtnCamera = root.findViewById<ImageView>(R.id.toolbarBtnCamera)
        val toolbarBtnPhone = root.findViewById<ImageView>(R.id.toolbarBtnPhone)
        toolbar.inflateMenu(R.menu.menu)
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when {
                item.itemId == R.id.chat_menu_view_contact -> {
                    toolbarUserAvatar.performClick()
                    true
                }

                item.itemId == R.id.chat_menu_media -> {
                    openViewContact()
                    true
                }

                item.itemId == R.id.chat_menu_search -> {
                    toolbarChatSingle.hide()
                    toolbarChatSingleTap.hide()
                    toolbarChatSingleMatch.hide()
                    toolbarChatSingleSearch.show()
                    etSearch.showKeyboard3()
                    true
                }

                item.itemId == R.id.chat_menu_mute_notifications -> {
                    muteNotifications()
                    true
                }

                item.itemId == R.id.chat_menu_visibility -> {
                    chooseVisibility()
                    true
                }

                item.itemId == R.id.chat_menu_clear_chat -> {
                    // do something
                    true
                }

                item.itemId == R.id.chat_menu_block -> {
                    blockUser()
                    true
                }

                else -> false
            }
        }

        toolbarUserName.text = presenter.opponentName
        toolbarUserAvatar.loadRoundCornersImage(
            radius = resources.getDimensionPixelSize(R.dimen.radius_100),
            url = presenter.opponentPhoto
        )
        toolbarUserName.setOnClickListener {
            openViewContact()
        }
        toolbarUserAvatar.setOnClickListener {
            openViewContact()
        }
        toolbarBtnBack.setOnClickListener {
            finish()
        }
        toolbarBtnCamera.setOnClickListener {
            val intent = Intent(this@ChatSingleActivity, VideoCallActivity::class.java)
            intent.action = VideoCallActivity.ACTION_START_ACTIVITY
            val callToUser = CallToUser(
                presenter.opponentIdentity,
                presenter.opponentName,
                presenter.opponentPhoto,
                presenter.channelSid
            )
            intent.putExtra(VideoCallActivity.CALL_TO_USER, callToUser)
            startActivity(intent)
        }
        toolbarBtnPhone.setOnClickListener {
            val intent = Intent(this@ChatSingleActivity, VoiceCallActivity::class.java)
            intent.action = VoiceCallActivity.ACTION_START_ACTIVITY
            val callToUser = CallToUser(
                presenter.opponentIdentity,
                presenter.opponentName,
                presenter.opponentPhoto,
                presenter.channelSid
            )
            intent.putExtra(VoiceCallActivity.CALL_TO_USER, callToUser)
            startActivity(intent)
        }
    }

    private fun openViewContact() {
        startActivity<ViewContactActivity>(
            Constants.EXTRA_CHANNEL to presenter.channel,
            Constants.EXTRA_CHANNEL_NAME to presenter.opponentName,
            Constants.EXTRA_CHANNEL_PHOTO to presenter.opponentPhoto,
            Constants.EXTRA_CHANNEL_PHONE to presenter.opponentPhone
        )
    }

    private fun setupMatchesToolbar() {
        toolbarChatSingleMatch.show()
        toolbarChatSingle.hide()
        toolbarMatchUserName.text = presenter.opponentName
        toolbarMatchUserAvatar.loadRoundCornersImage(
            radius = resources.getDimensionPixelSize(R.dimen.radius_100),
            url = presenter.opponentPhoto
        )
        toolbarMatchUserAvatar.setOnClickListener {
            startActivity<ViewContactActivity>(
                Constants.EXTRA_CHANNEL to presenter.channel,
                Constants.EXTRA_CHANNEL_NAME to presenter.opponentName,
                Constants.EXTRA_CHANNEL_PHOTO to presenter.opponentPhoto,
                Constants.EXTRA_CHANNEL_PHONE to presenter.opponentPhone
            )
        }
        toolbarMatchBtnBack.setOnClickListener {
            finish()
        }
        matchedUserAvatar.loadRoundCornersImage(
            radius = resources.getDimensionPixelSize(R.dimen.radius_100),
            url = presenter.opponentPhoto
        )
    }

    private var lastState = -1
    override fun onEmptyMatchesLayout(empty: Int) {
        if (lastState != empty) {
            Timber.e("onEmptyMatchesLayout called, empty = $empty")
            lastState = empty
            if (empty == 1) {
                layoutChatSingleEmpty.show()
                chatSingleRecyclerView.hide()
                layoutChatSingleMatchesTips.show()
                tvMatchedWith.text = getString(R.string.you_matched_with, presenter.opponentName)
                matchedUserAvatar.loadRoundCornersImage(
                    radius = resources.getDimensionPixelSize(R.dimen.radius_100),
                    url = presenter.opponentPhoto
                )

                //todo: set moreThan24Hours in minutes when chat was created
                tvTime.text = getString(R.string.n_mins_ago, 15)
            } else {
                chatSingleRecyclerView.show()
                layoutChatSingleEmpty.hide()
                layoutChatSingleMatchesTips.hide()
            }
        }
    }

    private fun chooseVisibility() {
        presenter.setVisibility(presenter.opponentName)
    }

    private fun muteNotifications() {
        presenter.muteNotifications()
    }

    private fun blockUser() {
        presenter.blockUser(presenter.opponentName)
    }

    override fun openPattern(viaVisibility: Boolean) {
        if (canOpen())
            PatternLockerActivity.startActivity(this)
    }

    override fun openPIN(viaVisibility: Boolean) {
        if (canOpen())
            PinLockerActivity.startActivity(
                this,
                viaVisibility = viaVisibility,
                channelSid = presenter.channelSid
            )
    }

    override fun openFingerprint(viaVisibility: Boolean) {
        if (canOpen())
            FingerprintLockerActivity.startActivity(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("onActivityResult, Path = ${data?.data?.path}, ${data?.data?.toString()}")

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_SELECT_CONTACT -> {
                    data?.let {
                        val contactName = it.getStringExtra("name")!!
                        val contactPhone = it.getStringExtra("phone")!!
//                        val contactPhoto = it.getParcelableExtra<Bitmap>("photo")
                        presenter.sendContactMessage(contactName, contactPhone/*, contactPhoto*/)
                    }
                }

                GALLERY_REQUEST -> {
                    Timber.e("Path = ${data?.data?.path}, ${data?.data?.toString()}")

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data?.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_IMAGE
                    )
                }

                CAMERA_REQUEST -> {
                    Timber.e("CAMERA_REQUEST, outputFileUri = $outputFileUri")
                    val name = "IMG_" + System.currentTimeMillis() + ".JPG"
                    val file = File(cacheDir, name)

                    val maxBufferSize = 1 * 1024 * 1024

                    try {
                        val fos = FileOutputStream(file.path)
                        var realImage =
                            MediaStore.Images.Media.getBitmap(contentResolver, outputFileUri)

                        Timber.e("realImage width 1 = ${realImage.width}, ${realImage.height}")
                        //E/ChatSingleActivity: realImage width = 4032, 3024

                        val exif = ExifInterface(file.path)

                        Timber.e("EXIF orientation = ${exif.getAttribute(ExifInterface.TAG_ORIENTATION)}")

                        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                .equals("6", ignoreCase = true)
                        ) {
                            realImage = rotate(realImage, 90)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                .equals("8", ignoreCase = true)
                        ) {
                            realImage = rotate(realImage, 270)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                .equals("3", ignoreCase = true)
                        ) {
                            realImage = rotate(realImage, 180)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                .equals("0", ignoreCase = true)
                        ) {
                            realImage = rotate(realImage, 90)
                        }

                        Timber.e("realImage width 2 = ${realImage.width}, ${realImage.height}")
                        //E/ChatSingleActivity: realImage width 2 = 3024, 4032

                        val bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        Timber.e("BOO = $bo")
                        fos.close()

                        val uri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                contentResolver,
                                file.path,
                                null,
                                null
                            )
                        )

                        val inputStream = contentResolver.openInputStream(uri)!!
                        val bytesAvailable = inputStream.available()
                        val bufferSize = Math.min(bytesAvailable, maxBufferSize)
                        val buffers = ByteArray(bufferSize)


                        val outputStream = FileOutputStream(file)

                        var bytesRead: Int
                        while (inputStream.read(buffers).also { bytesRead = it } >= 0) {
                            outputStream.write(buffers, 0, bytesRead)
                        }
                        inputStream.close()
                        outputStream.close()

                        Timber.e("File stored in cache dir, $file, name: $name")
                        Timber.e("File stored in cache dir, path = ${Uri.parse(file.path)}")

                        Timber.e("Uriiii = $uri")



                        startService<MediaService>(
                            MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                            MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                            MediaService.EXTRA_MEDIA_URI to uri.toString(),
                            MediaService.EXTRA_MEDIA_URI_TO_DELETE to name,
                            MediaService.EXTRA_MEDIA_FILE_NAME to name,
                            MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_CAMERA   // for camera use TYPE_FILE
                        )

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                FILE_REQUEST -> {
                    Timber.e(
                        "FILE_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${
                            data?.data?.path?.split(
                                "/"
                            )?.last()
                        }"
                    )
                    //    FILE_REQUEST, Path = /storage/emulated/0/Download/13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf,
                    //    file:///storage/emulated/0/Download/13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf.
                    //    fileName = 13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf


                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(data?.data?.path),
                        null
                    ) { p0, _ ->
                        val uri = data?.data?.toString()
                        Timber.e("onScanCompleted, p0 = $p0, uri = $uri")
                        startService<MediaService>(
                            MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                            MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                            MediaService.EXTRA_MEDIA_URI to uri?.toString(),
                            MediaService.EXTRA_MEDIA_FILE_NAME to data?.data?.path?.split("/")
                                ?.last(),
                            MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_FILE
                        )
                    }
                }

                MUSIC_REQUEST -> {
                    Timber.e(
                        "MUSIC_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${
                            data?.data?.path?.split(
                                "/"
                            )?.last()
                        }"
                    )

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_MUSIC
                    )
                }

                VIDEO_REQUEST -> {
                    Timber.e(
                        "VIDEO_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${
                            data?.data?.path?.split(
                                "/"
                            )?.last()
                        }"
                    )

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_VIDEO
                    )
                }
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degree: Int): Bitmap {
        val mtx = Matrix()
//        mtx.postRotate(degree.toFloat())
        mtx.setRotate(degree.toFloat())

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mtx, true)
    }

    private fun openMusicPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Music"), MUSIC_REQUEST)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_REQUEST)
    }

    private fun openGallery() {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST)
    }

    private lateinit var outputFileUri: Uri

    private fun openCamera() {
        //to avoid exposed beyond app through ClipData.Item.getUri()
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        var grantedPermCount = 0
        var isShowAlready = false

        RxPermissions(this)
            .requestEach(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe({ permission ->
                if (permission.granted) {
                    grantedPermCount++
                    if (grantedPermCount == 2) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (cameraIntent.resolveActivity(packageManager) != null) {
                            // create file
                            outputFileUri = Uri.fromFile(createFile())
                            Timber.e("Create file, outputFileUri = $outputFileUri")

                            cameraIntent.action = MediaStore.ACTION_IMAGE_CAPTURE
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                            this@ChatSingleActivity.startActivityForResult(
                                cameraIntent,
                                CAMERA_REQUEST
                            )
                        }
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    if (isShowAlready.not()) {
                        isShowAlready = true
                        alert("Чтобы продолжить необходимо разрешить доступ к камере и фото") {
                            isCancelable = false
                            positiveButton("OK") { isShowAlready = false }
                        }.show()
                    }
                }
            }, {
                Timber.e("onError = ${it.message}")
                Toast.makeText(this@ChatSingleActivity, "error ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }).addTo(getDestroyDisposable())
    }

    private fun createFile(): File {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root)
        if (!myDir.exists())
            myDir.mkdirs()

        val newfile = File(myDir, "IMG___" + System.currentTimeMillis() + ".JPG")
//        val newfile = File(cacheDir, presenter.channel?.)
        try {
            newfile.createNewFile()
        } catch (e: IOException) {
        }
        return newfile
    }

    private fun openChooserFile() {
        val intent = Intent()
        intent.type = "file/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST)
    }

    private fun openFile(/*String mimeType*/) {


        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = /*"application/pdf"*/"*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // special intent for Samsung file manager
        val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", /*"application/pdf")*/"*/*")
        sIntent.addCategory(Intent.CATEGORY_DEFAULT)

        val chooserIntent: Intent?
        if (packageManager.resolveActivity(sIntent, 0) != null) {
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayListOf(intent))
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file")
        }

        try {
            startActivityForResult(chooserIntent, FILE_REQUEST)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(
                this@ChatSingleActivity,
                "No suitable File Manager was found.",
                Toast.LENGTH_SHORT
            ).show()
        }

        /*val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // special intent for Samsung file manager
        val sIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)//Intent("com.sec.android.app.myfiles.PICK_DATA")
         // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", "application/pdf")
        sIntent.addCategory(Intent.CATEGORY_DEFAULT)

        var chooserIntent: Intent? = null
        if (packageManager.resolveActivity(sIntent, 0) != null){
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayListOf(intent))
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file")
        }

        try {
            startActivityForResult(chooserIntent, FILE_REQUEST)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this@ChatSingleActivity, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show()
        }*/
    }

    private fun searchMessages(query: String) {
        presenter.searchMessages(query)
    }

    override fun loadSearchResult() {
        if (presenter.searchResultIndexes.isNotEmpty()) {
            toolbarBtnSearchDown.alpha = 0.5f
            toolbarBtnSearchUp.alpha = 1f
            presenter.chats[presenter.searchResultIndexes.last()].select = true
            chatSingleRecyclerView.scrollToPosition(presenter.searchResultIndexes.last())
        }
    }

    private fun initListeners() {
        fun setTextInto(text: String) {
            etMessageField.setText(text)
            etMessageField.setSelection(etMessageField.text.length)
            etMessageField.showKeyboard3()
        }
        toolbarSearchBtnBack.setOnClickListener {
            etSearch.hideKeyboard()
            toolbarChatSingle.show()
            toolbarChatSingleTap.hide()
            toolbarChatSingleMatch.hide()
            toolbarChatSingleSearch.hide()
            presenter.searchResultIndexes.clear()
        }
        etSearch.setOnEditorActionListener { p0, actionId, p2 ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                etSearch.hideKeyboard()
                Handler().postDelayed({
                    searchMessages(etSearch.text.toString().trim())
                }, 250)
            }
            true
        }
        toolbarBtnSearchUp.setOnClickListener {
            val curIndex = presenter.searchResultIndexes.indexOf(presenter.searchPointer)
            if (curIndex > 0) {
                toolbarBtnSearchUp.alpha = 1f
                val nextIndex = presenter.searchResultIndexes[curIndex - 1]
                presenter.searchPointer = nextIndex
//                if (presenter.searchPointer > 0) {
//                    toolbarBtnSearchUp.alpha = 1f
//                }
//                else {
//                    toolbarBtnSearchUp.alpha = 0.5f
//                }
                presenter.chats[presenter.searchPointer].select = true
                chatSingleRecyclerView.scrollToPosition(presenter.searchPointer)
                adapter?.notifyItemChanged(presenter.searchPointer)
            } else {
                toolbarBtnSearchUp.alpha = 0.5f
            }
            if (curIndex < presenter.searchResultIndexes.size - 1) {
                toolbarBtnSearchDown.alpha = 1f
            }
        }
        toolbarBtnSearchDown.setOnClickListener {
            val curIndex = presenter.searchResultIndexes.indexOf(presenter.searchPointer)
            if (curIndex < presenter.searchResultIndexes.size - 1) {
                toolbarBtnSearchDown.alpha = 1f
                val nextIndex = presenter.searchResultIndexes[curIndex + 1]
                presenter.searchPointer = nextIndex
//                if (presenter.searchPointer < presenter.searchResultIndexes.size-1) {
//                    toolbarBtnSearchDown.alpha = 1f
//                }
//                else {
//                    toolbarBtnSearchDown.alpha = 0.5f
//                }
                presenter.chats[presenter.searchPointer].select = true
                chatSingleRecyclerView.scrollToPosition(presenter.searchPointer)
                adapter?.notifyItemChanged(presenter.searchPointer)
            } else {
                toolbarBtnSearchDown.alpha = 0.5f
            }
            if (curIndex > 0) {
                toolbarBtnSearchUp.alpha = 1f
            }
        }
        btnSendMessage.setOnClickListener {
            val text = etMessageField.text.toString().trim()
            if (text.isNotEmpty()) {
                etMessageField.setText("")
                presenter.sendTextMessage(text)
                adapter?.selectedModeOff()
                adapter?.notifyDataSetChanged()
                changeToolbarState(TOOLBAR_STATE_SIMPLE)
            }
        }
        etMessageField.setOnEditorActionListener { p0, actionId, p2 ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnSendMessage.performClick()
            }
            true
        }
        etMessageField.doAfterTextChanged {
            presenter.typing()
            it?.toString()?.let {
                if (it.isNotEmpty()) {
                    btnSendMessage.show()
                    btnMicrophone.hide()
                } else {
                    btnSendMessage.hide()
                    btnMicrophone.show()
                }
            }
        }
        toolbarChatSingleBtnBack.setOnClickListener {
            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        btnSmile.setOnClickListener {

        }
        btnAttachFile.setOnClickListener {
            attachFileDialog.show(supportFragmentManager)
//            btnAttachFile.
//            etSearch.showKeyboard3()
//            val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            keyboard.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED)
//            val i = Intent(this@ChatSingleActivity, TransparentActivity::class.java)
//            i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
//            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//            i.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
//            i.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
//            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            i.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
//            i.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION)
//            i.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
//            i.addFlags(Intent.FLAG_FROM_BACKGROUND)
//            i.addFlags(Intent.METADATA_DOCK_HOME)
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(i)
//            startActivityFromChild(this@ChatSingleActivity, Intent(this@ChatSingleActivity, TransparentActivity::class.java), 1000)
//            startNextMatchingActivity(Intent(this@ChatSingleActivity, TransparentActivity::class.java))
//            startActivityForResult(Intent(this@ChatSingleActivity, TransparentActivity::class.java), 1000)
        }
        btnRecordCancel.setOnClickListener {
            layoutVoice.startAnimation(hideRecordingLayoutAnimation(150))
            voiceMessageHelper.stopRecord()
        }
        btnMicrophone.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.e("ACTION_DOWN")
                    if (ContextCompat.checkSelfPermission(
                            this@ChatSingleActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestMicPermission {}
                    } else {
                        btnSendVoiceMessage.background = this drawable R.drawable.bg_round_violet
                        layoutVoice.startAnimation(showRecordingLayoutAnimation(200))
                        voiceMessageHelper.init()
                        voiceMessageHelper.start(
                            presenter.opponentName,
                            presenter.channel?.sid ?: presenter.channelSid,
                        )
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    Timber.e("ACTION_UP")
                    voiceMessageHelper.stopRecord()
                    btnSendVoiceMessage.background = this drawable R.drawable.bg_round_6
                    true
                }

                else -> false
            }
        }
        btnSendVoiceMessage.setOnClickListener {
            layoutVoice.startAnimation(hideRecordingLayoutAnimation(150))
            voiceMessageHelper.send(this)
        }
        btnTip1.setOnClickListener {
            setTextInto(Constants.Chat.TIP_1)
        }
        btnTip2.setOnClickListener {
            setTextInto(Constants.Chat.TIP_2)
        }
        btnTip3.setOnClickListener {
            setTextInto(Constants.Chat.TIP_3)
        }

        actionBtnReplyLeft.setOnClickListener {

            changeToolbarState(TOOLBAR_STATE_SIMPLE)

            toolbarChatSingle.hide()
            toolbarChatSingleTap.hide()
            toolbarChatSingleSearch.hide()
            toolbarChatSingleMatch.hide()
            chatSingleRecyclerView.hide()
            layoutChatSingleEmpty.hide()
            layoutChatSingleMessageActions.hide()
            layoutWriteMessage.hide()

            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()


//            val text = presenter.currentSelectedMessage.messageBody ?: null
//            val attributes = presenter.currentSelectedMessage.attributes ?: null
//            val hasMedia = presenter.currentSelectedMessage.hasMedia()
//            val mediaType = if (hasMedia) presenter.currentSelectedMessage.media.type else null
//
//            Timber.e("text = $text, attributes = $attributes, hasMedia = $hasMedia, mediaType = $mediaType")

//            val fragment = SelectFragment.newInstance(text, attributes, hasMedia, mediaType)
            val fragment = SelectFragment.newInstance(presenter.currentSelectedMessage)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.chatSingleRoot, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        actionBtnCopy.setOnClickListener {
            presenter.copyToClipboard(this)
        }
        actionBtnDelete.setOnClickListener {
            showDeleteMessageDialog(this@ChatSingleActivity) {
                presenter.deleteSelectedMessages()
            }
        }
        actionBtnReplyRight.setOnClickListener {
            layoutChatSingleMessageActions.hide()
            layoutChatSingleMessageReply.show()
            btnSendMessage.show()
            btnMicrophone.hide()

            var replyAuthor = ""
            var replyText = ""
            presenter.chats.forEach {
                if (it.isChecked) {
                    replyAuthor = TwilioSingleton.instance.getNameByEmail(
                        presenter.channel,
                        it.message.author
                    )
                    replyText = it.message.messageBody
                    return@forEach
                }
            }
            tvReplyAuthor.text = replyAuthor
            tvReplyText.text = replyText
            presenter.buildReplyJson(replyAuthor, replyText)
        }
        btnReplyClose.setOnClickListener {
            btnSendMessage.hide()
            btnMicrophone.show()
            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
    }

    //  SelectListener
    override fun close() {
        supportFragmentManager.popBackStackImmediate()

        toolbarChatSingle.show()
        chatSingleRecyclerView.show()
        layoutWriteMessage.show()
        toolbarChatSingleTap.hide()
        toolbarChatSingleSearch.hide()
        toolbarChatSingleMatch.hide()
        layoutChatSingleEmpty.hide()
        layoutChatSingleMessageActions.hide()
    }

    override fun galleryAttachDialog() {
        openGallery()
    }

    override fun cameraAttachDialog() {
        openCamera()
    }

    override fun videoAttachDialog() {
        openVideoPicker()
    }

    override fun musicAttachDialog() {
        openMusicPicker()
    }

    override fun fileAttachDialog() {
//        openChooserFile()
        openFile()
    }

    override fun locationAttachDialog() {

    }

    override fun contactAttachDialog() {
        openSelectContact()
    }

    private fun openSelectContact() {
        RxPermissions(this)
            .request(Manifest.permission.READ_CONTACTS)
            .subscribe({ granted ->
                if (granted) {
                    val intent = Intent(this@ChatSingleActivity, SelectContactActivity::class.java)
                    startActivityForResult(intent, RC_SELECT_CONTACT)
                } else {
                    longToast("You should give access to your contacts")
                }
            }, {
                Timber.e("onError = ${it.message}")
            })
            .addTo(getDestroyDisposable())
    }

    private fun showRecordingLayoutAnimation(duration: Long): Animation {
        layoutVoice.show()
        val outToLeft = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        outToLeft.fillAfter = true // move back when animation ends and hide
        outToLeft.duration = duration
        outToLeft.interpolator = AccelerateInterpolator()
        return outToLeft
    }

    private fun hideRecordingLayoutAnimation(duration: Long): Animation {
        layoutVoice.show()
        val outToLeft = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        outToLeft.fillAfter = false // move back when animation ends and hide
        outToLeft.duration = duration
        outToLeft.interpolator = AccelerateInterpolator()
        outToLeft.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                Timber.e("onAnimationEnd hide")
                layoutVoice.hide()
            }
        })
        return outToLeft
    }

    private fun requestMicPermission(granted: () -> Unit) {
        RxPermissions(this).request(Manifest.permission.RECORD_AUDIO)
            .subscribe({
                if (it) {
                    requestWritePermission {
                        granted.invoke()
                    }
                } else {
                    longToast("Record permission wasn't granted!")
                }
            }, {
                Timber.e("Error Record ==== ${it.message}")
                //E/ChatSingleActivity$requestMicPermission: Error = /storage/emulated/0: open failed: EACCES (Permission denied)
            })
            .addTo(getDestroyDisposable())
    }

    val REQUEST_READ_EXTERNAL_STORAGE = 2
    private fun requestWritePermission(granted: () -> Unit) {

        // Проверяем разрешение
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Запрос разрешения
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            // Разрешение уже есть, выполняем нужные действия
            // Например, продолжаем с доступом к файлам
        }

        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        RxPermissions(this)
            .request(*permissions.toTypedArray())
            .subscribe({

                if (it) {
                    granted.invoke()
                } else {
                    longToast("Write permission wasn't granted!")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
            }, {
                Timber.e("Error Write ==== ${it.message} $it, ${it.localizedMessage}, ${it.cause}")
            })
            .addTo(getDestroyDisposable())
    }

    private fun setupList() {
        chatSingleRecyclerView?.let {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.reverseLayout = false
            layoutManager.stackFromEnd = true
            it.layoutManager = layoutManager
        }
        adapter = ChatSingleAdapter(presenter.chats, setClickListener())
        chatSingleRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    val voiceMessageHelper = VoiceMessageHelper(
        this,
        object : VoiceMessageListener {
            override fun recorded(path: String) {}
            override fun onEnded(pos: Int) {
                val item = presenter.chats[pos]
                ((item) as? VoiceObject)?.progress = 0f
                ((item) as? VoiceObject)?.duration = 0
                ((item) as? VoiceObject)?.playing = false
                adapter?.notifyItemChanged(pos)
            }

            override fun newAudioSetted(pos: Int) {
                ((presenter.chats[pos]) as? VoiceObject)?.playing = true
                adapter?.notifyItemChanged(pos)
            }

            override fun paused(pos: Int) {
                ((presenter.chats[pos]) as? VoiceObject)?.playing = false
                adapter?.notifyItemChanged(pos)
            }

            override fun resumed(pos: Int) {
                ((presenter.chats[pos]) as? VoiceObject)?.playing = true
                adapter?.notifyItemChanged(pos)
            }

            override fun updateTime(time: Float) {
                tvRecordingTime.text = time.toString()
            }

            override fun setDuration(pos: Int, seconds: Int) {
                ((presenter.chats[pos]) as? VoiceObject)?.duration = seconds
                adapter?.notifyItemChanged(pos)
            }

            override fun setProgress(pos: Int, progress: Float) {
                Timber.e("setProgress, pos = $pos, progress = $progress")
                ((presenter.chats[pos]) as? VoiceObject)?.progress = progress
                adapter?.notifyItemChanged(pos)
            }
        })

    private fun setClickListener(): ItemChatClickListener<CommonMessageObject> {
        return object : ItemChatClickListener<CommonMessageObject> {

            override fun onItemClick(view: View, pos: Int, item: CommonMessageObject) {
                if (item is ImageObject) {
                    var mediaSid = item.message.media.sid
                    if (mediaSid == null) mediaSid = ""
                    ChatMediaActivity.startActivity(getActivityContext(), mediaSid, "", "")
                }
                if (item is VoiceObject) {
                    //todo: play voice

                    val voiceFile = cacheDir.absolutePath.plus("/").plus(item.message.media.sid)
                    Timber.e("voiceFile = $voiceFile")

                    voiceMessageHelper.playFromPath(voiceFile, pos)
                }
                if (item is MusicObject) {
//                    val musicFile = cacheDir.absolutePath.plus("/").plus(item.message.media.sid)
                    val musicFile = File(item.path)
                    Timber.e("musicFile exists = ${musicFile.exists()}, path = ${musicFile.path}")
                    if (musicFile.exists()) {
//                        item.playing = true
                        adapter?.let {
                            Timber.e("SELE mode = ${it.isSelectModeEnabled()}")
                            if (it.isSelectModeEnabled().not()) {
                                it.notifyItemChanged(pos)
                            }
                        }
                    } else {
                        //todo: need to download music in download folder and then to play

                    }
                }
                if (item is VideoObject) {
                    ChatMediaActivity.startActivity(
                        getActivityContext(),
                        "",
                        item.path,
                        item.duration
                    )
                }
            }

            override fun onItemLongClick(
                view: View,
                pos: Int,
                item: CommonMessageObject,
                plusOrMinus: Boolean
            ) {
                presenter.currentSelectedMessage = item
                if (plusOrMinus) {
                    presenter.checkedItems++
                } else {
                    presenter.checkedItems--
                }
                if (presenter.checkedItems == 1) {  // if forwarded last message -> smooth scroll list to bottom in case not to hide part of a message
                    if (presenter.chats.last().isChecked) {
                        Handler().postDelayed({
                            chatSingleRecyclerView.smoothScrollToPosition(presenter.chats.size - 1)
                        }, 150)
                    }
                }
                if (presenter.checkedItems >= 1) {
                    changeToolbarState(TOOLBAR_STATE_TAPPED)
                    tvChatSingleNumberCounter.text = presenter.checkedItems.toString()
                } else {
                    adapter?.selectedModeOff()
                    changeToolbarState(TOOLBAR_STATE_SIMPLE)
                }
                adapter?.notifyItemChanged(pos)
//                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun changeToolbarState(state: Int) {
        when (state) {
            TOOLBAR_STATE_SIMPLE -> {
                presenter.checkedItems = 0
                presenter.resetReplyJson()
                toolbarChatSingle.show()
                toolbarChatSingleTap.hide()
                layoutChatSingleMessageActions.hide()
                layoutChatSingleMessageReply.hide()
            }

            TOOLBAR_STATE_TAPPED -> {
                toolbarChatSingle.hide()
                layoutChatSingleMessageReply.hide()
                toolbarChatSingleTap.show()
                layoutChatSingleMessageActions.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        voiceMessageHelper.stopPlay()
    }
}
