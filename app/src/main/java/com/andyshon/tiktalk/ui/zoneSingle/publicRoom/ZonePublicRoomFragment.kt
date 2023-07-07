package com.andyshon.tiktalk.ui.zoneSingle.publicRoom

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.services.MediaService
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.chatSingle.VoiceMessageHelper
import com.andyshon.tiktalk.ui.chatSingle.VoiceMessageListener
import com.andyshon.tiktalk.ui.chatSingle.chatMedia.ChatMediaActivity
import com.andyshon.tiktalk.ui.chatSingle.entity.*
import com.andyshon.tiktalk.ui.chatSingle.selectContacts.SelectFragment
import com.andyshon.tiktalk.ui.dialogs.AttachFileDialog
import com.andyshon.tiktalk.ui.selectContact.SelectContactActivity
import com.andyshon.tiktalk.ui.zoneSingle.ZoneSingleActivity
import com.andyshon.tiktalk.ui.zoneSingle.ZoneSingleListener
import com.andyshon.tiktalk.utils.extensions.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.chat.Member
import com.twilio.chat.Message
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_zone_public_room.*
import kotlinx.android.synthetic.main.layout_chat_single_message_actions.*
import kotlinx.android.synthetic.main.layout_chat_single_message_actions.actionBtnReplyRight
import kotlinx.android.synthetic.main.layout_chat_single_message_reply.*
import kotlinx.android.synthetic.main.layout_chat_single_typing.*
import kotlinx.android.synthetic.main.layout_text_message.*
import kotlinx.android.synthetic.main.layout_voice_message.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.startService
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TOOLBAR_STATE_SIMPLE = 1
private const val TOOLBAR_STATE_TAPPED = 2
private const val CAMERA_REQUEST = 101
private const val GALLERY_REQUEST = 102
private const val FILE_REQUEST = 103
private const val MUSIC_REQUEST = 104
private const val VIDEO_REQUEST = 105
private const val RC_SELECT_CONTACT = 111

class ZonePublicRoomFragment: BaseInjectFragment(), ZonePublicRoomContract.View, AttachFileDialog.AttachFileClickListener {

    @Inject lateinit var presenter: ZonePublicRoomPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: ChatPublicRoomAdapter? = null

    private var listener: ZoneSingleListener? = null

    private var attachFileDialog = AttachFileDialog.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_zone_public_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)

        initListeners()
        setupList()


        presenter.channel = (activity as ZoneSingleActivity).getChannel()
        presenter.channelSid = presenter.channel?.sid ?: ""
        Timber.e("channel = ${presenter.channel}")
        Timber.e("channelSid = ${presenter.channelSid}")

        if (presenter.channel != null) {
            requestWritePermission {
                presenter.loadAndShowMessages()
            }
        }
    }

    fun pressedBackSelected() {
        adapter?.selectedModeOff()
        adapter?.notifyDataSetChanged()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }

    private fun initListeners() {
        etMessageField.clearFocus()
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
            it?.toString()?.let {
                if (it.isNotEmpty()) {
                    presenter.typing()
                    btnSendMessage.show()
                    btnMicrophone.hide()
                }
                else {
                    btnSendMessage.hide()
                    btnMicrophone.show()
                }
            }
        }


        actionBtnReplyLeft.setOnClickListener {

//            changeToolbarState(TOOLBAR_STATE_SIMPLE)

//            toolbarChatSingle.hide()
//            toolbarChatSingleTap.hide()
//            toolbarChatSingleSearch.hide()
//            toolbarChatSingleMatch.hide()
//            chatSingleRecyclerView.hide()
//            layoutChatSingleEmpty.hide()
//            layoutChatSingleMessageActions.hide()
//            layoutWriteMessage.hide()

//            adapter?.selectedModeOff()
//            adapter?.notifyDataSetChanged()


//            val fragment = SelectFragment.newInstance(presenter.currentSelectedMessage)
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.add(R.id.chatSingleRoot, fragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
        }
        actionBtnCopy.setOnClickListener {
            presenter.copyToClipboard(getActivityContext())
        }
        actionBtnDelete.setOnClickListener {
            var b = false
            for (i in (0..presenter.chats.lastIndex).reversed()) {
                if (presenter.chats[i].isChecked) {
                    if (presenter.chats[i].message.attributes.getString("userEmail") != UserMetadata.userEmail) {
                        b = true
                    }
                }
            }
            if (b.not()) {
                showDeleteMessageDialog(getActivityContext()) {
                    presenter.deleteSelectedMessages()
                }
            }
        }
        actionBtnReplyRight.setOnClickListener {
            /*layoutChatSingleMessageActions.hide()
            layoutChatSingleMessageReply.show()
            btnSendMessage.show()
            btnMicrophone.hide()

            var replyAuthor = ""
            var replyText = ""
            presenter.chats.forEach {
                if (it.isChecked) {
                    replyAuthor = TwilioSingleton.instance.getNameByEmail(presenter.channel, it.message.author)
                    replyText = it.message.messageBody
                    return@forEach
                }
            }
            tvReplyAuthor.text = replyAuthor
            tvReplyText.text = replyText
            presenter.buildReplyJson(replyAuthor, replyText)*/
        }



        btnAttachFile.setOnClickListener {
            activity?.let {
                attachFileDialog.show(it.supportFragmentManager)
            }
        }
        btnRecordCancel.setOnClickListener {
            layoutVoice.startAnimation(hideRecordingLayoutAnimation(150))
            voiceMessageHelper.stopRecord()
        }
        btnMicrophone.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.e("ACTION_DOWN")
                    context?.let {
                        if (ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            requestMicPermission {}
                        }
                        else {
                            btnSendVoiceMessage.background = this drawable R.drawable.bg_round_violet
                            layoutVoice.startAnimation(showRecordingLayoutAnimation(200))
                            voiceMessageHelper.start(/*presenter.opponentName*/"default media name", presenter.channel?.sid ?: presenter.channelSid)
                        }
                        true
                    }
                    false
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
            voiceMessageHelper.send(getActivityContext())
        }
        actionBtnReplyLeft.setOnClickListener {

            changeToolbarState(TOOLBAR_STATE_SIMPLE)

            layoutChatSingleMessageActions.hide()
            placePublicRoomListRecyclerView.hide()
            layoutChatSingleMessageReply.hide()
            layoutWriteMessage.show()

            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()

            activity?.let {
                val fragment = SelectFragment.newInstance(presenter.currentSelectedMessage)
                val transaction = it.supportFragmentManager.beginTransaction()
                transaction.add(R.id.chatSingleRoot, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
        actionBtnCopy.setOnClickListener {
            presenter.copyToClipboard(getActivityContext())
        }
        actionBtnDelete.setOnClickListener {
            context?.let {
                showDeleteMessageDialog(it) {
                    presenter.deleteSelectedMessages()
                }
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
                    replyAuthor = presenter.getUsernameByEmail(it.message.author)
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

    override fun hideRecycler(b: Boolean) {
        if (b) {
            progressBar.show()
            placePublicRoomListRecyclerView.invisible()
        }
        else {
            progressBar.hide()
            placePublicRoomListRecyclerView.show()
        }
    }

    override fun onMessagesLoaded() {
        adapter?.notifyDataSetChanged()
    }

    override fun onMessageAdded(message: Message?) {
//        onEmptyMatchesLayout(/*false*/2)
        adapter?.notifyItemInserted(presenter.chats.size-1)
        placePublicRoomListRecyclerView.scrollToPosition(presenter.chats.size - 1)

        if (message != null) {
            if (message.hasMedia()) {
                if (message.media.type == Constants.Chat.Media.TYPE_VOICE || message.media.type == Constants.Chat.Media.TYPE_IMAGE) {
                    val file = File(getActivityContext().cacheDir, message.media.sid)
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
            val username = presenter.getUsernameByEmail(member.identity)
            val text = "$username is typing ..."
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

    private fun changeToolbarState(state: Int) {
        when(state) {
            TOOLBAR_STATE_SIMPLE -> {
                presenter.checkedItems = 0
                presenter.resetReplyJson()
                listener?.setToolbarStateSimple()
                layoutChatSingleMessageActions.hide()
                layoutChatSingleMessageReply.hide()
            }
            TOOLBAR_STATE_TAPPED -> {
                listener?.setToolbarStateTapped()
                layoutChatSingleMessageActions.show()
                layoutChatSingleMessageReply.hide()
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

        RxPermissions(getActivityContext())
            .requestEach(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe ({ permission ->
                if (permission.granted) {
                    grantedPermCount++
                    if (grantedPermCount == 2) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (cameraIntent.resolveActivity(getActivityContext().packageManager) != null) {
                            // create file
                            outputFileUri = Uri.fromFile(createFile())
                            Timber.e("Create file, outputFileUri = $outputFileUri")

                            cameraIntent.action = MediaStore.ACTION_IMAGE_CAPTURE
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                            getActivityContext().startActivityForResult(cameraIntent, CAMERA_REQUEST)
                        }
                    }
                }
                else if (permission.shouldShowRequestPermissionRationale) {
                    if (isShowAlready.not()) {
                        isShowAlready = true
                        getActivityContext().alert("Чтобы продолжить необходимо разрешить доступ к камере и фото") {
                            isCancelable = false
                            positiveButton("OK") { isShowAlready = false }
                        }.show()
                    }
                }
            }, {
                Timber.e("onError = ${it.message}")
                Toast.makeText(getActivityContext(), "error ${it.message}", Toast.LENGTH_LONG).show()
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
        } catch (e: IOException) { }
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
        if (getActivityContext().packageManager.resolveActivity(sIntent, 0) != null){
            // it is device with Samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayListOf(intent))
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file")
        }

        try {
            startActivityForResult(chooserIntent, FILE_REQUEST)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(getActivityContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show()
        }
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
        RxPermissions(getActivityContext())
            .request(Manifest.permission.READ_CONTACTS)
            .subscribe ({ granted ->
                if (granted) {
                    val intent = Intent(getActivityContext(), SelectContactActivity::class.java)
                    startActivityForResult(intent, RC_SELECT_CONTACT)
                }
                else {
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
        RxPermissions(getActivityContext()).request(Manifest.permission.RECORD_AUDIO)
            .subscribe({
                if (it) {
                    requestWritePermission {
                        granted.invoke()
                    }
                }
                else { longToast("Record permission wasn't granted!") }
            }, {
                Timber.e("Error Record ==== ${it.message}")
                //E/ChatSingleActivity$requestMicPermission: Error = /storage/emulated/0: open failed: EACCES (Permission denied)
            })
            .addTo(getDestroyDisposable())
    }

    private fun requestWritePermission(granted: () -> Unit) {
        RxPermissions(getActivityContext()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe({
                if (it) {
                    granted.invoke()
                }
                else { longToast("Write permission wasn't granted!") }
            }, {
                Timber.e("Error Write ==== ${it.message} $it, ${it.localizedMessage}, ${it.cause}")
            })
            .addTo(getDestroyDisposable())
    }

    private fun setupList() {
        placePublicRoomListRecyclerView?.let {
            val layoutManager = LinearLayoutManager(getActivityContext())
            layoutManager.reverseLayout = false
            layoutManager.stackFromEnd = true
            it.layoutManager = layoutManager
        }
        adapter = ChatPublicRoomAdapter(presenter.chats, setClickListener(), rxEventBus)
        placePublicRoomListRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    val voiceMessageHelper = VoiceMessageHelper(object: VoiceMessageListener {
        override fun recorded(path: String) {}
        override fun onEnded(pos: Int) {
            val item = presenter.chats[pos]
            ((item)as? VoiceObject)?.progress=0f
            ((item)as? VoiceObject)?.duration=0
            ((item)as? VoiceObject)?.playing=false
            adapter?.notifyItemChanged(pos)
        }
        override fun newAudioSetted(pos: Int) {
            ((presenter.chats[pos])as? VoiceObject)?.playing=true
            adapter?.notifyItemChanged(pos)
        }
        override fun paused(pos: Int) {
            ((presenter.chats[pos])as? VoiceObject)?.playing=false
            adapter?.notifyItemChanged(pos)
        }
        override fun resumed(pos: Int) {
            ((presenter.chats[pos])as? VoiceObject)?.playing=true
            adapter?.notifyItemChanged(pos)
        }
        override fun updateTime(time: Float) {
            tvRecordingTime.text = time.toString()
        }
        override fun setDuration(pos: Int, seconds: Int) {
            ((presenter.chats[pos])as? VoiceObject)?.duration=seconds
            adapter?.notifyItemChanged(pos)
        }
        override fun setProgress(pos: Int, progress: Float) {
            Timber.e("setProgress, pos = $pos, progress = $progress")
            ((presenter.chats[pos])as? VoiceObject)?.progress=progress
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

                    val voiceFile = getActivityContext().cacheDir.absolutePath.plus("/").plus(item.message.media.sid)
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
                    }
                    else {
                        //todo: need to download music in download folder and then to play

                    }
                }
                if (item is VideoObject) {
                    ChatMediaActivity.startActivity(getActivityContext(), "", item.path, item.duration)
                }
            }
            override fun onItemLongClick(view: View, pos: Int, item: CommonMessageObject, plusOrMinus: Boolean) {
                presenter.currentSelectedMessage = item
                if (plusOrMinus) {
                    presenter.checkedItems++
                } else {
                    presenter.checkedItems--
                }
                if (presenter.checkedItems == 1) {  // if forwarded last message -> smooth scroll list to bottom in case not to hide part of a message
                    if (presenter.chats.last().isChecked) {
                        Handler().postDelayed({
                            placePublicRoomListRecyclerView.smoothScrollToPosition(presenter.chats.size - 1)
                        }, 150)
                    }
                }
                if (presenter.checkedItems >= 1) {
                    changeToolbarState(TOOLBAR_STATE_TAPPED)
                    listener?.setToolbartNumberCounter(presenter.checkedItems)
                } else {
                    adapter?.selectedModeOff()
                    changeToolbarState(TOOLBAR_STATE_SIMPLE)
                }
                adapter?.notifyItemChanged(pos)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ZoneSingleListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ZoneSingleListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        presenter.removeListener()
        super.onDestroy()
    }
}