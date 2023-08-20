package com.andyshon.tiktalk.ui.chatSingle.selectContacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesAdapter
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.app_toolbar_search_simple.*
import timber.log.Timber
import javax.inject.Inject
import ChatCallbackListener
import android.Manifest
import android.net.Uri
import android.os.Parcelable
import androidx.core.widget.doAfterTextChanged
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.services.MediaService
import com.andyshon.tiktalk.ui.chatSingle.entity.CommonMessageObject
import com.andyshon.tiktalk.ui.chatSingle.entity.MessageObject
import com.andyshon.tiktalk.ui.messages.MessagesContract
import com.andyshon.tiktalk.ui.messages.MessagesPresenter
import com.twilio.chat.Channel
import kotlinx.android.synthetic.main.app_toolbar_select.*
import kotlinx.android.synthetic.main.fragment_select.*
import org.jetbrains.anko.support.v4.startService
import java.io.File
import android.provider.MediaStore
import android.content.ContentValues
import com.andyshon.tiktalk.events.RxEventBus
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.support.v4.longToast

class SelectFragment : BaseInjectFragment(), SelectContract.View, MessagesContract.View {

    companion object {
        fun newInstance(message: CommonMessageObject): SelectFragment {
            val bundle = Bundle()
            bundle.putString("messageText", message.message.messageBody ?: "")
            bundle.putBoolean("messageHasMedia", message.message.hasMedia())
//            bundle.putString("channelSid", message.message.channelSid)
//            bundle.putParcelable("channel", message.message.channel)
            if (message.message.hasMedia()) {
                message.message.attributes?.let { attrs ->
                    Timber.e("attributes = ${message.message.attributes}")
                    message.message.media.type?.let { type ->
                        when (type) {
                            Constants.Chat.Media.TYPE_MUSIC -> {
                                if (attrs.jsonObject?.has("musicName") == true) {

                                }
                            }

                            Constants.Chat.Media.TYPE_VIDEO -> {

                            }

                            Constants.Chat.Media.TYPE_VOICE -> {

                            }

                            Constants.Chat.Media.TYPE_IMAGE -> {
                                Timber.e("TYPE IMAGE -> media sid = ${message.message.media.sid}")
                                bundle.putString("mediaSid", message.message.media.sid)
                                bundle.putString(
                                    "fileUri",
                                    message.message.attributes.jsonObject?.getString("fileUri")
                                )
                            }

                            Constants.Chat.Media.TYPE_FILE -> {

                            }
                        }
                    }
                }
            } else {
                when {
                    message.message.attributes.jsonObject?.has("replyName") == true -> {
//                        chats.add(ReplyObject(msg = msg))
                    }

                    message.message.attributes.jsonObject?.has("contactName") == true -> {
//                        chats.add(ContactObject(msg = msg))
                    }

                    else -> {   // text message
                        Timber.e("mess = $message")
                        if (message is MessageObject) {
                            Timber.e("Message object -> send simple text message")
                            bundle.putString("messageType", "message")
                        }
//                        chats.add(MessageObject(msg = msg))
                    }
                }
            }

            val fragment = SelectFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var presenter: MessagesPresenter

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    private var adapter: MessagesAdapter? = null
    @Inject
    lateinit var rxEventBus: RxEventBus

    private var listener: SelectListener? = null


    private var messageText = ""
    private var messageHasMedia = false

    //    private var channel: Channel? = null
    private var messageType = ""
    private var mediaSid = ""
    private var fileUri = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)


        arguments?.let {
//            channel = it.getParcelable("channel")
            messageType = it.getString("messageType") ?: ""
            messageText = it.getString("messageText") ?: ""
            messageHasMedia = it.getBoolean("messageHasMedia")
            mediaSid = it.getString("mediaSid") ?: ""
            fileUri = it.getString("fileUri") ?: ""

            Timber.e("messageText = $messageText, messageHasMedia = $messageHasMedia, fileUri = $fileUri")
        }

        setupToolbar()
        setupList()

        presenter.setTwilioListener()
        presenter.listAllChannels()
    }

    private fun setupList() {
        chatsRecyclerView?.let { it.layoutManager = LinearLayoutManager(getActivityContext()) }
        adapter = MessagesAdapter(
            presenter.chats,
            setClickListener(),
            isSecret = false,
            rxEventBus = rxEventBus
        )
        chatsRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemChatClickListener<ChannelModel> {
        return object : ItemChatClickListener<ChannelModel> {

            override fun onItemClick(view: View, pos: Int, item: ChannelModel) {
                item.getChannel(ChatCallbackListener { channel ->
                    //                    listener?.openSingleChat(channel, item)
                    Timber.e("Click on channel -> ${channel.sid}, ${channel.friendlyName}, ${channel.members.membersList.size}, attributes = ${channel.attributes}")

                    showProgress()
                    sendMessage(channel)
                })
            }

            override fun onItemLongClick(
                view: View,
                pos: Int,
                item: ChannelModel,
                plusOrMinus: Boolean
            ) {
            }
        }
    }

    fun sendMessage(channel: Channel) {

        val file = File(getActivityContext().cacheDir, mediaSid)


        RxPermissions(getActivityContext()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe({
                if (it) {
                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to channel as Parcelable,
//            MediaService.EXTRA_MEDIA_URI to data?.data?.toString(),
//            MediaService.EXTRA_MEDIA_URI to Uri.parse(file.path).toString(),
                        MediaService.EXTRA_MEDIA_URI to fileUri,//getImageContentUri(file).toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_IMAGE
                    )

                    Timber.e("sendMessage, uri = ${Uri.parse(file.path)}, ${getImageContentUri(file)}")
                } else {
                    longToast("Write permission wasn't granted!")
                }
            }, {
                Timber.e("Error Write ==== ${it.message} $it, ${it.localizedMessage}, ${it.cause}")
            })
            .addTo(getDestroyDisposable())


        //  sendMessage, uri = /data/user/0/com.andyshon.tiktalk/cache

//        Timber.e("sendMessage, text = $text")
//        val options = Message.options()
//        options.withBody(text)
//        Timber.e("replyJson $replyJson")
//        replyJson?.let {
//            options.withAttributes(it)
//        }

        /*channel.messages?.sendMessage(options, object : CallbackListener<Message>() {
            override fun onSuccess(p0: Message?) {
                Timber.e("sendMessage, onSuccess: ${p0?.messageBody}, author = ${p0?.author}, ${p0?.channel?.friendlyName}, ${p0?.channelSid}, ${p0?.dateCreated}, ${p0?.dateCreatedAsDate}")
                hideProgress()
                listener?.close()
            }
            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("sendMessage, onError: ${errorInfo?.status}, ${errorInfo?.message}, ${errorInfo?.code}")
                hideProgress()
                listener?.close()
            }
        })*/
    }

    fun getImageContentUri(imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = getActivityContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID) ?: 0)
            cursor.close()
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                return getActivityContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                return null
            }
        }
    }


    override fun onChatsLoaded() {
        adapter?.notifyDataSetChanged()
        adapter?.setTempRealItems()
    }

    override fun itemChanged(pos: Int) {
        adapter?.notifyDataSetChanged()
    }

    override fun updateAdapter() {
        adapter?.notifyDataSetChanged()
    }

    private fun setupToolbar() {
        toolbarBtnBack.setOnClickListener {
            listener?.close()
        }
        toolbarBtnSearch.setOnClickListener {
            toolbarSelectMessages.hide()
            toolbarSelectMessagesSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarSearchSimpleBtnBack.setOnClickListener {
            toolbarSelectMessagesSearch.hide()
            changeToolbarState()
            etSearch.hideKeyboard()
            adapter?.selectedModeOff()
            adapter?.showFullList()
        }
        etSearch.doAfterTextChanged {
            it?.let { adapter?.search(it.toString().trim()) }
        }
    }

    private fun changeToolbarState() {
        toolbarSelectMessages.show()
    }

    override fun onChatDelete() {
        // nothing
    }

    override fun chatDeleted(pos: Int) {
        // nothing
    }

    override fun setEmptyChats(empty: Boolean) {
        // nothing
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SelectListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}