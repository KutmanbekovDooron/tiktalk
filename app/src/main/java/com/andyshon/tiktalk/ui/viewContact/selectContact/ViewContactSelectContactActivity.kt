package com.andyshon.tiktalk.ui.viewContact.selectContact

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.MobileContact
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import android.provider.ContactsContract
import timber.log.Timber
import android.content.Context
import android.os.Handler
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.hide
import com.andyshon.tiktalk.utils.extensions.hideKeyboard
import com.andyshon.tiktalk.utils.extensions.show
import com.andyshon.tiktalk.utils.extensions.showKeyboard3
import kotlinx.android.synthetic.main.activity_select_contact.toolbarSelectContact
import kotlinx.android.synthetic.main.activity_select_contact.toolbarSelectContactSearch
import kotlinx.android.synthetic.main.activity_view_contact_select_contact.*
import kotlinx.android.synthetic.main.app_toolbar_search_simple.*
import kotlinx.android.synthetic.main.app_toolbar_select_contact.*
import javax.inject.Inject
import org.jetbrains.anko.dip
import java.lang.StringBuilder

class ViewContactSelectContactActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, ViewContactSelectContactActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: ViewContactSelectContactPresenter

    private var adapter: ViewContactSelectContactAdapter? = null
    private var contacts: ArrayList<MobileContact> = arrayListOf()
    private var selectedContact: MobileContact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_view_contact_select_contact)

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
        }, 250)
    }

    private fun initListeners() {
        toolbarBtnSearch.setOnClickListener {
            toolbarSelectContact.hide()
            toolbarSelectContactSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarBtnBack.setOnClickListener {
//            setResult(Activity.RESULT_CANCELED)
            finish()
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
        viewContactSelectContactRecyclerView?.let { it.layoutManager = LinearLayoutManager(this) }
        adapter = ViewContactSelectContactAdapter(contacts, setClickListener())
        viewContactSelectContactRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemClickListener<MobileContact> {
        return object : ItemClickListener<MobileContact> {
            override fun onItemClick(view: View, pos: Int, item: MobileContact) {
                var text = ""
                if (item.isChecked) {
                    presenter.checkedChats++
                } else {
                    presenter.checkedChats--
                }
                if (presenter.checkedChats >= 1) {
                    setMargin(40)
                    layoutSendContact.show()
                    val s0 = arrayListOf<String>()
                    adapter?.names?.forEach {
                        s0.add(it)
                    }
                    val sb = StringBuilder()
                    s0.forEach {
                        if (s0.size == 1) {
                            sb.append(it.plus(" "))
                            text = sb.toString()
                        }
                        else {
                            sb.append(it.plus("; "))
                        }
                    }
                    if (s0.size > 1) {
                        text = sb.toString()
                        text = text.substring(0, text.length - 2)
                    }
                    tvSendContact.text = text
                } else {
                    setMargin(0)
                    layoutSendContact.hide()
                    adapter?.selectedModeOff()
                }
//                adapter?.notifyItemChanged(pos)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setMargin(bottom: Int) {
        val params = viewContactSelectContactRecyclerView.layoutParams as FrameLayout.LayoutParams
        params.setMargins(0, 0, 0, dip(bottom))
        viewContactSelectContactRecyclerView.layoutParams = params
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
                        val phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Timber.e("Name $name, Phone Number $phoneNo")
                        contacts.forEach {
                            if (it.phoneNumber == phoneNo) {
                                no = true
                            }
                        }
                        if (no.not()) {
                            contacts.add(MobileContact(name, phoneNo))
                        }
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        Timber.e("contacts size = ${contacts.size}")
    }
}
