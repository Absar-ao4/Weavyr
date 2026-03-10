package com.weavyr.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun openInChromeTab(context: Context, url: String) {

    val intent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()

    intent.launchUrl(context, Uri.parse(url))
}