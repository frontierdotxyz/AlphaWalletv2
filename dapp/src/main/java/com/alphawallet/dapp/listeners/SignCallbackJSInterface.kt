package com.alphawallet.dapp.listeners

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.alphawallet.dapp.model.DappMessage
import com.alphawallet.dapp.model.DappTransaction

internal class SignCallbackJSInterface(
        private val webView: WebView,
        private val dappRequestListener: DappRequestListener) {

    @JavascriptInterface
    fun signTransaction(
            callbackId: Int,
            recipient: String?,
            txValue: String,
            nonce: String?,
            gasLimit: String?,
            txGasPrice: String?,
            payload: String?) {

        var value = txValue
        var gasPrice = txGasPrice
        if (value == "undefined") value = "0"
        if (gasPrice == null) gasPrice = "0"
        val transaction = DappTransaction(callbackId, recipient.orEmpty(), value, nonce, gasPrice, gasLimit, payload ?: "0x")
        webView.post { dappRequestListener.onTransactionSignRequest(transaction) }
    }

    @JavascriptInterface
    fun signMessage(callbackId: Int, data: String) {
        webView.post { dappRequestListener.onMessageSignRequest(DappMessage(callbackId, data, false)) }
    }

    @JavascriptInterface
    fun signPersonalMessage(callbackId: Int, data: String) {
        webView.post { dappRequestListener.onMessageSignRequest(DappMessage(callbackId, data, true)) }
    }

    @JavascriptInterface
    fun signTypedMessage(callbackId: Int, data: String) {
        webView.post { dappRequestListener.onSignTypedMessageRequest(callbackId, data) }
    }

    @JavascriptInterface
    fun ethCall(callbackId: Int, recipient: String, payload: String) {
        webView.post { dappRequestListener.onEthCall(callbackId, recipient, payload) }
    }
}