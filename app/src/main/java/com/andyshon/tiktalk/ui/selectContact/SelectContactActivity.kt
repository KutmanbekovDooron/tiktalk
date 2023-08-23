package com.andyshon.tiktalk.ui.selectContact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.MobileContact
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.network.request.Friend
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.hide
import com.andyshon.tiktalk.utils.extensions.hideKeyboard
import com.andyshon.tiktalk.utils.extensions.show
import com.andyshon.tiktalk.utils.extensions.showKeyboard3
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_select_contact.selectContactRecyclerView
import kotlinx.android.synthetic.main.activity_select_contact.toolbarSelectContact
import kotlinx.android.synthetic.main.activity_select_contact.toolbarSelectContactSearch
import kotlinx.android.synthetic.main.app_toolbar_search_simple.etSearch
import kotlinx.android.synthetic.main.app_toolbar_search_simple.toolbarSearchSimpleBtnBack
import kotlinx.android.synthetic.main.app_toolbar_select_contact.toolbarBtnBack
import kotlinx.android.synthetic.main.app_toolbar_select_contact.toolbarBtnSearch
import timber.log.Timber
import javax.inject.Inject


class SelectContactActivity : BaseInjectActivity(), SelectContactContract.View {

    @Inject
    lateinit var presenter: SelectContactPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    private var adapter: SelectContactAdapter? = null
    private var contacts: ArrayList<MobileContact> = arrayListOf()
    private var selectedContact: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_select_contact)

        initListeners()

        Handler().postDelayed({
            showProgress()
            Thread(Runnable {
                getContactList()
                runOnUiThread {
                    setupList()
                    hideProgress()
                }
            }).start()
        }, 500)
    }

    private fun initListeners() {
        toolbarBtnSearch.setOnClickListener {
            toolbarSelectContact.hide()
            toolbarSelectContactSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarBtnBack.setOnClickListener {
            onBackPressed()
        }
        toolbarSearchSimpleBtnBack.setOnClickListener {
            toolbarSelectContact.show()
            toolbarSelectContactSearch.hide()
            etSearch.hideKeyboard()
            adapter?.showFullList()
        }
        etSearch.doAfterTextChanged {
            it?.let { adapter?.search(it.toString().trim()) }
        }
    }

    private fun setupList() {
        selectContactRecyclerView?.let { it.layoutManager = LinearLayoutManager(this) }
        adapter = SelectContactAdapter(presenter.friends, setClickListener())
        selectContactRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemClickListener<User> {
        return object : ItemClickListener<User> {
            override fun onItemClick(view: View, pos: Int, item: User) {
                selectedContact = item
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("id", item.id)
                    putExtra("name", item.name)
                    putExtra("email", item.email)
                    putExtra("photo", item.images.firstOrNull()?.url ?: "")
                    putExtra("phone", item.phoneNumber)
                })
                finish()
            }
        }
    }

    override fun showFriends() {
        adapter?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun getContactList() {
        val cr = contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)


        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    var no = false
                    while (pCur!!.moveToNext()) {
                        val phoneNo =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Timber.e("Name $name, Phone Number $phoneNo")
                        contacts.forEach {
                            if (it.phoneNumber == phoneNo) {
                                no = true
                            }
                        }
                        if (no.not()) {
                            val phone = if (phoneNo[0] != '0') phoneNo else "+38".plus(phoneNo)
                            contacts.add(MobileContact(name, phone))
                        }
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        Timber.e("contacts size = ${contacts.size}")

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val friends = contacts.map { contact ->
            val countryCode = try {
                val parsedNumber = phoneNumberUtil.parse(contact.phoneNumber, null)
                parsedNumber.countryCode.toString()
            } catch (e: Exception) {
                "+38"
            }

            Friend(
                phoneNumber = contact.phoneNumber,
                countryCode = "+380"
            )
        }
        presenter.getFriends(friends)
    }
}

