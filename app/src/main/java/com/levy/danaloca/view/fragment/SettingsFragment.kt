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

        // Setup logout button
        val logoutButton = view.findViewById<ImageButton>(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        // Setup WebView
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
            defaultTextEncodingName = "utf-8"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "Error loading: ${error?.description}")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page loaded successfully: $url")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(TAG, "Console: ${consoleMessage?.message()}")
                return true
            }
        }

        try {
            // Load HTML content from raw resource
            val inputStream = resources.openRawResource(R.raw.chat)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val htmlContent = reader.readLines().joinToString("\n")
            reader.close()
            
            // Load with base URL to allow loading external resources
            webView.loadDataWithBaseURL(
                "https://sf-cdn.coze.com",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
            
            Log.d(TAG, "HTML content loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading HTML: ${e.message}")
            e.printStackTrace()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.clearCache(true)
        webView.clearHistory()
    }
}