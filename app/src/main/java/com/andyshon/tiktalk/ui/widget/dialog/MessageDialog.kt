package com.andyshon.tiktalk.ui.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.andyshon.tiktalk.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_message.*

class MessageDialog : BottomSheetDialogFragment() {

    companion object {

        private const val ARG_MESSAGE = "message"
        private const val ARG_ACCEPT_TEXT = "accept"

        fun newInstance(message: String, acceptText: String? = null): MessageDialog {
            val fragment = MessageDialog()
            val args = Bundle()

            args.putString(ARG_MESSAGE, message)
            args.putString(ARG_ACCEPT_TEXT, acceptText)

            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater?.inflate(R.layout.dialog_message, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = arguments?.getString(ARG_MESSAGE)
        if (message != null) {
            dialogMessageText.text = message
        }

        val acceptText = arguments?.getString(ARG_ACCEPT_TEXT)
        if (acceptText != null) {
            dialogBtnAccept.text = acceptText
        }

        dialogBtnAccept.setOnClickListener {
            dismiss()
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, MessageDialog::class.java.name)
    }
}