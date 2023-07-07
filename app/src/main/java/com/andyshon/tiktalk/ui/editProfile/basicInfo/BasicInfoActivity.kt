package com.andyshon.tiktalk.ui.editProfile.basicInfo

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.dialogs.DatePickerFragmentDialog
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import kotlinx.android.synthetic.main.activity_basic_info.*
import kotlinx.android.synthetic.main.app_toolbar_edit_profile_basic_info.*
import timber.log.Timber
import javax.inject.Inject

class BasicInfoActivity : BaseInjectActivity(), BasicInfoContract.View, DatePickerFragmentDialog.DatePickerListener {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, BasicInfoActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject lateinit var presenter: BasicInfoPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = null
    @Inject lateinit var prefs: PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_basic_info)

        presenter.name = prefs.getObject(Preference.KEY_USER_NAME, String::class.java) ?: ""
        presenter.gender = prefs.getObject(Preference.KEY_USER_GENDER, String::class.java) ?: "male"
        presenter.birthDate = prefs.getObject(Preference.KEY_USER_BIRTH_DATE, String::class.java) ?: ""
        Timber.e("birthDate = ${presenter.birthDate}")
        initListeners()
    }

    private fun initListeners() {
        toolbarBtnBack.setOnClickListener {
            finish()
        }
        toolbarBtnApply.setOnClickListener {
            Timber.e("birthDate update = ${presenter.birthDate}")
            presenter.updateUser()
        }
        switcherGender.isChecked = presenter.gender == "female"
        if (switcherGender.isChecked) {
            setFemale()
        } else {
            setMale()
        }
        switcherGender.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                setFemale()
            } else {
                setMale()
            }
        }
        etDateOfBirth.setText(presenter.birthDate)    // "birth_date":"1998-07-20T00:00:00.000Z" but need dd-MM-yyyy
        etDateOfBirth.setOnClickListener {
            val dialog = DatePickerFragmentDialog()
            showPickerDialog(dialog)
        }
        etName.setText(UserMetadata.userName)
        presenter.name = UserMetadata.userName
        etName.setOnClickListener {
            presenter.changeName {
                etName.setText(it)
                presenter.name = it
            }
        }
    }

    private fun setFemale() {
        presenter.gender = "female"
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_regular)
        tvMale.setTypeface(typeface, Typeface.NORMAL)
        tvFemale.setTypeface(tvMale.typeface, Typeface.BOLD)
    }

    private fun setMale() {
        presenter.gender = "male"
        tvMale.setTypeface(tvMale.typeface, Typeface.BOLD)
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_regular)
        tvFemale.setTypeface(typeface, Typeface.NORMAL)
    }

    override fun updatedUser() {
        finish()
    }

    private fun showPickerDialog(dialog: DialogFragment) {
        dialog.show(supportFragmentManager, "")
        if (etDateOfBirth.text.isNotEmpty()) {
            (dialog as DatePickerFragmentDialog).setDateFromString(etDateOfBirth.text.toString().trim())
        }
    }

    override fun onDatePicked(year: Int, month: Int, day: Int) {
        etDateOfBirth.setText("$day-${month + 1}-$year")
        presenter.birthDate = "$day-${month + 1}-$year"
    }
}
