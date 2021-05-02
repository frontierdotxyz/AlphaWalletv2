package com.alphawallet.dapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream


fun Context.readRawResource(resId: Int): String {

    var buffer = ByteArray(0)
    try {
        val stream = resources.openRawResource(resId)
        buffer = ByteArray(stream.available())
        val len = stream.read(buffer)
        if (len < 1) {
            throw IOException("Nothing is read.")
        }
        stream.close()
    } catch (ex: Exception) {
        Log.d("READ_JS_TAG", "Ex", ex)
    }
    return String(buffer)
}

fun Context.getUserAgent() : String {

    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    val appName = applicationInfo.loadLabel(packageManager).toString()

    return appName+"(Platform=Android&AppVersion=" + packageInfo.versionName + ")"
}

@Throws(IOException::class)
fun WebResourceResponse.readStream() : String {
    val len = data.available()
    val outBytes = ByteArray(len)
    val readLen = data.read(outBytes)
    if (readLen == 0) {
        throw IOException("Nothing is read.")
    }

    return String(outBytes)
}

