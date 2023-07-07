package com.andyshon.tiktalk.ui.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragmentDialog: DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var listener: DatePickerListener
    private var datePickerDialog: DatePickerDialog? = null
    private var dateToRestore: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = activity as DatePickerListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement DatePickerListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(activity!!, AlertDialog.THEME_HOLO_LIGHT,this, year, month, day)
        val maxYears = Calendar.getInstance()
        maxYears.add(Calendar.YEAR, -16)
        datePickerDialog?.datePicker?.maxDate = maxYears.timeInMillis//Date().time
        restoreDate()
        return datePickerDialog!!
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        listener.onDatePicked(year, month, day)
    }

    private fun restoreDate() {
        if (datePickerDialog != null && !dateToRestore.isNullOrBlank()) {
            val date = SimpleDateFormat("dd-MM-yyyy").parse(dateToRestore)


            val calendar = dateToCalendar(date)
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            datePickerDialog?.datePicker?.updateDate( year, month, day)
        }
    }

    fun setDateFromString(inDate: String) {
        dateToRestore = inDate
    }

    //Convert Date to Calendar
    private fun dateToCalendar(date: Date): Calendar {

        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar

    }

    //Convert Calendar to Date
    private fun calendarToDate(calendar: Calendar): Date {
        return calendar.time
    }

    interface DatePickerListener {
        fun onDatePicked(year: Int, month: Int, day: Int)
    }

}