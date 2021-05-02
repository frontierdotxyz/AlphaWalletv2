package com.alphawallet.dapp

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class WebViewCookieJar : CookieJar {
    private val webViewCookieManager: CookieManager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        for (cookie in cookies) {
            webViewCookieManager.setCookie(urlString, cookie.toString())
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()
        val cookiesString = webViewCookieManager.getCookie(urlString)

        if(cookiesString.isNullOrEmpty()) return emptyList()

        val cookieHeaders = cookiesString.split(";".toRegex())
        return cookieHeaders.mapNotNull { Cookie.parse(url, it) }
    }

}