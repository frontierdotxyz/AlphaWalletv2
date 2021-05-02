package com.alphawallet.dapp;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alphawallet.dapp.listeners.DappRequestListener;
import com.alphawallet.dapp.listeners.SignCallbackJSInterface;
import com.alphawallet.dapp.listeners.UrlLoadListener;
import com.alphawallet.dapp.model.DappMessage;
import com.alphawallet.dapp.model.DappTransaction;
import com.alphawallet.dapp.utils.ResourceExtensionsKt;

import org.jetbrains.annotations.NotNull;

public class Web3View extends WebView {
    private static final String JS_PROTOCOL_CANCELLED = "cancelled";
    private static final String JS_PROTOCOL_ON_SUCCESSFUL = "executeCallback(%1$s, null, \"%2$s\")";
    private static final String JS_PROTOCOL_ON_FAILURE = "executeCallback(%1$s, \"%2$s\", null)";

    private final JsInjectorClient jsInjectorClient = new JsInjectorClient();
    private final WrapWebViewClient wrapClient = new WrapWebViewClient(new Web3ViewClient(jsInjectorClient));

    private DappRequestListener outerListener = null;

    public Web3View(@NonNull Context context) {
        super(context);
    }

    public Web3View(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Web3View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setWebViewClient(WebViewClient actualClient) {
        wrapClient.setExternalClient(actualClient);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString()
                + ResourceExtensionsKt.getUserAgent(getContext()));
        WebView.setWebContentsDebuggingEnabled(true); //so devs can debug their scripts/pages

        super.setWebViewClient(wrapClient);

        addJavascriptInterface(new SignCallbackJSInterface(
                this,
                innerListener), "alpha");
    }

    public void setWalletAddress(@NonNull String address) {
        jsInjectorClient.setWalletAddress(address);
    }

    public void setChainId(int chainId) {
        jsInjectorClient.setChainId(chainId);
    }

    public void setRpcUrl(@NonNull String rpcUrl) {
        jsInjectorClient.setRpcUrl(rpcUrl);
    }

    public void setWebLoadCallback(UrlLoadListener listener) {
        wrapClient.setUrlLoadListener(listener);
    }

    public void setDappListener(@NonNull DappRequestListener listener) {
        this.outerListener = listener;
    }

    public void onSignTransactionSuccessful(long callbackId, String signHex) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_SUCCESSFUL, signHex);
    }

    public void onSignMessageSuccessful(long callbackId, String signHex) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_SUCCESSFUL, signHex);
    }

    public void onCallFunctionSuccessful(long callbackId, String result) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_SUCCESSFUL, result);
    }

    public void onCallFunctionError(long callbackId, String error) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_FAILURE, error);
    }

    public void onSignError(long callbackId, String error) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_FAILURE, error);
    }

    public void onSignCancel(long callbackId) {
        callbackToJS(callbackId, JS_PROTOCOL_ON_FAILURE, JS_PROTOCOL_CANCELLED);
    }

    private void callbackToJS(long callbackId, String function, String param) {
        String callback = String.format(function, callbackId, param);
        post(() -> evaluateJavascript(callback, value -> Log.d("WEB_VIEW", value)));
    }

    private final DappRequestListener innerListener = new DappRequestListener() {
        @Override
        public void onEthCall(int callbackId, @NotNull String recipient, @NotNull String payload) {
            if(outerListener == null) return;
            outerListener.onEthCall(callbackId, recipient, payload);
        }

        @Override
        public void onSignTypedMessageRequest(int callbackId, @NotNull String payload) {
            if(outerListener == null) return;
            outerListener.onSignTypedMessageRequest(callbackId, payload);
        }

        @Override
        public void onTransactionSignRequest(@NotNull DappTransaction transaction) {
            if(outerListener == null) return;
            outerListener.onTransactionSignRequest(transaction);
        }

        @Override
        public void onMessageSignRequest(@NotNull DappMessage message) {
            if(outerListener == null) return;
            outerListener.onMessageSignRequest(message);
        }
    };
}
