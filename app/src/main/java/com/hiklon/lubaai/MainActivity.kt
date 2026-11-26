package com.hiklon.lubaai

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.webkit.WebViewAssetLoader
import com.hiklon.lubaai.bridge.LubaBridge
import com.hiklon.lubaai.databinding.ActivityMainBinding
import com.hiklon.lubaai.web.FileChooserHandler
import com.hiklon.lubaai.web.LubaWebChromeClient
import com.hiklon.lubaai.web.LubaWebViewClient
import com.hiklon.lubaai.web.WebPermissionHandler
import com.hiklon.lubaai.web.WebViewConfigurator

/** Conteneur fin : toute la logique WebView est déléguée au package `web` et `bridge`. */
class MainActivity : ComponentActivity(), LubaWebViewClient.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissions: WebPermissionHandler
    private lateinit var fileChooser: FileChooserHandler
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { keepSplash }

        val barStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        permissions = WebPermissionHandler(this)
        fileChooser = FileChooserHandler(this)
        setupWebView()
        setupErrorScreen()
        setupBackNavigation()

        binding.root.postDelayed({ keepSplash = false }, LubaConfig.SPLASH_TIMEOUT_MS)

        if (savedInstanceState == null) {
            binding.webView.loadUrl(LubaConfig.START_URL)
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupWebView() {
        val webView = binding.webView
        WebViewConfigurator.apply(webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain(LubaConfig.ASSET_HOST)
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.addJavascriptInterface(LubaBridge(this, webView), LubaBridge.NAME)
        webView.webViewClient = LubaWebViewClient(this, assetLoader, this)
        webView.webChromeClient = LubaWebChromeClient(permissions, fileChooser) { progress ->
            binding.progress.progress = progress
            binding.progress.isVisible = progress in 1..99
        }
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: Exception) {
                Toast.makeText(this, R.string.error_download, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupErrorScreen() {
        binding.retryButton.setOnClickListener {
            binding.errorView.isVisible = false
            binding.webView.reload()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // --- LubaWebViewClient.Listener ---

    override fun onPageStarted() {
        binding.errorView.isVisible = false
    }

    override fun onPageFinished(success: Boolean) {
        binding.errorView.isVisible = !success
        keepSplash = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        permissions.cancel()
        fileChooser.cancel()
        binding.webView.apply {
            stopLoading()
            removeJavascriptInterface(LubaBridge.NAME)
            (parent as? android.view.ViewGroup)?.removeView(this)
            destroy()
        }
        super.onDestroy()
    }
}
