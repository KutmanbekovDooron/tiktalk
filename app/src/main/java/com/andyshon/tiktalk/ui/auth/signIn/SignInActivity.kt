package com.andyshon.tiktalk.ui.auth.signIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import androidx.core.widget.doAfterTextChanged
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.auth.chooseCountry.ChooseCountryActivity
import com.andyshon.tiktalk.ui.auth.verification.CodeVerificationActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.*
import com.andyshon.tiktalk.utils.phone.getMobilePhoneMaxLength
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SignInActivity : BaseInjectActivity(), SignInContract.View {

    override fun getPresenter(): BaseContract.Presenter<*> = presenter
    @Inject lateinit var presenter: SignInPresenter

    private var mobilePhoneMaxLength = 0
    private var mobileCode = ""
    private var selectedPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

//        presenter.logout()
        initListeners()
        setAgreementClickableSpan()
    }

    private fun initListeners() {
        tvIsoCode.setOnClickListener {
            val intent = Intent(this@SignInActivity, ChooseCountryActivity::class.java)
            intent.putExtra("position", selectedPos)
            startActivityForResult(intent, 444)
        }
        btnToolbarBack.setOnClickListener { finish() }
        etPhoneNumber.doAfterTextChanged {
            tvIncorrectNumber.hide()
            btnVerifyPhone.isEnabled = it?.isNotEmpty() != false && (it?.length ?: 0) == mobilePhoneMaxLength
            if (it?.isNotEmpty() != false) {
                btnClearNumber.show()
            } else btnClearNumber.hide()
        }
        btnVerifyPhone.setOnClickListener {
            if (canOpen().not()) return@setOnClickListener

            val fullNumber = "+".plus(mobileCode).plus(etPhoneNumber.text.toString())
            presenter.sendSms(fullNumber)

//            onSmsSent()

        }
        btnClearNumber.setOnClickListener { etPhoneNumber.setText("") }

        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso.toUpperCase()

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val code = phoneNumberUtil.getCountryCodeForRegion(countryCodeValue)
        mobileCode = code.toString()
        setMaxNumberLength("+".plus(mobileCode))
    }

    override fun onSmsSent() {
        CodeVerificationActivity.startActivity(this, "+".plus(mobileCode).plus(etPhoneNumber.text.toString()))
    }

    override fun onError() {
        setIncorrectNumber()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                444 -> {
                    val code = data?.getStringExtra("code") ?: "-"
                    tvIsoCode.text = code
                    setMaxNumberLength(code)
                }
            }
        }
    }

    private fun setMaxNumberLength(lastSelectedPhoneCode: String) {
        Timber.e("lastSelectedPhoneCode = $lastSelectedPhoneCode")
        val split = lastSelectedPhoneCode.split("+")
        mobileCode = split.last().trim()
        val countryCode = split[split.lastIndex]
        mobilePhoneMaxLength = getMobilePhoneMaxLength(countryCode.trim().toInt())
        etPhoneNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mobilePhoneMaxLength))
    }

    private fun setAgreementClickableSpan() {
        val spannableText = SpannableStringBuilder()
        spannableText.append(this string R.string.start_with_terms)
        val termsOfUse = this string R.string.start_with_terms_part1
        spannableText.addClickableSpannable(termsOfUse, this color R.color.colorGrey) {
            //termsOfUse click
        }
        val copyright = this string R.string.start_with_terms_part2
        spannableText.addClickableSpannable(copyright, this color R.color.colorGrey) {
            //copyright click
        }
        if (copyright == "Terms of Service") {
            spannableText.setSpan(UnderlineSpan(), spannableText.length-37, spannableText.length-23, 0)
            spannableText.setSpan(UnderlineSpan(), spannableText.length-15, spannableText.length, 0)
        }
        else {
            spannableText.setSpan(UnderlineSpan(), spannableText.length-35, spannableText.length-19, 0)
            spannableText.setSpan(UnderlineSpan(), spannableText.length-14, spannableText.length, 0)
        }
        tvSignInTerms.text = spannableText
        tvSignInTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setIncorrectNumber() {
        tvIncorrectNumber.show()
    }
}
