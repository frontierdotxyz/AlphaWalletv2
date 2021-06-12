package com.alphawallet.dapp.utils

import org.json.JSONObject

fun String.toJSONObjectOrNull() : JSONObject? {

    return try {
        JSONObject(this)
    } catch (t : Throwable) {
        null
    }
}