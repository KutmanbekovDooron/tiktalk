package com.andyshon.tiktalk.ui.editProfile.moreLanguages

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import kotlinx.android.synthetic.main.activity_more_languages.*
import kotlinx.android.synthetic.main.app_toolbar_title_more_languages.*
import java.util.*
import javax.inject.Inject

class MoreLanguagesActivity : BaseInjectActivity(), MoreLanguagesContract.View {

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: MoreLanguagesPresenter
    @Inject lateinit var prefs: PreferenceManager

    private var adapter: MoreLanguagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_more_languages)

        initListeners()

        showProgress()
        if (LanguagesMetadata.languages.isEmpty()) {
            Handler().postDelayed({
                fillList()
                setupList()
            }, 250)
        }
        else {
            Handler().postDelayed({
                setupList()
            }, 250)
        }
    }

    override fun updatedUser() {
        prefs.removeValue(Preference.KEY_USER_I_SPEAK)
        prefs.putStringSet(Preference.KEY_USER_I_SPEAK, presenter.selectedLanguages)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun initPrefs() {
        presenter.selectedLanguages = prefs.getStringSet(Preference.KEY_USER_I_SPEAK) ?: mutableSetOf()

        if (presenter.selectedLanguages.isNotEmpty()) {
            for (i in presenter.selectedLanguages.withIndex()) {
                LanguagesMetadata.languages.forEach {
                    val element = presenter.selectedLanguages.elementAt(i.index)
                    if (it.value.contains(element)) {
                        it.checked = true
                        presenter.selectedLanguages.add(it.value)
                    }
                }
            }
            adapter?.notifyDataSetChanged()
        }
    }

    private fun initListeners() {
        btnToolbarBack.setOnClickListener {
            finish()
        }
        btnToolbarApply.setOnClickListener {
            presenter.selectedLanguages.clear()
            adapter?.getSelectedItems()?.forEach {
                if (it.checked) {
                    presenter.selectedLanguages.add(it.value)
                }
            }
            presenter.updateLanguages()
        }
    }

    private fun fillList() {
        if (LanguagesMetadata.languages.isEmpty()) {
            Locale.setDefault(Locale.ENGLISH)
            val locales = Locale.getAvailableLocales()
            locales.forEach {
                presenter.set.add(it.displayLanguage)
            }
            presenter.set.forEach {
                LanguagesMetadata.languages.add(MoreLanguagesItem(it))
            }
        }
    }

    private fun setupList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MoreLanguagesAdapter(LanguagesMetadata.languages, setClickListener())
        recyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
        hideProgress()

        initPrefs()
    }

    private fun setClickListener(): ItemClickListener<MoreLanguagesItem> {
        return object: ItemClickListener<MoreLanguagesItem> {
            override fun onItemClick(view: View, pos: Int, item: MoreLanguagesItem) {
                item.checked = item.checked.not()
                if (item.checked) {
                    adapter?.selectItem(pos)
                }
                else {
                    adapter?.unselectItem(pos)
                }
                adapter?.notifyItemChanged(pos)
            }
        }
    }
}
