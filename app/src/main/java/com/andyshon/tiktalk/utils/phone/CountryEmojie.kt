package com.andyshon.tiktalk.utils.phone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CountryEmojie(
    var isoCode: String,
    var code: Int,
    var country: String,
    var region: String,
    var isChecked: Boolean = false
): Parcelable