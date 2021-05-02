package com.alphawallet.dapp.utils

import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern

const val DEFAULT_CHARSET = "utf-8"
const val DEFAULT_MIME_TYPE = "text/html"


fun Response.getContentType(): String {

    val contentType1 = headers["Content-Type"]
    val contentType2 = headers["content-Type"]
    val defaultContentType = "text/data; charset=utf-8"

    val finalContentType = contentType1 ?: contentType2 ?: defaultContentType

    return finalContentType.trim()
}

fun Response.toStringBody() : String? {

    return try {
        if (isSuccessful) {
            body?.string()
        }
        else {
            null
        }
    } catch (ex: IOException) {
        Log.d("READ_BODY_ERROR", "Ex", ex)
        null
    }
}

fun String.getCharset(): String? {
    val regexResult = Pattern.compile("charset=([a-zA-Z0-9-]+)").matcher(this)
    if (regexResult.find()) {
        if (regexResult.groupCount() >= 2) {
            return regexResult.group(1)
        }
    }
    return DEFAULT_CHARSET
}

fun String.getMimeType(): String? {
    val regexResult = Pattern.compile("^.*(?=;)").matcher(this)
    return if (regexResult.find()) {
        regexResult.group()
    } else DEFAULT_MIME_TYPE
}

fun String.urlToRequest(headers: Map<String, String>): Request? {
    val httpUrl = this.toHttpUrlOrNull() ?: return null
    val requestBuilder = Request.Builder()
            .get()
            .url(httpUrl)
    val keys = headers.keys
    for (key in keys) {
        val value = headers[key]
        if (value != null) {
            requestBuilder.addHeader(key, value)
        }
    }
    return requestBuilder.build()
}