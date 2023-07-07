package com.andyshon.tiktalk.utils

object ExceptionUtils {

    /**
     */
    fun <T> checkNull(item: Any?, clazz: Class<T>) {
        if (item == null) {
            throw IllegalArgumentException(clazz.name + "is null!")
        }
    }

}