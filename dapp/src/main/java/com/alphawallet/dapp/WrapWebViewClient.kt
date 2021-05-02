package com.alphawallet.dapp

import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import com.alphawallet.dapp.listeners.UrlLoadListener

internal class WrapWebViewClient(
        private val internalClient: Web3ViewClient,
        var externalClient: WebViewClient) : WebViewClient() {

    constructor(internalClient: Web3ViewClient) : this(internalClient, WebViewClient())

    private var loadListener: UrlLoadListener? = null
    private var loadingError = false
    private var redirect = false

    fun setUrlLoadListener(loadListener: UrlLoadListener?) {
        this.loadListener = loadListener
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!redirect && !loadingError) {
            loadListener?.onWebPageLoaded(url, view.title.orEmpty())
        } else if (!loadingError) {
            loadListener?.onWebPageLoadComplete()
        }
        redirect = false
        loadingError = false
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        redirect = true
        return (externalClient.shouldOverrideUrlLoading(view, url)
                || internalClient.shouldOverrideUrlLoading(view, url))
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        loadingError = true
        externalClient.onReceivedError(view, request, error)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        redirect = true
        return (externalClient.shouldOverrideUrlLoading(view, request)
                || internalClient.shouldOverrideUrlLoading(view, request))
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return internalClient.shouldInterceptRequest(view, request)
    }
}