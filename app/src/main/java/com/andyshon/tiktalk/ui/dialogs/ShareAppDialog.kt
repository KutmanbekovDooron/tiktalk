package com.andyshon.tiktalk.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.andyshon.tiktalk.R
import android.view.WindowManager
import android.widget.ImageView

class ShareAppDialog : DialogFragment() {

    private var shareAppClickListener: ShareAppClickListener? = null
    interface ShareAppClickListener {
        fun shareWhatsApp()
        fun shareTelegram()
        fun shareSMS()
        fun shareSkype()
        fun shareMail()
        fun shareMessenger()
    }

    fun show(manager: FragmentManager) {
        show(manager, this.javaClass.name)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog.window != null) {
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val window = dialog?.window ?: return
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE/*SOFT_INPUT_STATE_VISIBLE*/)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.setLayout(MATCH_PARENT, WRAP_CONTENT)

        val layoutparam = window.attributes
        layoutparam.gravity = Gravity.BOTTOM
//        layoutparam.verticalMargin = 150f
        layoutparam.flags = layoutparam.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = layoutparam

        window.attributes.windowAnimations = R.style.DialogAnimationAlphaFast
        window.setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.color./*colorPrimaryTransparent*/colorTransparent))
        // hide status bar while progress is showing
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_share_app_bottom_sheet, container, false)

        view.findViewById<ImageView>(R.id.btnShareWhatsApp).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareWhatsApp()
        }
        view.findViewById<ImageView>(R.id.btnShareTelegram).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareTelegram()
        }
        view.findViewById<ImageView>(R.id.btnShareSms).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareSMS()
        }
        view.findViewById<ImageView>(R.id.btnShareSkype).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareSkype()
        }
        view.findViewById<ImageView>(R.id.btnShareMail).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareMail()
        }
        view.findViewById<ImageView>(R.id.btnShareMessenger).setOnClickListener {
            dismiss()
            shareAppClickListener?.shareMessenger()
        }

        return view
    }

    companion object {
        fun newInstance() = ShareAppDialog()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            shareAppClickListener = context as ShareAppClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement ShareAppClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        shareAppClickListener = null
    }
}