package com.hiklon.lubaai.web

import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.hiklon.lubaai.BuildConfig

class LubaWebChromeClient(
    private val permissions: WebPermissionHandler,
    private val fileChooser: FileChooserHandler,
    private val onProgress: (Int) -> Unit
) : WebChromeClient() {

    override fun onPermissionRequest(request: PermissionRequest) = permissions.handle(request)

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean = fileChooser.open(filePathCallback, fileChooserParams)

    override fun onProgressChanged(view: WebView?, newProgress: Int) = onProgress(newProgress)

    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        if (BuildConfig.DEBUG) Log.d("LubaWeb", "${message.message()} (${message.sourceId()}:${message.lineNumber()})")
        return true
    }
}
