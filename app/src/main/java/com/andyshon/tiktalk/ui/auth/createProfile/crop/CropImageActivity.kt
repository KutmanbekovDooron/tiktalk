package com.andyshon.tiktalk.ui.auth.createProfile.crop

import android.os.Bundle
import android.graphics.Bitmap
import android.net.Uri
import com.isseiaoki.simplecropview.CropImageView
import com.isseiaoki.simplecropview.callback.LoadCallback
import android.content.Intent
import com.isseiaoki.simplecropview.callback.CropCallback
import android.app.Activity
import android.widget.Toast
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.isseiaoki.simplecropview.callback.SaveCallback
import kotlinx.android.synthetic.main.activity_crop_my_image.*
import timber.log.Timber

class CropImageActivity : BaseInjectActivity() {

    private var mSourceUri: Uri? = null
    override fun getPresenter(): BaseContract.Presenter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_my_image)

        mSourceUri = intent.data

        cropImageView.setCropMode(CropImageView.CropMode.RATIO_3_4)
        cropImageView.load(mSourceUri)
            .useThumbnail(false)
            .execute(mLoadCallback)

        buttonDone.setOnClickListener {
            showProgress()
            cropImageView.crop(mSourceUri).execute(mCropCallback)
        }
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    private val mLoadCallback = object : LoadCallback {
        override fun onSuccess() {
//            hideProgress()
        }

        override fun onError(e: Throwable) {
            hideProgress()
            Toast.makeText(this@CropImageActivity, "Can't perform load, ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val mCropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap) {
            cropImageView.save(cropped)
                .compressFormat(Bitmap.CompressFormat.JPEG)
                .execute(mSourceUri, mSaveCallback)
        }

        override fun onError(e: Throwable) {
            hideProgress()
            Toast.makeText(this@CropImageActivity, "Can't perform crop, ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val mSaveCallback = object : SaveCallback {
        override fun onSuccess(outputUri: Uri) {
            Timber.e("Crop, onSuccess = $outputUri, ${outputUri.path}")
            hideProgress()
            val i = Intent().apply { data = outputUri }
            setResult(Activity.RESULT_OK, i)
            finish()
        }

        override fun onError(e: Throwable) {
            hideProgress()
            Toast.makeText(this@CropImageActivity, "Perform saving cropped image, ${e.message}", Toast.LENGTH_LONG).show()
            Timber.e("Crop, onError = ${e.message}")
        }
    }
}
