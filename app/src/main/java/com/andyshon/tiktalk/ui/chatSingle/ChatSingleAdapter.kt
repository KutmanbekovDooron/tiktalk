package com.andyshon.tiktalk.ui.chatSingle

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.chatSingle.entity.*
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.item_single_chat_date.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_image.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_reply.view.*
import kotlinx.android.synthetic.main.item_single_chat_own.view.*
import kotlinx.android.synthetic.main.item_single_chat_own.view.layoutChatSingleRoot
import kotlinx.android.synthetic.main.item_single_chat_own_image.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_reply.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_voice.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_voice.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_voice.view.btnPlayVoiceOwn
import kotlinx.android.synthetic.main.item_single_chat_own_voice.view.ivVoiceTrack
import kotlinx.android.synthetic.main.item_single_chat_own_voice.view.tvVoiceOwnMessageTime
import timber.log.Timber
import java.io.File
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import kotlinx.android.synthetic.main.item_single_chat_own_music.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_contact.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_file.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_contact.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_file.view.*
import android.media.MediaPlayer
import android.os.Environment
import java.io.IOException
import android.os.Handler
import android.view.SurfaceHolder
import android.widget.SeekBar
import com.twilio.chat.ErrorInfo
import com.twilio.chat.ProgressListener
import com.twilio.chat.StatusListener
import kotlinx.android.synthetic.main.item_single_chat_call.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_music.view.*
import kotlinx.android.synthetic.main.item_single_chat_opponent_video.view.*
import kotlinx.android.synthetic.main.item_single_chat_own_video.view.*
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

private const val TYPE_DATE = 1
private const val TYPE_OWN = 2
private const val TYPE_OPPONENT = 3
private const val TYPE_IMAGE_OWN = 4
private const val TYPE_IMAGE_OPPONENT = 5
private const val TYPE_REPLY_OWN = 6
private const val TYPE_REPLY_OPPONENT = 7
private const val TYPE_UNCONSUMED_HORIZON = 8
private const val TYPE_VOICE_OWN = 9
private const val TYPE_VOICE_OPPONENT = 10
private const val TYPE_FILE_OWN = 11
private const val TYPE_FILE_OPPONENT = 12
private const val TYPE_CONTACT_OWN = 13
private const val TYPE_CONTACT_OPPONENT = 14
private const val TYPE_MUSIC_OWN = 15
private const val TYPE_MUSIC_OPPONENT = 16
private const val TYPE_VIDEO_OWN = 17
private const val TYPE_VIDEO_OPPONENT = 18
private const val TYPE_CALL = 19

class ChatSingleAdapter(
    private val messages: List<CommonMessageObject>,
    private val itemClicksListener: ItemChatClickListener<CommonMessageObject>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedMode = false

    fun selectedModeOff() {
        messages.forEach { it.isChecked = false }
        selectedMode = false
    }

    fun isSelectModeEnabled() = selectedMode

    override fun getItemCount(): Int = messages.size

    private fun setOnClickListeners(holder: RecyclerView.ViewHolder) {
        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            selectedMode = true
            messages[position].isChecked = messages[position].isChecked.not()
            itemClicksListener.onItemLongClick(it, position, messages[position], messages[position].isChecked)
            true
        }
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (selectedMode) {
                messages[position].isChecked = messages[position].isChecked.not()
                itemClicksListener.onItemLongClick(it, position, messages[position], messages[position].isChecked)
            }
            else {
                itemClicksListener.onItemClick(it, position, messages[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE -> ChatsDateViewHolder.create(parent)
            TYPE_OWN -> {
                val holder = ChatsSingleOwnViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_OPPONENT -> {
                val holder = ChatsSingleOpponentViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_IMAGE_OWN -> {
                val holder = ChatsSingleOwnImageViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_REPLY_OWN -> {
                val holder = ChatsSingleOwnReplyViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_REPLY_OPPONENT -> {
                val holder = ChatsSingleOpponentReplyViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_UNCONSUMED_HORIZON -> {
                val holder = ChatsHorizonViewHolder.create(parent)
                holder
            }
            TYPE_VOICE_OWN -> {
                val holder = ChatsSingleOwnVoiceViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_VOICE_OPPONENT -> {
                val holder = ChatsSingleOpponentVoiceViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_FILE_OWN -> {
                val holder = ChatsSingleOwnFileViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_FILE_OPPONENT -> {
                val holder = ChatsSingleOpponentFileViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_CONTACT_OWN -> {
                val holder = ChatsSingleOwnContactViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_CONTACT_OPPONENT -> {
                val holder = ChatsSingleOpponentContactViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_MUSIC_OWN -> {
                val holder = ChatsSingleOwnMusicViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_MUSIC_OPPONENT -> {
                val holder = ChatsSingleOpponentMusicViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_VIDEO_OWN -> {
                val holder = ChatsSingleOwnVideoViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_VIDEO_OPPONENT -> {
                val holder = ChatsSingleOpponentVideoViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
            TYPE_CALL -> {
                val holder = ChatsSingleCallViewHolder.create(parent)
                setOnClickListeners(holder) // todo remove click event
                holder
            }
            else -> {
                val holder = ChatsSingleOpponentImageViewHolder.create(parent)
                setOnClickListeners(holder)
                holder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position]) {
            is DateObject -> TYPE_DATE
            is MessageObject -> if (isTypeOwn(position)) TYPE_OWN else TYPE_OPPONENT
            is ReplyObject -> if (isTypeOwn(position)) TYPE_REPLY_OWN else TYPE_REPLY_OPPONENT
            is UnConsumedHorizonObject -> TYPE_UNCONSUMED_HORIZON
            is VoiceObject -> if (isTypeOwn(position)) TYPE_VOICE_OWN else TYPE_VOICE_OPPONENT
            is FileObject -> if (isTypeOwn(position)) TYPE_FILE_OWN else TYPE_FILE_OPPONENT
            is ContactObject -> if (isTypeOwn(position)) TYPE_CONTACT_OWN else TYPE_CONTACT_OWN
            is MusicObject -> if (isTypeOwn(position)) TYPE_MUSIC_OWN else TYPE_MUSIC_OPPONENT
            is VideoObject -> if (isTypeOwn(position)) TYPE_VIDEO_OWN else TYPE_VIDEO_OPPONENT
            is CallObject -> TYPE_CALL
            else -> if (isTypeOwn(position)) TYPE_IMAGE_OWN else TYPE_IMAGE_OPPONENT
        }
    }

    private fun isTypeOwn(position:Int) = messages[position].message.author == UserMetadata.userEmail

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_DATE -> (holder as ChatsDateViewHolder).bind(messages[position])
            TYPE_OWN -> (holder as ChatsSingleOwnViewHolder).bind(messages[position])
            TYPE_OPPONENT -> (holder as ChatsSingleOpponentViewHolder).bind(messages[position])
            TYPE_IMAGE_OWN -> (holder as ChatsSingleOwnImageViewHolder).bind(messages[position])
            TYPE_IMAGE_OPPONENT -> (holder as ChatsSingleOpponentImageViewHolder).bind(messages[position])
            TYPE_REPLY_OWN -> (holder as ChatsSingleOwnReplyViewHolder).bind(messages[position])
            TYPE_REPLY_OPPONENT -> (holder as ChatsSingleOpponentReplyViewHolder).bind(messages[position])
            TYPE_UNCONSUMED_HORIZON -> (holder as ChatsHorizonViewHolder).bind(messages[position])
            TYPE_VOICE_OWN -> (holder as ChatsSingleOwnVoiceViewHolder).bind(messages[position])
            TYPE_VOICE_OPPONENT -> (holder as ChatsSingleOpponentVoiceViewHolder).bind(messages[position])
            TYPE_FILE_OWN -> (holder as ChatsSingleOwnFileViewHolder).bind(messages[position])
            TYPE_FILE_OPPONENT -> (holder as ChatsSingleOpponentFileViewHolder).bind(messages[position])
            TYPE_CONTACT_OWN -> (holder as ChatsSingleOwnContactViewHolder).bind(messages[position])
            TYPE_CONTACT_OPPONENT -> (holder as ChatsSingleOpponentContactViewHolder).bind(messages[position])
            TYPE_MUSIC_OWN -> (holder as ChatsSingleOwnMusicViewHolder).bind(messages[position])
            TYPE_MUSIC_OPPONENT -> (holder as ChatsSingleOpponentMusicViewHolder).bind(messages[position])
            TYPE_VIDEO_OWN -> (holder as ChatsSingleOwnVideoViewHolder).bind(messages[position])
            TYPE_VIDEO_OPPONENT -> (holder as ChatsSingleOpponentVideoViewHolder).bind(messages[position])
            TYPE_CALL -> (holder as ChatsSingleCallViewHolder).bind(messages[position])
        }
    }

    class ChatsHorizonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsHorizonViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_horizon, parent, false)
                return ChatsHorizonViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
//            itemView.tvDate.text = getDateMessageFromDate(message.message.dateCreatedAsDate.toString())
        }
    }

    class ChatsDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsDateViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_date, parent, false)
                return ChatsDateViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            itemView.tvDate.text = getDateMessageFromDate(message.message.dateCreatedAsDate.toString())
        }
    }

    class ChatsSingleOwnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own, parent, false)
                return ChatsSingleOwnViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("UserMetadata.lastCMI = ${UserMetadata.lastCMI}, ${message.message.messageIndex}")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnMessageVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnMessageVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }

            if (message.isChecked) {
                itemView.layoutChatSingleRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            itemView.tvOwnMessage.text = message.message.messageBody
            itemView.tvOwnMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())


            if (message.select) {
                val colorFrom = itemView.context color R.color.colorChatOwn
                itemView.layoutChatSingleRoot.setBackgroundColor(colorFrom)
                val colorTo = itemView.context color R.color.colorDefaultThemeBackground
                val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                colorAnimation.duration = 1500 // milliseconds
                colorAnimation.addUpdateListener { animator -> itemView.layoutChatSingleRoot.setBackgroundColor(animator.animatedValue as Int) }
                itemView.layoutChatSingleRoot.postDelayed({
                    colorAnimation.start()
                }, 500)
                colorAnimation.doOnEnd { message.select = false }
            }
        }
    }

    class ChatsSingleOpponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent, parent, false)
                return ChatsSingleOpponentViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            itemView.tvOpponentMessage.text = message.message.messageBody
            itemView.tvOpponentMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())


            if (message.select) {
                val colorFrom = itemView.context color R.color.colorChatOwn
                itemView.layoutChatSingleOpponentRoot.setBackgroundColor(colorFrom)
                val colorTo = itemView.context color R.color.colorDefaultThemeBackground
                val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                colorAnimation.duration = 1500 // milliseconds
                colorAnimation.addUpdateListener { animator -> itemView.layoutChatSingleOpponentRoot.setBackgroundColor(animator.animatedValue as Int) }
                itemView.layoutChatSingleOpponentRoot.postDelayed({
                    colorAnimation.start()
                }, 500)
                colorAnimation.doOnEnd { message.select = false }
            }
        }
    }

    class ChatsSingleOwnImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnImageViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_image, parent, false)
                return ChatsSingleOwnImageViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOwnImageViewHolder called, ${message.message.attributes}")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnImageVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnImageVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            if (message.isChecked) {
                itemView.layoutChatSingleOwnImageRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOwnImageRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            itemView.tvOwnImageMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.message.hasMedia()) {
                itemView.ivOwnImage.show()
                if (((message)as ImageObject).hideProgress) {
                    itemView.ownProgressBar.hide()
                } else {
                    itemView.ownProgressBar.show()
                }
                itemView.ivOwnImage.loadRoundCornersImage(
                    uri = Uri.fromFile(File(itemView.context.cacheDir, message.message.media.sid))
                )
            }
        }
    }

    class ChatsSingleOpponentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentImageViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_image, parent, false)
                return ChatsSingleOpponentImageViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOpponentImageViewHolder called, ${message.message.attributes}, has media = ${message.message.hasMedia()}")
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentImageRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentImageRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            itemView.tvOpponentImageMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.message.hasMedia()) {
                itemView.ivOpponentImage.show()
                Timber.e("has progress = ${((message)as ImageObject).hideProgress}")
                if (((message)as ImageObject).hideProgress) {
                    itemView.opponentProgressBar.hide()
                } else {
                    itemView.opponentProgressBar.show()
                }
                itemView.ivOpponentImage.loadRoundCornersImage(
                    uri = Uri.fromFile(File(itemView.context.cacheDir, message.message.media.sid))
                )
            }
        }
    }

    class ChatsSingleOwnReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnReplyViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_reply, parent, false)
                return ChatsSingleOwnReplyViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnReplyVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnReplyVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            itemView.tvOwnReplyMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOwnReplyRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOwnReplyRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            val replyName = message.message.attributes.jsonObject?.getString("replyName")
            val replyText = message.message.attributes.jsonObject?.getString("replyText")
            itemView.tvOwnReplyUserName.text = replyName
            itemView.tvOwnReplyMessage.text = replyText
            itemView.tvOwnAfterReplyMessage.text = message.message.messageBody
        }
    }

    class ChatsSingleOpponentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentReplyViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_reply, parent, false)
                return ChatsSingleOpponentReplyViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            itemView.tvOpponentReplyMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentReplyRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentReplyRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            val replyName = message.message.attributes.jsonObject?.getString("replyName")
            val replyText = message.message.attributes.jsonObject?.getString("replyText")
            itemView.tvOpponentReplyUserName.text = replyName
            itemView.tvOpponentReplyMessage.text = replyText
            itemView.tvOpponentAfterReplyMessage.text = message.message.messageBody
        }
    }


    class ChatsSingleOwnVoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnVoiceViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_voice, parent, false)
                return ChatsSingleOwnVoiceViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOwnVoiceViewHolder bind !!!! ${message.message.dateCreatedAsDate}")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnVoiceVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnVoiceVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            if (message.isChecked) {
                itemView.layoutChatSingleVoiceRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleVoiceRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            if (((message)as VoiceObject).playing) {
                itemView.btnPlayVoiceOwn.setImageResource(R.drawable.ic_btn_pause)
            }
            else {
                itemView.btnPlayVoiceOwn.setImageResource(R.drawable.ic_btn_play)
            }

            itemView.tvVoiceOwnMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())


            val file = File(itemView.context.cacheDir, message.message.media.sid)
            Timber.e("is file exist = $file, ${file.exists()}, length = ${file.length()}")

            // set waveform image
            Timber.e("setRawData, bytesArray = ${message.bytesArray}, size = ${message.bytesArray.size}")
            itemView.ivVoiceTrack.setRawData(message.bytesArray) { }

            var isEnded = true

            val progressAnim: ObjectAnimator by lazy {
                ObjectAnimator.ofFloat(itemView.ivVoiceTrack, "progress", /*0F*/(message.progress * 100/*+50*/) / message.duration, 100F).apply {
                    interpolator = LinearInterpolator()
                    if (message.progress != 0f) {
//                        val dif = message.progress * 100 / message.duration
//                        Timber.e("DIF = $dif, ${dif.toLong()}")
//                        duration = dif.toLong()
                        duration = (message.duration).toLong()
                    }
                    else {
                        duration = (message.duration).toLong()
                    }
                }
            }
            with(progressAnim) {
                addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) { isEnded = true }
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationStart(animation: Animator) { isEnded = false }
                    })
            }

            if (message.playing) {
                if (message.duration != 0) {
                    Timber.e("voice duration = ${message.duration}")

                    if (isEnded) {
                        progressAnim.start()
                    } else {
                        if (message.progress != 0f) {
                            val progress = message.progress * 100 / message.duration
                            itemView.ivVoiceTrack.progress = progress//message.progress//50f
                        }
                        progressAnim.resume()
                    }
                }
            }
            else {
                Timber.e("Progresss === ${message.progress}, ${message.duration}")
                progressAnim.pause()

                if (message.progress != 0f) {
                    val progress = message.progress * 100 / message.duration
                    itemView.ivVoiceTrack.progress = progress//message.progress//50f
                }
                else {
                    itemView.ivVoiceTrack.progress = 0f
                }
            }
        }
    }


    class ChatsSingleOpponentVoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentVoiceViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_voice, parent, false)
                return ChatsSingleOpponentVoiceViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOpponentVoiceViewHolder bind !!!!")
            if (message.isChecked) {
                itemView.layoutChatSingleVoiceOpponentRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleVoiceOpponentRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            if (((message)as VoiceObject).playing) {
                itemView.btnPlayVoiceOpponent.setImageResource(R.drawable.ic_btn_pause)
            }
            else {
                itemView.btnPlayVoiceOpponent.setImageResource(R.drawable.ic_btn_play)
            }

            itemView.tvVoiceOpponentMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())


            val file = File(itemView.context.cacheDir, message.message.media.sid)
            Timber.e("is file exist = $file, ${file.exists()}, length = ${file.length()}")

            // set waveform image
            Timber.e("setRawData, bytesArray = ${message.bytesArray}, size = ${message.bytesArray.size}")
            itemView.ivVoiceTrack.setRawData(message.bytesArray) { }

            var isEnded = true

            val progressAnim: ObjectAnimator by lazy {
                ObjectAnimator.ofFloat(itemView.ivVoiceTrack, "progress", /*0F*/(message.progress * 100/*+50*/) / message.duration, 100F).apply {
                    interpolator = LinearInterpolator()
                    if (message.progress != 0f) {
//                        val dif = message.progress * 100 / message.duration
//                        Timber.e("DIF = $dif, ${dif.toLong()}")
//                        duration = dif.toLong()
                        duration = (message.duration).toLong()
                    }
                    else {
                        duration = (message.duration).toLong()
                    }
                }
            }
            progressAnim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) { isEnded = true }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) { isEnded = false }
            })

            if (message.playing) {
                if (message.duration != 0) {
                    Timber.e("voice duration = ${message.duration}")

                    if (isEnded) {
                        progressAnim.start()
                    } else {
                        if (message.progress != 0f) {
                            val progress = message.progress * 100 / message.duration
                            itemView.ivVoiceTrack.progress = progress//message.progress//50f
                        }
                        progressAnim.resume()
                    }
                }
            }
            else {
                Timber.e("Progresss === ${message.progress}, ${message.duration}")
                progressAnim.pause()

                if (message.progress != 0f) {
                    val progress = message.progress * 100 / message.duration
                    itemView.ivVoiceTrack.progress = progress//message.progress//50f
                }
                else {
                    itemView.ivVoiceTrack.progress = 0f
                }
            }
        }
    }


    class ChatsSingleOwnFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnFileViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_file, parent, false)
                return ChatsSingleOwnFileViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOwnFileViewHolder called, ${message.message.attributes}")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnFilePdfVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnFilePdfVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            itemView.tvOwnFileTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOwnFilePdfRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOwnFilePdfRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            if (message.message.attributes != null) {
                if (message.message.attributes.jsonObject?.has("fileUri") == true) {
                    val fileUri = message.message.attributes?.jsonObject?.getString("fileUri") ?: "-"
                    val fileName = message.message.attributes?.jsonObject?.getString("fileName") ?: "-"
                    itemView.tvOwnFileName.text = fileName
                    Timber.e("FileUri = $fileUri")
                }
            }
        }
    }

    class ChatsSingleOpponentFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentFileViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_file, parent, false)
                return ChatsSingleOpponentFileViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            itemView.tvOpponentFileTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentFilePdfRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentFilePdfRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            if (message.message.attributes != null) {
                if (message.message.attributes.jsonObject?.has("fileUri") == true) {
                    val fileUri = message.message.attributes?.jsonObject?.getString("fileUri") ?: "-"
                    val fileName = message.message.attributes?.jsonObject?.getString("fileName") ?: "-"
                    itemView.tvOpponentFileName.text = fileName
                    Timber.e("FileUri = $fileUri")
                }
            }
        }
    }

    class ChatsSingleOwnContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnContactViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_contact, parent, false)
                return ChatsSingleOwnContactViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnContactVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnContactVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            itemView.tvOwnContactMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOwnContactRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOwnContactRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            if (message.message.attributes != null) {
                if (message.message.attributes.jsonObject?.has("contactName") == true) {
                    val contactName = message.message.attributes?.jsonObject?.getString("contactName") ?: "-"
                    val contactPhone = message.message.attributes?.jsonObject?.getString("contactPhone") ?: "-"
                    itemView.tvOwnContactUserName.text = contactName
//                    itemView.ivOwnContactAvatar.
                    //todo: set contact user's avatar
                    val sid = if (message.message.media != null) {
                        message.message.media.sid ?: ""
                    } else {
                        ""
                    }
                    itemView.ivOwnContactAvatar.loadRoundCornersImage(
                        radius = itemView.context.resources.getDimensionPixelSize(R.dimen.radius_100),
                        onError = R.drawable.ic_profile_placeholder,
                        uri = Uri.fromFile(File(itemView.context.cacheDir, sid))
                    )
                }
            }
        }
    }

    class ChatsSingleOpponentContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentContactViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_contact, parent, false)
                return ChatsSingleOpponentContactViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            itemView.tvOpponentContactMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentContactRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentContactRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            if (message.message.attributes != null) {
                if (message.message.attributes.jsonObject?.has("contactName") == true) {
                    val contactName = message.message.attributes?.jsonObject?.getString("contactName") ?: "-"
                    val contactPhone = message.message.attributes?.jsonObject?.getString("contactPhone") ?: "-"
                    itemView.tvOpponentContactUserName.text = contactName
                    //todo: set contact user's avatar
                    itemView.ivOpponentContactAvatar.loadRoundCornersImage(
                        radius = itemView.context.resources.getDimensionPixelSize(R.dimen.radius_100),
                        onError = R.drawable.ic_profile_placeholder,
                        uri = Uri.fromFile(File(itemView.context.cacheDir, message.message.media.sid))
                    )
                }
            }
        }
    }


    class ChatsSingleOwnMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnMusicViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_music, parent, false)
                return ChatsSingleOwnMusicViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOwnMusicViewHolder bind!")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnMusicVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnMusicVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            itemView.tvMusicOwnMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleMusicRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleMusicRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            itemView.tvMusicOwnTitle.text = (message as MusicObject).title
            itemView.tvMusicOwnName.text = message.name
            itemView.tvMusicOwnDuration.text = (itemView.context string R.string.music_start_time_).plus(message.duration)

            var mp = MediaPlayer()
            var isRun: Boolean
            val handler = Handler()

            fun setInactive() {
                isRun = false
                message.playing = false
                try {
                    mp.stop()
                    mp.release()
                } catch (e:java.lang.IllegalStateException) {
                    Timber.e("MP STOP CATCH ${e.message}")
                }
                itemView.btnPlayMusicOwn.setImageResource(R.drawable.ic_btn_play)
                itemView.seekBarMusicOwn.hide()
                itemView.seekBarMusicOwn.progress = 0
                itemView.tvMusicOwnDuration.text = (itemView.context string R.string.music_start_time_).plus(message.duration)
            }

            itemView.setOnClickListener {

                Timber.e("Filee exists = ${File(message.path).exists()}")

                fun play(path: String) {
                    message.playing = message.playing.not()

                    if (message.playing.not()) {
                        setInactive()
                    }
                    else {
                        mp = MediaPlayer()
                        mp.reset()
                        try {
                            mp.setDataSource(path)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        mp.prepareAsync()

                        mp.setOnCompletionListener {
                            setInactive()
                        }
                        mp.setOnPreparedListener {
                            mp.start()
                            isRun = true
                            itemView.seekBarMusicOwn.show()
                            itemView.btnPlayMusicOwn.setImageResource(R.drawable.ic_btn_pause)
                            itemView.seekBarMusicOwn.progress = 0
                            itemView.seekBarMusicOwn.max = TimeUnit.MILLISECONDS.toSeconds(mp.duration.toLong()).toInt()

                            (itemView.context as ChatSingleActivity).runOnUiThread(object: Runnable {
                                override fun run() {
                                    if(isRun) {
                                        val mCurrentPosition = mp.currentPosition / 1000
                                        itemView.seekBarMusicOwn.progress = mCurrentPosition

                                        itemView.tvMusicOwnDuration.text = calcDurationTime(mCurrentPosition, message.duration)

                                        handler.postDelayed(this, 1000)
                                    }
                                }
                            })
                        }
                        itemView.seekBarMusicOwn.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                                Timber.e("onProgressChanged = $progress, message.duration = ${message.duration}")

                                if(fromUser){
                                    mp.seekTo(progress * 1000)
                                }

                                itemView.tvMusicOwnDuration.text = calcDurationTime(progress, message.duration)
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {}
                            override fun onStopTrackingTouch(p0: SeekBar?) {}
                        })
                    }
                }


                if (File(message.path).exists().not()) {    // check if file exists from creator's uri

                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(path.toString().plus("/").plus(message.name))
                    Timber.e("Find file to store = ${file.path}, ${file.exists()}")

                    if (file.exists().not() || file.length() == 0L) {   // check if file exists in downloads folder

                        val musicSizeInBytes = message.musicSizeInBytes.toInt()
                        Timber.e("musicSizeInBytes = $musicSizeInBytes")

                        itemView.ownMusicProgressBar.progressDrawable = itemView.context drawable R.drawable.circular
                        itemView.ownMusicProgressBar.max = musicSizeInBytes
                        itemView.ownMusicProgressBar.secondaryProgress = musicSizeInBytes
                        itemView.ownMusicProgressBar.progress = 0

                        itemView.btnPlayMusicOwn.hide()
                        itemView.ownMusicProgressBar.show()

                        val outStream = FileOutputStream(file)
                        message.message.media.download(outStream, object: StatusListener() {
                            override fun onSuccess() {}
                            override fun onError(errorInfo: ErrorInfo?) {
                                Timber.e("download, onError = $errorInfo")
                                itemView.ownMusicProgressBar.hide()
                                itemView.btnPlayMusicOwn.show()
                            }
                        }, object: ProgressListener() {
                            override fun onStarted() {}
                            override fun onProgress(p0: Long) {
                                Timber.e("onProgress, long = $p0")
                                itemView.ownMusicProgressBar.progress = p0.toInt()
                            }
                            override fun onCompleted(p0: String?) {
                                Timber.e("onCompleted, p0 = $p0")
                                itemView.ownMusicProgressBar.hide()
                                itemView.btnPlayMusicOwn.show()
                            }
                        })
                    }
                    else {
                        play(file.path)
                    }
                }
                else {
                    play(message.path)
                }
            }

            Timber.e("musicUri = ${message.message.attributes.jsonObject?.getString("musicUri")}")
            Timber.e("musicPath = ${message.path}")
        }
    }


    class ChatsSingleOpponentMusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentMusicViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_music, parent, false)
                return ChatsSingleOpponentMusicViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOpponentMusicViewHolder bind!")
            itemView.tvMusicOpponentMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleMusicOpponentRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleMusicOpponentRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }
            itemView.tvMusicOpponentTitle.text = (message as MusicObject).title
            itemView.tvMusicOpponentName.text = message.name
            itemView.tvMusicOpponentDuration.text = (itemView.context string R.string.music_start_time_).plus(message.duration)

            var mp = MediaPlayer()
            var isRun: Boolean
            val handler = Handler()

            fun setInactive() {
                isRun = false
                message.playing = false
                try {
                    mp.stop()
                    mp.release()
                } catch (e:java.lang.IllegalStateException) {
                    Timber.e("MP STOP CATCH ${e.message}")
                }
                itemView.btnPlayMusicOpponent.setImageResource(R.drawable.ic_btn_play)
                itemView.seekBarMusicOpponent.hide()
                itemView.seekBarMusicOpponent.progress = 0
                itemView.tvMusicOpponentDuration.text = (itemView.context string R.string.music_start_time_).plus(message.duration)
            }

            itemView.setOnClickListener {

                Timber.e("Filee exists = ${File(message.path).exists()}")

                fun play(path: String) {
                    message.playing = message.playing.not()

                    if (message.playing.not()) {
                        setInactive()
                    }
                    else {
                        mp = MediaPlayer()
                        mp.reset()
                        try {
                            mp.setDataSource(path)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        mp.prepareAsync()

                        mp.setOnCompletionListener {
                            setInactive()
                        }
                        mp.setOnPreparedListener {
                            mp.start()
                            isRun = true
                            itemView.seekBarMusicOwn.show()
                            itemView.btnPlayMusicOwn.setImageResource(R.drawable.ic_btn_pause)
                            itemView.seekBarMusicOwn.progress = 0
                            itemView.seekBarMusicOwn.max = TimeUnit.MILLISECONDS.toSeconds(mp.duration.toLong()).toInt()

                            (itemView.context as ChatSingleActivity).runOnUiThread(object: Runnable {
                                override fun run() {
                                    if(isRun) {
                                        val mCurrentPosition = mp.currentPosition / 1000
                                        itemView.seekBarMusicOwn.progress = mCurrentPosition

                                        itemView.tvMusicOwnDuration.text = calcDurationTime(mCurrentPosition, message.duration)

                                        handler.postDelayed(this, 1000)
                                    }
                                }
                            })
                        }
                        itemView.seekBarMusicOwn.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                                Timber.e("onProgressChanged = $progress")

                                if(fromUser){
                                    mp.seekTo(progress * 1000)
                                }

                                itemView.tvMusicOwnDuration.text = calcDurationTime(progress, message.duration)
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {}
                            override fun onStopTrackingTouch(p0: SeekBar?) {}
                        })
                    }
                }


                if (File(message.path).exists().not()) {    // check if file exists from creator's uri

                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(path.toString().plus("/").plus(message.name))
                    Timber.e("Find file to store = ${file.path}, ${file.exists()}")

                    if (file.exists().not() || file.length() == 0L) {   // check if file exists in downloads folder

                        val musicSizeInBytes = message.musicSizeInBytes.toInt()
                        Timber.e("musicSizeInBytes = $musicSizeInBytes")

                        itemView.opponentMusicProgressBar.progressDrawable = itemView.context drawable R.drawable.circular
                        itemView.opponentMusicProgressBar.max = musicSizeInBytes
                        itemView.opponentMusicProgressBar.secondaryProgress = musicSizeInBytes
                        itemView.opponentMusicProgressBar.progress = 0

                        itemView.btnPlayMusicOwn.hide()
                        itemView.opponentMusicProgressBar.show()

                        val outStream = FileOutputStream(file)
                        message.message.media.download(outStream, object: StatusListener() {
                            override fun onSuccess() {}
                            override fun onError(errorInfo: ErrorInfo?) {
                                Timber.e("download, onError = $errorInfo")
                                itemView.opponentMusicProgressBar.hide()
                                itemView.btnPlayMusicOpponent.show()
                            }
                        }, object: ProgressListener() {
                            override fun onStarted() {}
                            override fun onProgress(p0: Long) {
                                Timber.e("onProgress, long = $p0")
                                itemView.opponentMusicProgressBar.progress = p0.toInt()
                            }
                            override fun onCompleted(p0: String?) {
                                Timber.e("onCompleted, p0 = $p0")
                                itemView.opponentMusicProgressBar.hide()
                                itemView.btnPlayMusicOpponent.show()
                            }
                        })
                    }
                    else {
                        play(file.path)
                    }
                }
                else {
                    play(message.path)
                }
            }

            Timber.e("musicUri = ${message.message.attributes.jsonObject?.getString("musicUri")}")
            Timber.e("musicPath = ${message.path}")
        }
    }



    class ChatsSingleOwnVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOwnVideoViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_own_video, parent, false)
                return ChatsSingleOwnVideoViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOwnVideoViewHolder bind!")
            if (UserMetadata.lastCMI >= message.message.messageIndex) {
                itemView.ivOwnVideoVerification.setImageResource(R.drawable.ic_message_verification_mark)
            }
            else {
                itemView.ivOwnVideoVerification.setImageResource(R.drawable.ic_message_unverification_mark)
            }
            itemView.tvOwnVideoMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOwnVideoRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOwnVideoRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            Timber.e("duration == ${(message as VideoObject).duration}")
            itemView.tvOwnVideoDuration.text = message.duration

            var mMediaPlayer: MediaPlayer?
            val mSurfaceHolder: SurfaceHolder? = itemView.surfaceOwnVideo.holder

            mSurfaceHolder?.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    holder.removeCallback(this)
                }
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer?.setDisplay(mSurfaceHolder)
                    mMediaPlayer?.setOnCompletionListener {
                        itemView.btnPlayVideoOwn.show()
                    }
                    Timber.e("(message as VideoObject).path = ${message.path}")
                    try {
                        mMediaPlayer?.setDataSource(message.path)
                        mMediaPlayer?.prepare()
                        mMediaPlayer?.start()
                        mMediaPlayer?.pause()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }


    class ChatsSingleOpponentVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleOpponentVideoViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_opponent_video, parent, false)
                return ChatsSingleOpponentVideoViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            Timber.e("ChatsSingleOpponentVideoViewHolder bind!")
            itemView.tvOpponentVideoMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            if (message.isChecked) {
                itemView.layoutChatSingleOpponentVideoRoot.setBackgroundColor(itemView.context color R.color.colorSelectedMessageBackground)
            }
            else {
                itemView.layoutChatSingleOpponentVideoRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            Timber.e("duration == ${(message as VideoObject).duration}")
            itemView.tvOpponentVideoDuration.text = message.duration

            var mMediaPlayer: MediaPlayer?
            val mSurfaceHolder: SurfaceHolder? = itemView.surfaceOpponentVideo.holder

            mSurfaceHolder?.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    holder.removeCallback(this)
                }
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer?.setDisplay(mSurfaceHolder)
                    mMediaPlayer?.setOnCompletionListener {
                        itemView.btnPlayVideoOpponent.show()
                    }
                    Timber.e("(message as VideoObject).path = ${message.path}")
                    try {
                        mMediaPlayer?.setDataSource(message.path)
                        mMediaPlayer?.prepare()
                        mMediaPlayer?.start()
                        mMediaPlayer?.pause()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }


    class ChatsSingleCallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?) : ChatsSingleCallViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_single_chat_call, parent, false)
                return ChatsSingleCallViewHolder(v)
            }
        }

        fun bind(message: CommonMessageObject) {
            itemView.tvMessageTime.text = getTimeForMessageDate(message.message.dateCreatedAsDate.toString())
            val attrs = message.message.attributes
            val status = attrs.jsonObject?.getString("callStatus")
            itemView.tvCallStatus.text = status
            val duration = attrs.jsonObject?.getString("callDuration")
            if (duration?.isEmpty() == true) {
                itemView.tvCallDuration.hide()
            }
            else {
                itemView.tvCallDuration.show()
                itemView.tvCallDuration.text = duration
            }
        }
    }
}