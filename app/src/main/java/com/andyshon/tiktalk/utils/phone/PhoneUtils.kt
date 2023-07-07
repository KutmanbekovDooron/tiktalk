package com.andyshon.tiktalk.utils.phone

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.google.i18n.phonenumbers.PhoneNumberUtil
import timber.log.Timber
import java.nio.ByteBuffer

fun contactExists(context: Context, number: String): Boolean {
    /// number is the phone number
    val lookupUri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(number)
    )
    val mPhoneNumberProjection = arrayOf(
        ContactsContract.PhoneLookup._ID,
        ContactsContract.PhoneLookup.NUMBER,
        ContactsContract.PhoneLookup.DISPLAY_NAME
    )
    val cur = context.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
    try {
        if (cur!!.moveToFirst()) {
            Timber.e("contactExists = true")
            return true
        }
    } finally {
        if (cur != null)
            cur.close()
    }
    Timber.e("contactExists = false")
    return false
}

fun getRegionNameForEmojiesList(region: String): String {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return "${getEmojiFlagForRegion(region)} +${phoneNumberUtil.getCountryCodeForRegion(region)}"
}

fun getCountryNameByRegion(region: String): String {
    return PhoneNumberFormatter.getRegionDisplayName(region, "en")
}

fun initRegionNameForEmojiesList() {
//    if (CountriesMetadata.countriesWithEmojies.isEmpty()) {
    val list = arrayListOf<CountryEmojie>()

    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val supportedRegions = phoneNumberUtil.supportedRegions

    for (region in supportedRegions) {
        val isoCode = getEmojiFlagForRegion(region)
        val code = phoneNumberUtil.getCountryCodeForRegion(region)
        val country = PhoneNumberFormatter.getRegionDisplayName(region, "en")

        val countryEmojie = CountryEmojie(isoCode, code, country, region)
        list.add(countryEmojie)
    }
    list.sortBy { it.country }
    CountriesMetadata.countriesWithEmojies.clear()
    CountriesMetadata.countriesWithEmojies.addAll(list)
//    }
}

fun getPhoneNumbersListWithEmojies(): List<String> {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val supportedRegions = phoneNumberUtil.supportedRegions

    val regionCodeMap = mutableMapOf<String, String>()
    for (region in supportedRegions) {
        val countryName = PhoneNumberFormatter.getRegionDisplayName(region, "en")
        regionCodeMap[region] = phoneNumberUtil.getCountryCodeForRegion(region).toString().plus(" ").plus(countryName)
    }

    regionCodeMap.toList().forEach {
        Timber.e("VALUE = ${it.first}, ${it.second}")
    }

    val sortedMap = regionCodeMap.toList().sortedBy { (_, value) ->
            val f = value.split(",").last()
            var f2 = f.split(" ")   // [1, Puerto, Rico]
            Timber.e("f2 old = $f2")
            f2 = f2.subList(1, f2.size)
            Timber.e("f = $f, f2 = $f2")

            if (f2.size == 2) {
                f2.first()
            }
            else {
                val full = f2.toString()
                Timber.e("FULL = $full")
                val sb = java.lang.StringBuilder()
                f2.forEach {
                    sb.append(it.plus(" "))
                }
                sb.toString().trim()
                Timber.e("SB = $sb")
                sb.toString()
            }

        }.toMap()

    val result = mutableListOf<String>()
    for (item in sortedMap) {
        val key = item.value
        result.add("${getEmojiFlagForRegion(item.key)} +$key")
        Timber.e("result code = ${getEmojiFlagForRegion(item.key)} +$key")
    }
    return result
}

fun getEmojiFlagForRegion(region: String): String {
    val result = StringBuilder()
    val buffer = ByteBuffer.allocate(4)
    for (ch in region) {
        val letterNum = ch.toInt().minus('A'.toInt())
        val newChar = -257980506 + letterNum //🇦
        buffer.clear()
        buffer.putInt(0, newChar)
        result.append(Charsets.UTF_8.decode(buffer))
    }
    return result.toString()
}

fun getMobilePhoneMaxLength(countryCode: Int): Int {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val regionCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode)
//    val f = phoneNumberUtil.getExampleNumberForType(regionCode, PhoneNumberUtil.PhoneNumberType.MOBILE).nationalNumber.toString().length
//    if (f != null) {
        return phoneNumberUtil.getExampleNumberForType(regionCode, PhoneNumberUtil.PhoneNumberType.MOBILE)?.nationalNumber?.toString()?.length?:9
//    }
}