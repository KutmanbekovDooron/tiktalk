package com.andyshon.tiktalk.ui.matches.share

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.activity_matches_share.*

class MatchesShareActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context, photo: String) {
            val intent = Intent(context, MatchesShareActivity::class.java)
            intent.putExtra("photo", photo)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_matches_share)

        avatar.loadRoundCornersImage(
            radius = resources.getDimensionPixelSize(R.dimen.radius_10),
            url = intent.getStringExtra("photo")?:""
        )
        btnCancel.setOnClickListener { finish() }
    }
}
