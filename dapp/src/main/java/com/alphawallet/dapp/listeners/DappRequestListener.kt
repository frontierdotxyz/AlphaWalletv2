package com.alphawallet.dapp.listeners

import com.alphawallet.dapp.model.DappMessage
import com.alphawallet.dapp.model.DappTransaction

interface DappRequestListener {

    fun onTransactionSignRequest(transaction: DappTransaction)
    fun onMessageSignRequest(message: DappMessage)

    fun onSignTypedMessageRequest(callbackId: Int, payload: String) {

    }
    fun onEthCall(callbackId: Int, recipient: String, payload: String) {

    }
}