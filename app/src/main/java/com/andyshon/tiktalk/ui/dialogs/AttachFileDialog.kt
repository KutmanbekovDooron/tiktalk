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

class AttachFileDialog : DialogFragment() {

    private var attachFileClickListener: AttachFileClickListener? = null
    interface AttachFileClickListener {
        fun cameraAttachDialog()
        fun galleryAttachDialog()
        fun videoAttachDialog()
        fun musicAttachDialog()
        fun fileAttachDialog()
        fun locationAttachDialog()
        fun contactAttachDialog()
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
        val view = inflater.inflate(R.layout.layout_attach_files_bottom_sheet2, container, false)

        view.findViewById<ImageView>(R.id.btnAttachClose).setOnClickListener {
            dismiss()
        }
        view.findViewById<ImageView>(R.id.btnAttachGallery).setOnClickListener {
            dismiss()
            attachFileClickListener?.galleryAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachCamera).setOnClickListener {
            dismiss()
            attachFileClickListener?.cameraAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachVideo).setOnClickListener {
            dismiss()
            attachFileClickListener?.videoAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachMusic).setOnClickListener {
            dismiss()
            attachFileClickListener?.musicAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachFile).setOnClickListener {
            dismiss()
            attachFileClickListener?.fileAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachLocation).setOnClickListener {
            dismiss()
            attachFileClickListener?.locationAttachDialog()
        }
        view.findViewById<ImageView>(R.id.btnAttachContact).setOnClickListener {
            dismiss()
            attachFileClickListener?.contactAttachDialog()
        }

        return view
    }

    companion object {
        fun newInstance() = AttachFileDialog()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            attachFileClickListener = context as AttachFileClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement AttachFileClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        attachFileClickListener = null
    }
}