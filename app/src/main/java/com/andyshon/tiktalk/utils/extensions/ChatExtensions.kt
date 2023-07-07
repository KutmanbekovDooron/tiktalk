package com.andyshon.tiktalk.utils.extensions

import org.json.JSONObject

fun generateUserColor(atr: JSONObject): String {
    val colorsValues = arrayListOf("479C73", "5E6996", "CE5612", "BF3EFF", "68228B", "414AC5", "CE3F3F", "5AC18E", "008080", "FFD700", "FFA500",
        "FFC3A0", "A0DB8E", "14F291", "482628", "794044", "AECBDD", "7D96B2", "202A38", "B3DD9F", "799BC1", "A7BEF1", "F1DAA7", "02A229", "0463C3", "F6AF0E")

    if (atr.has("usersColors").not()) {
        return colorsValues[0]
    }
    val colors = atr.getJSONArray("usersColors")

    return if (colors.length() < colorsValues.size) {
        colorsValues[colors.length()+1]
    } else {
        colorsValues[(0..25).random()]
    }
}