package com.alphawallet

import android.os.Bundle
import android.view.KeyEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.alphawallet.dapp.Web3View
import com.alphawallet.dapp.listeners.DappRequestListener
import com.alphawallet.dapp.model.DappMessage
import com.alphawallet.dapp.model.DappTransaction

class MainActivity : AppCompatActivity() {

    private lateinit var webView: Web3View
    private lateinit var searchBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        searchBar = findViewById(R.id.urlBar)
        webView = findViewById(R.id.webview)
        webView.setChainId(1)
        webView.setRpcUrl("https://mainnet.infura.io/v3/da3717f25f824cc1baa32d812386d93f")
        webView.setWalletAddress("0x615e8391d3f17fa4e29824b29ceeade2c3adcf4e")

        searchBar.setOnEditorActionListener{ _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                webView.loadUrl(searchBar.text.toString().trim())
                true
            }
            false
        }

        webView.setDappListener(object : DappRequestListener {
            override fun onTransactionSignRequest(transaction: DappTransaction) {
                Toast.makeText(this@MainActivity, "Tx(${transaction.toAddress}, ${transaction.value}", Toast.LENGTH_LONG).show()
            }

            override fun onMessageSignRequest(message: DappMessage) {
                Toast.makeText(this@MainActivity, "Tx(${message.message}", Toast.LENGTH_LONG).show()
            }

        })
    }
}