package com.andyshon.tiktalk.ui.auth.verification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import androidx.core.widget.doAfterTextChanged
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.MainActivity
import com.andyshon.tiktalk.ui.auth.createProfile.CreateProfileActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.activity_code_verification.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import kotlinx.android.synthetic.main.layout_auth_blocked.*
import javax.inject.Inject

class CodeVerificationActivity : BaseInjectActivity(), CodeVerificationContract.View {

    companion object {
        fun startActivity(context: Context, phone: String) {
            val intent = Intent(context, CodeVerificationActivity::class.java)
            intent.putExtra("phone", phone)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: CodeVerificationPresenter

    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_verification)

        phone = intent?.getStringExtra("phone") ?: ""

        tvToolbarTitle.text = this string R.string.code_verification_title
        tvSendToPhone.text = getString(R.string.code_verification_send_to_phone, phone)

        initListeners()
    }

    override fun onPhoneVerified() {
        CreateProfileActivity.startActivity(this)
    }

    override fun onUserReturn() {
        //todo: skip creation profile
        val intent = Intent(this@CodeVerificationActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onIncorrectCode() {
        tvIncorrectCode.show()
        codeEdit.setTextColor(this color R.color.colorRed)
        codeEdit.setItemBackground(this drawable R.drawable.bg_pin_btn_error)
    }

    override fun onBlockedAccount() {
        setBlockedAccount()
    }

    private fun initListeners() {
        codeEdit.showKeyboard3()
        codeEdit.doAfterTextChanged {
            tvIncorrectCode.hide()
            codeEdit.setTextColor(getThemeColor(theme, R.attr.colorGreyOrWhite))
//            codeEdit.colorAttr(R.attr.pin_text)
            codeEdit.setItemBackground(this drawable R.drawable.bg_pin_btn)
            btnVerify.isEnabled = it?.length == 4
        }
        btnToolbarBack.setOnClickListener { finish() }
        btnVerify.setOnClickListener {
            if (canOpen()) {
                presenter.verifyPhone(codeEdit.text.toString(), phone)
            }
//            onPhoneVerified()
        }
    }

    private fun setBlockedAccount() {
        setContentView(R.layout.layout_auth_blocked)
        btnBlockedToolbarBack.setOnClickListener { finish() }
        tvBlockedToolbarTitle.text = this string R.string.code_verification_title
        setAgreementClickableSpan()
    }

    private fun setAgreementClickableSpan() {
        val spannableText = SpannableStringBuilder()
        spannableText.append(this string R.string.blocked_account_text)
        val support = this string R.string.blocked_account_contact_support
        spannableText.addClickableSpannable(support, this color R.color.colorGrey) {
            //support click
        }
        spannableText.setSpan(UnderlineSpan(), spannableText.length-8, spannableText.length-1, 0)

        tvBlockedAccount.text = spannableText
        tvBlockedAccount.movementMethod = LinkMovementMethod.getInstance()
    }
}
