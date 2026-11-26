package com.hiklon.lubaai.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hiklon.lubaai.R

object ExternalLinks {
    /** Ouvre l'URI dans une application externe. Retourne toujours true (navigation gérée). */
    fun open(activity: Activity, uri: Uri): Boolean {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.error_link_open, Toast.LENGTH_SHORT).show()
        }
        return true
    }
}
