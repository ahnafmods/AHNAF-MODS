package com.ahnaf.mods

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var bubble: TextView? = null
    private var panel: WebView? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(9001, buildNotification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

    private fun showBubble() {
        if (bubble != null) return
        val bubbleBitmap = assets.open("assets/images/floating_logo.png").use {
            BitmapFactory.decodeStream(it)
        }
        val view = ImageView(this).apply {
            setImageBitmap(bubbleBitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundResource(R.drawable.bubble_bg)
            setPadding(dp(3), dp(3), dp(3), dp(3))
            elevation = 12f
            contentDescription = "Open AHNAF MODS V9 menu"
        }
        val params = WindowManager.LayoutParams(
            dp(56), dp(56), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(18)
            y = dp(180)
        }

        var downX = 0
        var downY = 0
        var startX = 0
        var startY = 0
        var dragged = false
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX.toInt()
                    downY = event.rawY.toInt()
                    startX = params.x
                    startY = params.y
                    dragged = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - downX
                    val dy = event.rawY.toInt() - downY
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > dp(7)) dragged = true
                    params.x = startX + dx
                    params.y = startY + dy
                    wm.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragged) showPanel()
                    true
                }
                else -> false
            }
        }

        bubble = view
        bubbleParams = params
        wm.addView(view, params)
    }

    private fun showPanel() {
        if (panel != null) return
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        bubble = null

        val web = WebView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webChromeClient = WebChromeClient()
            addJavascriptInterface(AndroidBridge(this@OverlayService), "Android")
            loadUrl("file:///android_asset/index.html")
        }

        val params = WindowManager.LayoutParams(
            dp(330), dp(590), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }
        panel = web
        panelParams = params
        wm.addView(web, params)
    }

    fun hidePanel() {
        mainHandler.post { hidePanelNow() }
    }

    private fun hidePanelNow() {
        panel?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            it.removeJavascriptInterface("Android")
            it.destroy()
        }
        panel = null
        panelParams = null
        showBubble()
    }

    fun closeOverlay() {
        mainHandler.post { stopSelf() }
    }

    fun showNativeToast(message: String) {
        mainHandler.post {
            Toast.makeText(this, message.take(100), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        panel?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            it.destroy()
        }
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel = null
        bubble = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ahnaf_overlay",
                "Floating menu",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "ahnaf_overlay")
        else
            @Suppress("DEPRECATION") Notification.Builder(this)
        return builder
            .setContentTitle("AHNAF MODS V9")
            .setContentText("Floating menu is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
