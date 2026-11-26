package com.hiklon.lubaai.web

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.hiklon.lubaai.R

/** Sélecteur de fichiers Android pour les champs HTML `<input type="file">`. */
class FileChooserHandler(private val activity: ComponentActivity) {

    private var callback: ValueCallback<Array<Uri>>? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cb = callback
        callback = null
        cb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data))
    }

    fun open(cb: ValueCallback<Array<Uri>>?, params: WebChromeClient.FileChooserParams?): Boolean {
        callback?.onReceiveValue(null)
        callback = cb
        return try {
            val intent = params?.createIntent()
                ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            launcher.launch(intent)
            true
        } catch (_: Exception) {
            callback = null
            Toast.makeText(activity, R.string.error_file_picker, Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun cancel() {
        callback?.onReceiveValue(null)
        callback = null
    }
}
