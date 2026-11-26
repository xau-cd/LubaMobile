package com.hiklon.lubaai.web

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.hiklon.lubaai.LubaConfig
import com.hiklon.lubaai.util.ExternalLinks

class LubaWebViewClient(
    private val activity: Activity,
    private val assetLoader: WebViewAssetLoader,
    private val listener: Listener
) : WebViewClient() {

    interface Listener {
        fun onPageStarted()
        fun onPageFinished(success: Boolean)
    }

    private var mainFrameError = false

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
        assetLoader.shouldInterceptRequest(request.url)

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        if (uri.scheme != "http" && uri.scheme != "https") return ExternalLinks.open(activity, uri)
        return if (LubaConfig.isInternalHost(uri.host)) false else ExternalLinks.open(activity, uri)
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        mainFrameError = false
        listener.onPageStarted()
    }

    override fun onPageFinished(view: WebView, url: String?) {
        listener.onPageFinished(!mainFrameError)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) mainFrameError = true
    }
}
