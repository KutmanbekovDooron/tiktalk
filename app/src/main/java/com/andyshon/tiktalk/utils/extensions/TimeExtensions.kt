package com.andyshon.tiktalk.utils.extensions

import android.os.SystemClock
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

const val TimeFormatConstant = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
fun getLocalCalendar(createdAt: String) : Calendar{
    val date = Calendar.getInstance()
    val dt = SimpleDateFormat(TimeFormatConstant).parse(createdAt)
    date.time = Date(dt.time - dt.getTimezoneOffset() * 60 * 1000)
    return date
}

fun calcDurationTime(mCurrentPosition: Int, messageDuration: String): String {
    Timber.e("mCurrentPosition = $mCurrentPosition")

    val time = if (mCurrentPosition < 60) {
        if (mCurrentPosition < 10) "0:0".plus(mCurrentPosition)
        else "0:".plus(mCurrentPosition)
    }
    else {
        val minutes = mCurrentPosition / 60
        val seconds = if (mCurrentPosition % 60 < 10) "0".plus(mCurrentPosition % 60) else mCurrentPosition % 60
        minutes.toString().plus(":").plus(seconds)
    }
    return time.plus(" / ").plus(messageDuration)
}

fun calculateTime(updatedAt: String): String {
    //  2019-06-20T12:05:11.305Z
    val thatDay = Calendar.getInstance()
    val dayOfMonth = updatedAt.split("-")[2].substring(0, 2).toInt()
    val month = updatedAt.split("-")[1].toInt()
    val year = updatedAt.split("-").first().toInt()

    val part1 = updatedAt.split(":").first()
    val part2 = updatedAt.split(":")[1]
    val part3 = updatedAt.split(":").last()

    val hours = part1.substring(part1.length-2, part1.length).toInt()
    val minutes = part2.toInt()
    val seconds = part3.substring(0, 2).toInt()

    thatDay.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    thatDay.set(Calendar.MONTH, month - 1) // 0-11 so 1 less
    thatDay.set(Calendar.YEAR, year)
    thatDay.set(Calendar.HOUR, hours)
    thatDay.set(Calendar.MINUTE, minutes)
    thatDay.set(Calendar.SECOND, seconds)

    val today = Calendar.getInstance()


    var diff = today.timeInMillis - thatDay.timeInMillis //result in millis
    val days = diff / (24 * 60 * 60 * 1000)

    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    val elapsedDays = diff / daysInMilli
    diff %= daysInMilli

    val elapsedHours = diff / hoursInMilli
    diff %= hoursInMilli

    val elapsedMinutes = diff / minutesInMilli
    diff %= minutesInMilli

    val elapsedSeconds = diff / secondsInMilli

    var totalHours = elapsedHours
    if (elapsedDays > 0) {
        totalHours += (elapsedDays * 24)
    }

    return when {
        totalHours < 24 -> {
            val mins = if (minutes>9) minutes.toString() else "0".plus(minutes)
            hours.toString().plus(":").plus(mins)
        }
        totalHours in 25..47 -> "Yesterday"
        else -> getNameOfMonth(month).plus(" ").plus(dayOfMonth)
    }
}

fun getNameOfMonth(month: Int): String {
    return when(month) {
        0 -> "Jan"
        1-> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        else -> "Dec"
    }
}

fun getDateMessageFromDate(dateCreatedAsDate: String): String {
    val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US)
    try {
        val date = formatter.parse(dateCreatedAsDate)   //"Thu Dec 17 15:37:43 GMT+05:30 2015"
        return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return "Jul 12, 2019"
}

fun getTimeForMessageDate(dateCreatedAsDate: String): String {
    val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US)
    try {
        val date = formatter.parse(dateCreatedAsDate)   //"Thu Dec 17 15:37:43 GMT+05:30 2015"
        return SimpleDateFormat("HH:mm", Locale.US).format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return "10:00"
}

fun getTimeForMessageDateClip(dateCreatedAsDate: String): String {
    Timber.e("getTimeForMessageDate, dateCreatedAsDate = $dateCreatedAsDate")
    val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US)
    try {
        val date = formatter.parse(dateCreatedAsDate)   //"Thu Dec 17 15:37:43 GMT+05:30 2015"
        val formatted = SimpleDateFormat("MM.dd, HH:mm", Locale.US).format(date)
        return "[".plus(formatted).plus("]")
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return "[12.07, 16:45]"
}

fun getAge(dateOfBirth: String): String {
    if (dateOfBirth.isNotEmpty()) {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.set(
            dateOfBirth.split("-")[2].substring(0, 4).toInt(),
            dateOfBirth.split("-")[1].toInt()-1,
            dateOfBirth.split("-").first().toInt()
        )

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        val ageInt = age
        return ageInt.toString()
    }
    else {
        return ""
    }
}

fun moreThan24Hours(date1: Date, date2: Date): Boolean {
    Timber.e("date1 = $date1, date2 = $date2")
    //date 1 = Fri Aug 09 14:14:19 GMT+03:00 2019, date2 = Fri Aug 09 14:14:00 GMT+03:00 2019

    val diff = date1.time - date2.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    Timber.e("diff = $diff")
    Timber.e("seconds = $seconds")
    Timber.e("minutes = $minutes")
    Timber.e("hours = $hours")
    Timber.e("days = $days")

    return hours >=24 && minutes > 10
}

fun getVoiceCallDurationInFormat(base: Long): String {
    val elapsedMillis = SystemClock.elapsedRealtime() - base

    val seconds = (elapsedMillis / 1000).toInt() % 60
    val minutes = (elapsedMillis / (1000 * 60) % 60).toInt()
    val hours = (elapsedMillis / (1000 * 60 * 60) % 24).toInt()

    return if (hours != 0) {
        val h = if (hours == 1) "hour" else "hours"
        val m = if (minutes == 1) "minute" else "minutes"
        val s = if (seconds == 1) "second" else "seconds"
        "Lasted $hours $h $minutes $m $seconds $s"
    }
    else if (hours == 0 && minutes != 0) {
        val m = if (minutes == 1) "minute" else "minutes"
        val s = if (seconds == 1) "second" else "seconds"
        "Lasted $minutes $m $seconds $s"
    }
    else if (hours == 0 && minutes == 0) {
        val s = if (seconds == 1) "second" else "seconds"
        "Lasted $seconds $s"
    }
    else ""
}