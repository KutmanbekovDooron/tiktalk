package com.andyshon.tiktalk.ui.payments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.viewpager.widget.ViewPager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.string
import kotlinx.android.synthetic.main.activity_payment_settings_plus.*
import kotlinx.android.synthetic.main.app_toolbar_title_gradient.*

class PaymentSettingsPlusActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, PaymentSettingsPlusActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_payment_settings_plus)


        initUI()
//        initViewPager()
    }

    private fun initUI() {
        toolbarBtnBack.setOnClickListener {
            finish()
        }
        toolbarTvTitle.text = this string R.string.payment_settings_title
        btnGetTikTalkPlus.setOnClickListener {
//            if (canOpen())
        }
    }

    private fun initViewPager() {
        pageIndicatorView?.count = 3
        pageIndicatorView?.selection = 0

        viewPager.adapter = PaymentsViewPagerAdapter(supportFragmentManager).apply {
            addFragment(PaymentSlideFragment.newInstance(""))
            addFragment(PaymentSlideFragment.newInstance(""))
            addFragment(PaymentSlideFragment.newInstance(""))
            addFragment(PaymentSlideFragment.newInstance(""))
            addFragment(PaymentSlideFragment.newInstance(""))
            addFragment(PaymentSlideFragment.newInstance(""))
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {/*empty*/
            }

            override fun onPageSelected(position: Int) {
                pageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {/*empty*/
            }
        })
    }
}
