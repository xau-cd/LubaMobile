package com.hiklon.lubaai

import android.net.Uri

/** Constantes centralisées de l'application. */
object LubaConfig {
    const val ASSET_HOST = "appassets.androidplatform.net"
    const val START_URL = "https://$ASSET_HOST/assets/www/index.html"

    /** Durée maximale d'affichage du splash si la page tarde à se charger. */
    const val SPLASH_TIMEOUT_MS = 2500L

    /** Hôtes qui restent dans la WebView (identiques à la version initiale). */
    private val INTERNAL_HOSTS = listOf(
        ASSET_HOST,
        "luba.web.app",
        "luba-backend-7bqk.onrender.com",
        "firebaseapp.com",
        "web.app",
        "googleapis.com",
        "gstatic.com",
        "google.com",
        "googleusercontent.com"
    )

    fun isInternalHost(host: String?): Boolean {
        val h = host?.lowercase().orEmpty()
        return INTERNAL_HOSTS.any { h == it || h.endsWith(".$it") }
    }

    /** Seule la page embarquée est autorisée à utiliser le pont natif et micro/caméra. */
    fun isTrustedPage(url: String?): Boolean =
        url != null && Uri.parse(url).host.equals(ASSET_HOST, ignoreCase = true)
}
