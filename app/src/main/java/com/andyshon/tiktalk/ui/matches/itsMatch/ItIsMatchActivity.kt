package com.andyshon.tiktalk.ui.matches.itsMatch

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import kotlinx.android.synthetic.main.activity_it_is_match.*
import kotlinx.android.synthetic.main.layout_match_text_message.*

class ItIsMatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_it_is_match)

        initListeners()

        val id = intent?.getStringExtra("id") ?: ""
        val img = intent?.getStringExtra("photo") ?: ""
    }

    private fun initListeners() {
        btnContinue.setOnClickListener {
            val intent = Intent()
            intent.putExtra("type", 1)
            setResult(Activity.RESULT_OK, intent)

        }
        btnSendMessage.setOnClickListener {
            val intent = Intent()
            intent.putExtra("type", 2)
            setResult(Activity.RESULT_OK, intent)
        }
    }
}
