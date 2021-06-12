package com.alphawallet.dapp.listeners

import com.alphawallet.dapp.utils.toJSONObjectOrNull
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class Web3EthCall(val callback: Callback) {

    interface Callback {
        fun onCallSuccessful(callbackId: Int, out: String)
        fun onCallFailure(callbackId: Int, throwable: Throwable)
    }


    private val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS).build()

    var fromAddress: String = ""
    var rpcUrl: String = ""

    fun ethCall(callbackId: Int, toAddress: String, payload: String) {

        val postJsonBody = """
                {"jsonrpc":"2.0","method":"eth_call","params":[{"from": "$fromAddress", "to": "$toAddress", "data": "$payload"}, "latest"], "id":1}
            """.trimIndent()

        val body: RequestBody = postJsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request: Request = Request.Builder()
            .url(rpcUrl)
            .post(body)
            .build()


        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onCallFailure(callbackId, e)
            }

            override fun onResponse(call: Call, response: Response) {

                val jsonResponse = response.body?.string()
                val json = jsonResponse?.toJSONObjectOrNull()
                val result = json?.optString("result")

                if (!response.isSuccessful || result == null) {
                    callback.onCallFailure(
                        callbackId,
                        RuntimeException("Http Response Not Successful or result is null ")
                    )
                    return
                }

                callback.onCallSuccessful(callbackId, result)
            }
        })
    }

}