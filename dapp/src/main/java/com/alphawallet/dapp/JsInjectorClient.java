package com.alphawallet.dapp;

import android.content.Context;

import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Map;

import com.alphawallet.dapp.utils.OkHttpExtensionsKt;
import com.alphawallet.dapp.utils.ResourceExtensionsKt;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsInjectorClient {

    private final static String JS_TAG_TEMPLATE = "<script type=\"text/javascript\">%1$s%2$s</script>";
    private final static String EMPTY_ADDRESS = "0000000000000000000000000000000000000000";

    private final OkHttpClient httpClient;

    private String jsLibrary;

    private int chainId = 1;
    private String walletAddress;
    //Note: this default RPC is overriden before injection
    private String rpcUrl;

    public JsInjectorClient() {
        this.httpClient = createHttpClient();
    }

    public void setWalletAddress(String address) {
        this.walletAddress = address;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    @Nullable
    WebResourceResponse loadUrl(Context context, final String url, final Map<String, String> headers) {
        Request request = OkHttpExtensionsKt.urlToRequest(url, headers);
        WebResourceResponse result = null;
        try {
            Response response = httpClient.newCall(request).execute();
            result = buildResponse(context, response);
        } catch (Exception ex) {
            Log.d("REQUEST_ERROR", "", ex);
        }
        return result;
    }

    String assembleJs(Context context, String template) {
        if (TextUtils.isEmpty(jsLibrary)) {
            jsLibrary = ResourceExtensionsKt.readRawResource(context, R.raw.alphawallet_min);
        }
        String initJs = loadInitJs(context);
        return String.format(template, jsLibrary, initJs);
    }

    private WebResourceResponse buildResponse(Context context, Response response) {
        String body = OkHttpExtensionsKt.toStringBody(response);

        Response prior = response.priorResponse();
        boolean isRedirect = prior != null && prior.isRedirect();

        if (isRedirect || body == null) return null;

        String data = injectJS(context, body);
        String contentType = OkHttpExtensionsKt.getContentType(response);
        String charset = OkHttpExtensionsKt.getCharset(contentType);
        String mime = OkHttpExtensionsKt.getMimeType(contentType);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        return new WebResourceResponse(mime, charset, inputStream);
    }

    String injectJS(Context context, String html) {
        String js = assembleJs(context, JS_TAG_TEMPLATE);
        return injectJS(html, js);
    }

    public String injectWeb3TokenInit(Context ctx, String view, String tokenContent, BigInteger tokenId) {
        String initSrc = ResourceExtensionsKt.readRawResource(ctx, R.raw.init_token);
        //put the view in here
        String tokenIdWrapperName = "token-card-" + tokenId.toString(10);
        initSrc = String.format(initSrc, tokenContent, walletAddress, rpcUrl, chainId, tokenIdWrapperName);
        //now insert this source into the view
        // note that the <div> is not closed because it is closed in njectStyleAndWrap().
        String wrapper = "<div id=\"token-card-" + tokenId.toString(10) + "\" class=\"token-card\">";
        initSrc = "<script>\n" + initSrc + "</script>\n" + wrapper;
        return injectJS(view, initSrc);
    }

    public String injectJSAtEnd(String view, String newCode) {
        int position = getEndInjectionPosition(view);
        if (position >= 0) {
            String beforeTag = view.substring(0, position);
            String afterTab = view.substring(position);
            return beforeTag + newCode + afterTab;
        }
        return view;
    }

    public String injectJS(String html, String js) {
        if (TextUtils.isEmpty(html)) {
            return html;
        }
        int position = getInjectionPosition(html);
        if (position >= 0) {
            String beforeTag = html.substring(0, position);
            String afterTab = html.substring(position);
            return beforeTag + js + afterTab;
        }
        return html;
    }

    private int getInjectionPosition(String body) {
        body = body.toLowerCase();
        int ieDetectTagIndex = body.indexOf("<!--[if");
        int scriptTagIndex = body.indexOf("<script");

        int index;
        if (ieDetectTagIndex < 0) {
            index = scriptTagIndex;
        } else {
            index = Math.min(scriptTagIndex, ieDetectTagIndex);
        }
        if (index < 0) {
            index = body.indexOf("</head");
        }
        if (index < 0) {
            index = 0; //just wrap whole view
        }
        return index;
    }

    private int getEndInjectionPosition(String body) {
        body = body.toLowerCase();
        int firstIndex = body.indexOf("<script");
        int nextIndex = body.indexOf("web3", firstIndex);
        return body.indexOf("</script", nextIndex);
    }

    private String loadInitJs(Context context) {
        String initSrc = ResourceExtensionsKt.readRawResource(context, R.raw.init);
        String address = walletAddress == null ? EMPTY_ADDRESS : walletAddress;
        return String.format(initSrc, address, rpcUrl, chainId);
    }

    public String injectStyleAndWrap(String view, String style) {
        if (style == null) style = "";
        //String injectHeader = "<head><meta name=\"viewport\" content=\"width=device-width, user-scalable=false\" /></head>";
        String injectHeader = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no\" />"; //iOS uses these header settings
        style = "<style type=\"text/css\">\n" + style + ".token-card {\n" +
                "padding: 0pt;\n" +
                "margin: 0pt;\n" +
                "}</style></head>" +
                "<body>\n";
        // the opening of the following </div> is in injectWeb3TokenInit();
        return injectHeader + style + view + "</div></body>";
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieJar())
                .build();
    }
}
