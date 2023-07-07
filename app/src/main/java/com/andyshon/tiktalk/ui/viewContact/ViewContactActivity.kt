package com.andyshon.tiktalk.ui.viewContact

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.ui.chatSingle.chatMedia.ChatMediaActivity
import com.andyshon.tiktalk.ui.viewContact.selectContact.ViewContactSelectContactActivity
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import com.twilio.chat.Channel
import kotlinx.android.synthetic.main.activity_view_contact.*
import kotlinx.android.synthetic.main.app_toolbar_view_contact.*
import javax.inject.Inject

class ViewContactActivity : BaseInjectActivity(), ViewContactContract.View {

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: ViewContactPresenter

    private var adapter: MediaAdapter? = null

    private var name = ""
    private var photo = ""
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_view_contact)


        if (intent != null) {
            presenter.channel = intent.getParcelableExtra<Channel>(Constants.EXTRA_CHANNEL)
            name = intent.getStringExtra(Constants.EXTRA_CHANNEL_NAME)
            photo = intent.getStringExtra(Constants.EXTRA_CHANNEL_PHOTO)
            phone = intent.getStringExtra(Constants.EXTRA_CHANNEL_PHONE)
        }

        initListeners()
        fillUI()
        setupList()
        presenter.loadSharedMedia()
    }

    override fun sharedMediaLoaded() {
        adapter?.notifyDataSetChanged()
    }

    private fun initListeners() {
        switcherNotifications.setOnCheckedChangeListener { p0, b ->
            if (b) {
                tvNotificationText.text = "On"
            }
            else {
                tvNotificationText.text = "Off"
            }
        }
        toolbarViewContactBtnBack.setOnClickListener {
            finish()
        }
        toolbarViewContactBtnMessage.setOnClickListener {

        }
        btnShare.setOnClickListener {
            ViewContactSelectContactActivity.startActivity(this)
        }
        btnBlock.setOnClickListener {
            presenter.block(name)
        }
        btnEdit.setOnClickListener {
            presenter.edit(name)
        }
        btnDelete.setOnClickListener {
            presenter.delete(name)
        }
    }

    private fun fillUI() {
        intent?.let {
            if (name.isEmpty()) {
                name = it.getStringExtra("name") ?: ""
                photo = it.getStringExtra("photo") ?: ""
                phone = it.getStringExtra("phone") ?: ""
            }
            etMobile.setText(phone)
            toolbarViewContactName.text = name
            avatar.loadRoundCornersImage(
                radius = resources.getDimensionPixelSize(R.dimen.radius_120),
                url = photo
            )
            avatar.setOnClickListener {
                ChatMediaActivity.startActivity(getActivityContext(), photo, "", "")
            }
        }
    }

    private fun setupList() {
        sharedMediaRecyclerView?.let { it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) }
        adapter = MediaAdapter(presenter.media, setClickListener())
        sharedMediaRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemClickListener<String> {
        return object : ItemClickListener<String> {
            override fun onItemClick(view: View, pos: Int, media: String) {
                ChatMediaActivity.startActivity(getActivityContext(), media, "", "")
            }
        }
    }

    override fun blocked() {

    }

    override fun edited() {

    }

    override fun deleted() {

    }
}
