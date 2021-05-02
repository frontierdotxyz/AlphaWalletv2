package com.alphawallet.dapp.listeners

interface UrlLoadListener {
    fun onWebPageLoaded(url: String, title: String)
    fun onWebPageLoadComplete()
}