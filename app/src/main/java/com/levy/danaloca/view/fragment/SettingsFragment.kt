package com.levy.danaloca.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.levy.danaloca.R
import com.levy.danaloca.view.activity.HomeActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsFragment : Fragment() {

    private lateinit var webView: WebView
    private val TAG = "SettingsFragment"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val logoutButton = view.findViewById<ImageButton>(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        webView = view.findViewById(R.id.coze_webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = WebSettings.getDefaultUserAgent(context) + " Mobile" // Thêm User-Agent
        }

        WebView.setWebContentsDebuggingEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                Log.e(TAG, "Error loading: ${error?.description}")
                // Hiển thị thông báo lỗi hoặc giao diện fallback
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "Page loaded: $url")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(TAG, "Console: ${consoleMessage?.message()}")
                return true
            }
        }


        // Tải GitHub Pages
        webView.loadUrl("https://levy1101.github.io/coze-chat/")
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.clearCache(true)
        webView.clearHistory()
    }
}


