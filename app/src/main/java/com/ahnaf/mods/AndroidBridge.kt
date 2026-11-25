package com.ahnaf.mods

import android.webkit.JavascriptInterface

class AndroidBridge(private val service: OverlayService) {
    @JavascriptInterface
    fun showToast(message: String) {
        service.showNativeToast(message)
    }

    @JavascriptInterface
    fun hideMenu() {
        service.hidePanel()
    }

    @JavascriptInterface
    fun closeMenu() {
        service.closeOverlay()
    }
}
