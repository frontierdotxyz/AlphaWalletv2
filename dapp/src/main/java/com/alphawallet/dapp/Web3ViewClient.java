package com.alphawallet.dapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import android.text.TextUtils;
import android.util.Base64;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import com.alphawallet.dapp.utils.IntentExtensionsKt;

import okhttp3.HttpUrl;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.N;

public class Web3ViewClient extends WebViewClient {

    private final Object lock = new Object();

    private final JsInjectorClient jsInjectorClient;

    private boolean isInjected;

    public Web3ViewClient(JsInjectorClient jsInjectorClient) {
        this.jsInjectorClient = jsInjectorClient;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoading(view, url, false, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request == null || view == null) {
            return false;
        }
        String url = request.getUrl().toString();
        boolean isMainFrame = request.isForMainFrame();
        boolean isRedirect = SDK_INT >= N && request.isRedirect();
        return shouldOverrideUrlLoading(view, url, isMainFrame, isRedirect);
    }

    private boolean shouldOverrideUrlLoading(WebView webView, String url, boolean isMainFrame, boolean isRedirect) {
        boolean result = false;
        synchronized (lock) {
            isInjected = false;
        }
        String urlToOpen = null;
        //manually handle trusted intents
        if (IntentExtensionsKt.handleTrustedApps(webView.getContext(), url)) {
            return true;
        }

        if (!url.startsWith("http")) {
            result = true;
        }
        if (isMainFrame && isRedirect) {
            urlToOpen = url;
            result = true;
        }

        if (result && !TextUtils.isEmpty(urlToOpen)) {
            webView.loadUrl(urlToOpen);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (request == null) {
            return null;
        }
        if (!request.getMethod().equalsIgnoreCase("GET") || !request.isForMainFrame()) {
            if (request.getMethod().equalsIgnoreCase("GET")
                    && (request.getUrl().toString().contains(".js")
                    || request.getUrl().toString().contains("json")
                    || request.getUrl().toString().contains("css"))) {
                synchronized (lock) {
                    if (!isInjected) {
                        injectScriptFile(view);
                        isInjected = true;
                    }
                }
            }
            super.shouldInterceptRequest(view, request);
            return null;
        }

        //check for known extensions
        if (IntentExtensionsKt.handleTrustedExtension(view.getContext(), request.getUrl().toString())) {
            return null;
        }

        HttpUrl httpUrl = HttpUrl.parse(request.getUrl().toString());
        if (httpUrl == null) {
            return null;
        }
        Map<String, String> headers = request.getRequestHeaders();

        WebResourceResponse response = jsInjectorClient.loadUrl(view.getContext(), httpUrl.toString(), headers);

        if (response == null) {
            return null;
        }
        synchronized (lock) {
            isInjected = true;
        }

        return response;
    }

    private void injectScriptFile(WebView view) {
        String js = jsInjectorClient.assembleJs(view.getContext(), "%1$s%2$s");
        byte[] buffer = js.getBytes();
        String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);

        view.post(() -> view.loadUrl("javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var script = document.createElement('script');" +
                "script.type = 'text/javascript';" +
                // Tell the browser to BASE64-decode the string into your script !!!
                "script.innerHTML = window.atob('" + encoded + "');" +
                "parent.appendChild(script)" +
                "})()"));
    }
}