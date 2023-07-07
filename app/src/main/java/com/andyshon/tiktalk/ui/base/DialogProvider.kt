package com.andyshon.tiktalk.ui.base

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.presentation.PresentationComponentProvider
import com.andyshon.tiktalk.ui.widget.dialog.ProgressDialog

class DialogProvider {

    var progressDialog: ProgressDialog? = null

    //==============================================================================================
    // Progress Dialog
    //==============================================================================================
    //region methods
    fun showProgress(presentationComponentProvider: PresentationComponentProvider): ProgressDialog {
        return showProgress(presentationComponentProvider, R.string.dialog_action_loading)
    }

    @SuppressLint("ResourceType")
    fun showProgress(presentationComponentProvider: PresentationComponentProvider, @StringRes description: Int): ProgressDialog {
        return showProgress(
            presentationComponentProvider,
            if (description > 0) presentationComponentProvider.provideActivity().getString(
                description
            ) else null
        )
    }

    fun showProgress(
        presentationComponentProvider: PresentationComponentProvider,
        description: String?
    ): ProgressDialog {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            if (progressDialog != null) progressDialog!!.dismiss()
            progressDialog = ProgressDialog.newInstance(description!!)
            progressDialog!!.show(presentationComponentProvider.provideSupportFragmentManager())
        } else {
            progressDialog!!.setDescription(description!!)
        }
        progressDialog!!.isCancelable = false

        return progressDialog as ProgressDialog
    }

    fun isShown(): Boolean {
        return progressDialog != null && progressDialog!!.isShowing
    }

    fun dismissProgress() {
        if (progressDialog != null) {
            dismissDialog(progressDialog)
            progressDialog = null
        }
    }

    private fun dismissDialog(dialogFragment: DialogFragment?) {
        if (dialogFragment == null) return
        try {
            dialogFragment.dismissAllowingStateLoss()
        } catch (ex: Exception) {
        }

    }
}