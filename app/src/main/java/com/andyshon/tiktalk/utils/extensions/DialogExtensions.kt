package com.andyshon.tiktalk.utils.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.ui.dialogs.profileHeight.HeightItem
import com.andyshon.tiktalk.ui.dialogs.profileHeight.ProfileHeightAdapter

fun showMuteNotificationsDialog(context: Context, muteFor: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_mute_notification, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnOk).setOnClickListener {
        alertDialog.dismiss()
        when (radioGroup.checkedRadioButtonId) {
            R.id.radio_2_hours -> muteFor.invoke("2_hours")
            R.id.radio_8_hours -> muteFor.invoke("8_hours")
            R.id.radio_1_week -> muteFor.invoke("1_week")
            R.id.radio_1_year -> muteFor.invoke("1_year")
        }
    }
}

fun showVisibilityDialog(context: Context, name: String, choose:(msg: String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_hide_chat, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.tvHideChatWith).text = "Hide chat with ".plus(name).plus("?")
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnOk).setOnClickListener {
        alertDialog.dismiss()
        showChooseLockDialog(context) {
            when (it) {
                "Pattern" -> {
                    choose.invoke("Pattern")
                }
                "PIN" -> {
                    choose.invoke("PIN")
                }
                "Fingerprint" -> {
                    choose.invoke("Fingerprint")
                }
            }
        }
    }
}

fun showChooseLockDialog(context: Context, choose: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_choose_lock, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.btnPattern).setOnClickListener { alertDialog.dismiss(); choose.invoke("Pattern") }
    view.findViewById<TextView>(R.id.btnPin).setOnClickListener { alertDialog.dismiss(); choose.invoke("PIN") }
    view.findViewById<TextView>(R.id.btnFingerprint).setOnClickListener { alertDialog.dismiss(); choose.invoke("Fingerprint") }
}

fun showDeleteChatDialog(context: Context, name: String, delete: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_delete_chat, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.tvDeleteChatWith).text = "Delete chat with ".plus(name).plus("?")
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnDelete).setOnClickListener { alertDialog.dismiss(); delete.invoke() }
}

fun showBlockUserDialog(context: Context, name: String, block: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_block_user, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.tvBlockUser).text = "Are you sure you want to block ".plus(name).plus("?")
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnBlock).setOnClickListener { alertDialog.dismiss(); block.invoke() }
}

fun showDeleteContactDialog(context: Context, name: String, delete: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_delete_contact, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.tvDeleteContact).text = "Are you sure you want to delete contact ".plus(name).plus("?")
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnDelete).setOnClickListener { alertDialog.dismiss(); delete.invoke() }
}

fun showDeleteMessageDialog(context: Context, delete: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_delete_message, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnDeleteMessage).setOnClickListener { alertDialog.dismiss(); delete.invoke() }
}

fun showChangeNameDialog(context: Context, name: String, setName: (name:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_change_name, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val etName = view.findViewById<EditText>(R.id.etChangeName)
    etName.showKeyboard3()
    etName.setText(name)
    etName.setSelection(etName.text.length)
    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnOk).setOnClickListener { alertDialog.dismiss(); setName.invoke(etName.text.toString().trim()) }
}

fun showExitStreamStopWatchingDialog(context: Context, exit: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_exit_stream_stop_watching, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnOk).setOnClickListener { alertDialog.dismiss(); exit.invoke() }
}

fun showExitStreamStopStreamingDialog(context: Context, exit: () -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_exit_stream_stop_streaming, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
    view.findViewById<TextView>(R.id.btnOk).setOnClickListener { alertDialog.dismiss(); exit.invoke() }
}

fun showReportUserDialog(context: Context, report: (type:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_report_user, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    view.findViewById<TextView>(R.id.actionInappropriateProfile).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.INAPPROPRIATE_PROFILE)
    }
    view.findViewById<TextView>(R.id.actionInappropriateMessages).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.INAPPROPRIATE_MESSAGES)
    }
    view.findViewById<TextView>(R.id.actionStolenPhoto).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.STOLEN_PHOTO)
    }
    view.findViewById<TextView>(R.id.actionScammer).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.SCAMMER)
    }
    view.findViewById<TextView>(R.id.actionBadOfflineBehavior).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.BAD_OFFLINE_BEHAVIOR)
    }
    view.findViewById<TextView>(R.id.actionOther).setOnClickListener {
        alertDialog.dismiss()
        report.invoke(Constants.ReportTypes.OTHER)
    }

    view.findViewById<TextView>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }
}

fun showRelationshipDialog(context: Context, status: String, relationship: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_relationship, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.relationship_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.relationship_single -> radioGroup.check(R.id.radioSingle)
        context string R.string.relationship_taken -> radioGroup.check(R.id.radioTaken)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> relationship.invoke(context string R.string.relationship_no_answer)
                R.id.radioSingle -> relationship.invoke(context string R.string.relationship_single)
                R.id.radioTaken -> relationship.invoke(context string R.string.relationship_taken)
            }
        }, 500)
    }

    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showSexualityDialog(context: Context, status: String, sexuality: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_sexuality, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.sexuality_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.sexuality_bisexual -> radioGroup.check(R.id.radioBisexual)
        context string R.string.sexuality_gay -> radioGroup.check(R.id.radioGay)
        context string R.string.sexuality_ask_me -> radioGroup.check(R.id.radioAskMe)
        context string R.string.sexuality_straight -> radioGroup.check(R.id.radioStraight)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> sexuality.invoke(context string R.string.sexuality_no_answer)
                R.id.radioBisexual -> sexuality.invoke(context string R.string.sexuality_bisexual)
                R.id.radioGay -> sexuality.invoke(context string R.string.sexuality_gay)
                R.id.radioAskMe -> sexuality.invoke(context string R.string.sexuality_ask_me)
                R.id.radioStraight -> sexuality.invoke(context string R.string.sexuality_straight)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showHeightDialog(context: Context, curHeight: String, height: (h:Int) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_height, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
    var adapter: ProfileHeightAdapter? = null

    val heights = arrayListOf(HeightItem(144),HeightItem(145),HeightItem(146),HeightItem(147),HeightItem(148),HeightItem(149),HeightItem(150),HeightItem(151),
        HeightItem(152),HeightItem(153),HeightItem(154),HeightItem(155),HeightItem(156),HeightItem(157),HeightItem(158),HeightItem(159),HeightItem(160),HeightItem(161),
        HeightItem(162),HeightItem(163),HeightItem(164),HeightItem(165),HeightItem(166),HeightItem(167),HeightItem(168),HeightItem(169),HeightItem(170),HeightItem(171),
        HeightItem(172),HeightItem(173),HeightItem(174),HeightItem(175),HeightItem(176),HeightItem(177),HeightItem(178),HeightItem(179),HeightItem(180),HeightItem(181),
        HeightItem(182),HeightItem(183),HeightItem(184),HeightItem(185),HeightItem(186),HeightItem(187),HeightItem(188),HeightItem(189),HeightItem(190),HeightItem(191),
        HeightItem(192),HeightItem(193),HeightItem(194),HeightItem(195),HeightItem(196),HeightItem(197),HeightItem(198),HeightItem(199),HeightItem(200))

    var selectedPos: Int

    fun setClickListener(): ItemClickListener<HeightItem> {
        return object: ItemClickListener<HeightItem> {
            override fun onItemClick(view: View, pos: Int, item: HeightItem) {
                selectedPos = pos
                adapter?.selectItem(selectedPos)
                adapter?.notifyDataSetChanged()
                view.postDelayed({
                    alertDialog.dismiss()
                    height.invoke(item.value)
                }, 500)
            }
        }
    }
    fun setupList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ProfileHeightAdapter(heights, setClickListener())
        recyclerView.adapter = adapter
        if (curHeight.isNotEmpty()) {
            var pos = -1
            heights.forEach {
                it.checked = it.value == curHeight.split(" ").first().toInt()
                if (it.checked) pos = heights.indexOf(it)
            }
            if (pos != -1) recyclerView.scrollToPosition(pos)
        }
        adapter?.notifyDataSetChanged()
    }

    setupList()
}

fun showLivingDialog(context: Context, status: String, living: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_living, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.living_popup_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.living_popup_by_myself -> radioGroup.check(R.id.radioByMyself)
        context string R.string.living_popup_student_residence -> radioGroup.check(R.id.radioStudent)
        context string R.string.living_popup_with_parents -> radioGroup.check(R.id.radioWithParents)
        context string R.string.living_popup_with_partner -> radioGroup.check(R.id.radioWithPartner)
        context string R.string.living_popup_with_housemate -> radioGroup.check(R.id.radioWithHousemate)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> living.invoke(context string R.string.living_popup_no_answer)
                R.id.radioByMyself -> living.invoke(context string R.string.living_popup_by_myself)
                R.id.radioStudent -> living.invoke(context string R.string.living_popup_student_residence)
                R.id.radioWithParents -> living.invoke(context string R.string.living_popup_with_parents)
                R.id.radioWithPartner -> living.invoke(context string R.string.living_popup_with_partner)
                R.id.radioWithHousemate -> living.invoke(context string R.string.living_popup_with_housemate)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showChildrenDialog(context: Context, status: String, living: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_children, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.children_popup_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.children_popup_grown_up -> radioGroup.check(R.id.radioGrownUp)
        context string R.string.children_popup_already_have -> radioGroup.check(R.id.radioAlreadyHave)
        context string R.string.children_popup_never -> radioGroup.check(R.id.radioNever)
        context string R.string.children_popup_someday -> radioGroup.check(R.id.radioSomeday)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> living.invoke(context string R.string.children_popup_no_answer)
                R.id.radioGrownUp -> living.invoke(context string R.string.children_popup_grown_up)
                R.id.radioAlreadyHave -> living.invoke(context string R.string.children_popup_already_have)
                R.id.radioNever -> living.invoke(context string R.string.children_popup_never)
                R.id.radioSomeday -> living.invoke(context string R.string.children_popup_someday)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showSmokingDialog(context: Context, status: String, living: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_smoking, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.smoking_popup_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.smoking_popup_heavy_smoker -> radioGroup.check(R.id.radioHeavySmoker)
        context string R.string.smoking_popup_hate_smoking -> radioGroup.check(R.id.radioHateSmoking)
        context string R.string.smoking_popup_dont_like -> radioGroup.check(R.id.radioDontLike)
        context string R.string.smoking_popup_social_smoker -> radioGroup.check(R.id.radioSocialSmoker)
        context string R.string.smoking_popup_smoke_occasionally -> radioGroup.check(R.id.radioSmokeOccasionally)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> living.invoke(context string R.string.smoking_popup_no_answer)
                R.id.radioHeavySmoker -> living.invoke(context string R.string.smoking_popup_heavy_smoker)
                R.id.radioHateSmoking -> living.invoke(context string R.string.smoking_popup_hate_smoking)
                R.id.radioDontLike -> living.invoke(context string R.string.smoking_popup_dont_like)
                R.id.radioSocialSmoker -> living.invoke(context string R.string.smoking_popup_social_smoker)
                R.id.radioSmokeOccasionally -> living.invoke(context string R.string.smoking_popup_smoke_occasionally)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showDrinkingDialog(context: Context, status: String, living: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_drinking, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.drinking_popup_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.drinking_popup_drink_socially -> radioGroup.check(R.id.radioDrinkSocially)
        context string R.string.drinking_popup_dont_drink -> radioGroup.check(R.id.radioDontDrink)
        context string R.string.drinking_popup_against_drinking -> radioGroup.check(R.id.radioAgainstDrinking)
        context string R.string.drinking_popup_drink_a_lot -> radioGroup.check(R.id.radioDrinkingALot)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> living.invoke(context string R.string.drinking_popup_no_answer)
                R.id.radioDrinkSocially -> living.invoke(context string R.string.drinking_popup_drink_socially)
                R.id.radioDontDrink -> living.invoke(context string R.string.drinking_popup_dont_drink)
                R.id.radioAgainstDrinking -> living.invoke(context string R.string.drinking_popup_against_drinking)
                R.id.radioDrinkingALot -> living.invoke(context string R.string.drinking_popup_drink_a_lot)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}

fun showZodiacDialog(context: Context, status: String, living: (msg:String) -> Unit) {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.layout_dialog_zodiac, null)
    val alertDialogBuilderUserInput = AlertDialog.Builder(context, R.style.CustomAlertDialog)
    alertDialogBuilderUserInput.setView(view)

    val mWidth = getCustomDialogWidthInPx(0.30, context as Activity)

    val alertDialog = alertDialogBuilderUserInput.create()
    alertDialog.setCanceledOnTouchOutside(true)
    alertDialog.show()
    alertDialog.window.setLayout(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
    alertDialog.window.setGravity(Gravity.CENTER)

    val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
    when(status) {
        context string R.string.zodiac_no_answer -> radioGroup.check(R.id.radioNoAnswer)
        context string R.string.zodiac_aries -> radioGroup.check(R.id.radioAries)
        context string R.string.zodiac_taurus -> radioGroup.check(R.id.radioTaurus)
        context string R.string.zodiac_gemini -> radioGroup.check(R.id.radioGemini)
        context string R.string.zodiac_cancer -> radioGroup.check(R.id.radioCancer)
        context string R.string.zodiac_leo -> radioGroup.check(R.id.radioLeo)
        context string R.string.zodiac_virgo -> radioGroup.check(R.id.radioVirgo)
        context string R.string.zodiac_libra -> radioGroup.check(R.id.radioLibra)
        context string R.string.zodiac_scorpio -> radioGroup.check(R.id.radioScorpio)
        context string R.string.zodiac_sagittarius -> radioGroup.check(R.id.radioSagittarius)
        context string R.string.zodiac_capricorn -> radioGroup.check(R.id.radioCapricorn)
        context string R.string.zodiac_aquarius -> radioGroup.check(R.id.radioAquarius)
        context string R.string.zodiac_pisces -> radioGroup.check(R.id.radioPisces)
        else -> radioGroup.check(R.id.radioNoAnswer)
    }

    fun choose() {
        radioGroup.postDelayed({
            alertDialog.dismiss()
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioNoAnswer -> living.invoke(context string R.string.zodiac_no_answer)
                R.id.radioAries -> living.invoke(context string R.string.zodiac_aries)
                R.id.radioTaurus -> living.invoke(context string R.string.zodiac_taurus)
                R.id.radioGemini -> living.invoke(context string R.string.zodiac_gemini)
                R.id.radioCancer -> living.invoke(context string R.string.zodiac_cancer)
                R.id.radioLeo -> living.invoke(context string R.string.zodiac_leo)
                R.id.radioVirgo -> living.invoke(context string R.string.zodiac_virgo)
                R.id.radioLibra -> living.invoke(context string R.string.zodiac_libra)
                R.id.radioScorpio -> living.invoke(context string R.string.zodiac_scorpio)
                R.id.radioSagittarius -> living.invoke(context string R.string.zodiac_sagittarius)
                R.id.radioCapricorn -> living.invoke(context string R.string.zodiac_capricorn)
                R.id.radioAquarius -> living.invoke(context string R.string.zodiac_aquarius)
                R.id.radioPisces -> living.invoke(context string R.string.zodiac_pisces)
            }
        }, 500)
    }
    radioGroup.setOnCheckedChangeListener { _, _ -> choose() }
}