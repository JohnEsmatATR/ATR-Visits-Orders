package com.akhnaton.foodvisits.shared.debugBanner

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.ConstantLinks

object DebugBannerManager {
    fun show(activity: Activity) {
        if (ConstantLinks.isProd()) return
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        if (root.findViewWithTag<View>("DEBUG_BANNER") != null) return
        val banner = LayoutInflater.from(activity)
            .inflate(R.layout.debug_banner, root, false)
        banner.tag = "DEBUG_BANNER"
        root.addView(banner)
    }
}