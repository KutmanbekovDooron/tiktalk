package com.andyshon.tiktalk.ui.auth.chooseCountry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.string
import com.andyshon.tiktalk.utils.phone.CountriesMetadata
import com.andyshon.tiktalk.utils.phone.CountryEmojie
import kotlinx.android.synthetic.main.activity_choose_country.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import timber.log.Timber
import android.telephony.TelephonyManager
import com.andyshon.tiktalk.utils.phone.getCountryNameByRegion

class ChooseCountryActivity : BaseInjectActivity() {

    private var currentPosition = 0
    private var adapter: PhoneAdapter? = null

    override fun getPresenter(): BaseContract.Presenter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_country)

        showProgress()
        currentPosition = intent?.getIntExtra("position", 0) ?: 0
        Handler().postDelayed({
            setupList()
        }, 150)
        tvToolbarTitle.text = this string R.string.choose_country_title
        btnToolbarBack.setOnClickListener { finish() }
    }

    private fun setupList() {
        Thread(Runnable {
            val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCodeValue = tm.simCountryIso
            val userCountry = getCountryNameByRegion(countryCodeValue)

            val list = CountriesMetadata.countriesWithEmojies
            runOnUiThread {
                adapter = PhoneAdapter(list, setClickListener())
                CountriesRecycler.adapter = adapter
                CountriesRecycler.setHasFixedSize(true)
                CountriesRecycler.layoutManager = LinearLayoutManager(this)

                list.forEach {
                    if (it.country == userCountry) {
                        currentPosition = list.indexOf(it)
                    }
                }
                list[currentPosition].isChecked = true
                adapter?.notifyItemChanged(currentPosition)
                CountriesRecycler.scrollToPosition(currentPosition)
                hideProgress()
            }
        }).start()
    }

    private fun setClickListener(): ItemClickListener<CountryEmojie> {
        return object : ItemClickListener<CountryEmojie> {
            override fun onItemClick(view: View, pos: Int, item: CountryEmojie) {
                currentPosition = pos
                Timber.e("clickcc = $currentPosition, code = ${item.code}")

                val result = item.isoCode.plus(" +").plus(item.code)
                Timber.e("result = $result")
                val intent = Intent().apply { putExtra("code", result) }
                setResult(Activity.RESULT_OK, intent)
                this@ChooseCountryActivity.finish()
            }
        }
    }
}
