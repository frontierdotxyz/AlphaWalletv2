package com.alphawallet.dapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.alphawallet.dapp.R


//Handling of trusted apps
fun Context.handleTrustedApps(url: String): Boolean {
    //get list
    val strArray = resources.getStringArray(R.array.TrustedApps)
    for (item in strArray) {
        val split = item.split(",".toRegex()).toTypedArray()
        if (url.startsWith(split[1])) {
            intentTryApp(split[0], url)
            return true
        }
    }
    return false
}

fun Context.handleTrustedExtension(url: String): Boolean {
    val strArray = resources.getStringArray(R.array.TrustedExtensions)
    for (item in strArray) {
        val split = item.split(",".toRegex()).toTypedArray()
        if (url.endsWith(split[1])) {
            useKnownOpenIntent(split[0], url)
            return true
        }
    }
    return false
}

private fun Context.intentTryApp(appId: String, msg: String) {
    val isAppInstalled = isAppAvailable(appId)
    if (isAppInstalled) {
        val myIntent = Intent(Intent.ACTION_VIEW)
        myIntent.setPackage(appId)
        myIntent.data = Uri.parse(msg)
        myIntent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(myIntent)
    } else {
        Toast.makeText(this, "Required App not Installed", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.useKnownOpenIntent(type: String, url: String) {
    val openIntent = Intent(Intent.ACTION_VIEW)
    openIntent.setDataAndType(Uri.parse(url), type)
    val intent = Intent.createChooser(openIntent, "Open using")
    if (isIntentAvailable(intent)) {
        startActivity(intent)
    } else {
        Toast.makeText(this, "Required App not Installed", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.isIntentAvailable(intent: Intent): Boolean {
    val packageManager = packageManager
    val list: List<*> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return list.isNotEmpty()
}

private fun Context.isAppAvailable(appName: String): Boolean {
    return try {
        packageManager.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}