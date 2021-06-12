package com.alphawallet

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alphawallet.dapp.Web3View
import com.alphawallet.dapp.listeners.DappRequestListener
import com.alphawallet.dapp.model.DappMessage
import com.alphawallet.dapp.model.DappTransaction

class MainActivity : AppCompatActivity() {

    private lateinit var webView: Web3View
    private lateinit var searchBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dapp)
        setSupportActionBar(findViewById(R.id.toolbar))

        searchBar = findViewById(R.id.urlBar)
        webView = findViewById(R.id.webview)
        webView.setChainId(1)
        webView.setRpcUrl("https://mainnet.infura.io/v3/da3717f25f824cc1baa32d812386d93f")
        webView.setWalletAddress("0xfc43f5f9dd45258b3aff31bdbe6561d97e8b71de")

        searchBar.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                webView.loadUrl(searchBar.text.toString().trim())
                true
            }
            false
        }

        webView.setDappListener(object : DappRequestListener {
            override fun onTransactionSignRequest(transaction: DappTransaction) {
                Toast.makeText(
                    this@MainActivity,
                    "Tx(${transaction.toAddress}, ${transaction.value}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onMessageSignRequest(message: DappMessage) {
                Toast.makeText(this@MainActivity, "Tx(${message.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}