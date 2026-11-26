package com.hiklon.lubaai.web

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hiklon.lubaai.LubaConfig

/** Fait le lien entre les demandes micro/caméra de la page web et les permissions Android. */
class WebPermissionHandler(private val activity: ComponentActivity) {

    private var pending: PermissionRequest? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        pending?.let { respond(it) }
        pending = null
    }

    fun handle(request: PermissionRequest) {
        if (!LubaConfig.isTrustedPage(request.origin.toString())) {
            request.deny()
            return
        }
        val missing = buildList {
            val r = request.resources
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in r && !has(Manifest.permission.RECORD_AUDIO)) add(Manifest.permission.RECORD_AUDIO)
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in r && !has(Manifest.permission.CAMERA)) add(Manifest.permission.CAMERA)
        }
        if (missing.isEmpty()) {
            respond(request)
        } else {
            pending?.deny()
            pending = request
            launcher.launch(missing.toTypedArray())
        }
    }

    fun cancel() {
        pending?.deny()
        pending = null
    }

    private fun respond(request: PermissionRequest) {
        val allowed = request.resources.filter {
            when (it) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> has(Manifest.permission.RECORD_AUDIO)
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> has(Manifest.permission.CAMERA)
                else -> false
            }
        }.toTypedArray()
        if (allowed.isNotEmpty()) request.grant(allowed) else request.deny()
    }

    private fun has(permission: String) =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}
