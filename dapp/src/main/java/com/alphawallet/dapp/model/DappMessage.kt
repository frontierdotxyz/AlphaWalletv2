package com.alphawallet.dapp.model

data class DappMessage(val callbackId: Int,
                  val message: String,
                  val isPersonalSign: Boolean)