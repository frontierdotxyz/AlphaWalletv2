package com.alphawallet.dapp.model

data class DappTransaction(val callbackId: Int,
                      val toAddress: String,
                      val value: String,
                      val nonce: String?,
                      val gasPrice: String?,
                      val gasLimit: String?,
                      val payload: String)