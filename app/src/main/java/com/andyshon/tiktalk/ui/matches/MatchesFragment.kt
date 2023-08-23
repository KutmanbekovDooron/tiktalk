package com.andyshon.tiktalk.ui.matches

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.matches.chat.MatchesChatListener
import com.andyshon.tiktalk.utils.extensions.*
import com.twilio.chat.Channel
import kotlinx.android.synthetic.main.app_toolbar_matches_user_page.*
import kotlinx.android.synthetic.main.fragment_mathces_user_page.*
import kotlinx.android.synthetic.main.layout_empty_matches.*
import kotlinx.android.synthetic.main.layout_user_page_preferences.*
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject

class MatchesFragment : BaseInjectFragment(), MatchesContract.View, MatchesChatListener {

    @Inject
    lateinit var presenter: MatchesPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    private var listener: MatchesListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mathces_user_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)

        initListeners()
        setupToolbar()
        Handler().postDelayed({
            presenter.getMatchUsers()
        }, 100)//3000)
    }

    override fun onMatchUsersLoaded() {
        rootNestedScroll.show()
        btnMatches.show()
        toolbarMatchesSettings.show()
        layoutNoOneNewAroundYou.hide()
        Timber.e("onMatchUsersLoaded called 1")
        if (presenter.matchUsers.isNotEmpty()) {
            Timber.e("onMatchUsersLoaded called 2")
            if (presenter.matchUsers[presenter.currentUserPointer].images.isNotEmpty()) {
                avatar.loadRoundCornersImageWithFallback(
                    radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_10),
                    url = presenter.matchUsers[presenter.currentUserPointer].images.firstOrNull()?.url
                        ?: ""
                )
            }
            tvUserNameAndAge.text =
                presenter.matchUsers[presenter.currentUserPointer].name.plus(", ")
                    .plus(presenter.matchUsers[presenter.currentUserPointer].birthDate)
            tvUserHobby.text = presenter.matchUsers[presenter.currentUserPointer].country

            populateUsersData()
        }
    }

    private fun populateUsersData() {
        Timber.e("onMatchUsersLoaded called 3")
        val user = presenter.matchUsers[presenter.currentUserPointer]
        btnReport.text = "Report ".plus(user.name)
        if (user.height != null) {
            view1.setText(user.height)
        }
        if (user.drinking != "no_answer") {
            view2.setText(user.drinking)
        }
        if (user.gender == "male") {
            view3.setText("Men")
        } else {
            view3.setText("Women")
        }
        if (user.smoking != "no_answer") {
            view4.setText(user.smoking)
        }
        if (user./*relationship*/sexuality != "no_answer") {
            view5.setText(user./*relationship*/sexuality)
        }
//        if (user.pets != "no_answer") {
//            view6.setText(user.sexuality)
//        }
//        if (user.sigittarius != "no_answer") {
//            view7.setText(user.sexuality)
//        }
        if (user.children != "no_answer") {
            view8.setText(user.children)
        }
    }

    override fun noOneNewAroundYou() {
        rootNestedScroll.hide()
        toolbarMatchesSettings.hide()
        btnMatches.show()
        layoutNoOneNewAroundYou.show()
        Timber.e("UserMetadata.photos.first().url = ${UserMetadata.photos.first().url}")
        noOneNewAroundYouAvatar.loadRoundCornersImageWithFallback(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_100),
            url = UserMetadata.photos.first().url
        )
    }

    override fun closeMatchesChat() {
        listener?.closeMatchesChat()
    }

    override fun openMatchChat(channel: Channel, chatUser: ChannelModel) {
        listener?.openMatchesChat()
    }

    override fun onLiked() {
        onMatchUsersLoaded()
        //D/OkHttp: {"id":5,"status":"nothing","user_id":13,"receiver_id":15,"created_at":"2019-07-10T16:31:18.101Z","updated_at":"2019-07-10T16:31:18.101Z"}
    }

    override fun onMatched(user: User) {
        Timber.e("onMatched called")
        // It'status a match -> create channel
        val photo =
            if (user.images.isEmpty()) "http://www.sclance.com/pngs/image-placeholder-png/image_placeholder_png_698412.png"
            else user.images.first().url
        TwilioSingleton.instance.createChannel(
            true,
            UserMetadata.userId,
            UserMetadata.userName,
            UserMetadata.userEmail,
            UserMetadata.photos.first().url,
            UserMetadata.userPhone,
            user.id,
            user.name,
            user.email,
            photo,
            user.phoneNumber,
            channelCreated = {
                Timber.e("Matched, Create channel and joined success!")

                onMatchUsersLoaded()
                listener?.openItIsMatch(user)
            }
        )
    }

    override fun onDisliked() {
        Timber.e("onDisliked called")
        onMatchUsersLoaded()
    }

    override fun onSuperliked() {
        Timber.e("onSuperliked called")
        onMatchUsersLoaded()
    }

    override fun onReported() {
        Timber.e("onReported called")
        onMatchUsersLoaded()
    }

    private fun setupToolbar() {
        toolbarMatchesUserAvatar.loadRoundCornersImageWithFallback(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_100),
            url = UserMetadata.photos.first().url
        )
        image1.loadRoundCornersImage(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_10),
            url = "https://i.pinimg.com/originals/65/95/85/6595856323f822a5e9b6411c5d415b49.jpg"
        )
    }

    private fun initListeners() {
        toolbarMatchesUserAvatar.setOnClickListener {
            listener?.openSettings()
        }
        btnMatches.setOnClickListener {
            listener?.openMatchesChat()
        }
        toolbarMatchesSettings.setOnClickListener {
            toast("Settings")
        }
        btnPrevious.setOnClickListener {
            toast("Previous")
        }

        btnIgnore.setOnClickListener {
            presenter.makeAction(Constants.MatchTypes.DISLIKE)
            toast("Dislike")
        }
        btnLike.setOnClickListener {
            presenter.makeAction(Constants.MatchTypes.LIKE)
        }
        btnGift.setOnClickListener {
            presenter.makeAction(Constants.MatchTypes.SUPERLIKE)
            toast("Superlike")
        }
        btnShare.setOnClickListener {
            listener?.openShare(presenter.matchUsers[presenter.currentUserPointer].images.first().url)
        }
        btnReport.setOnClickListener {
            presenter.report()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MatchesListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement MatchesListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}