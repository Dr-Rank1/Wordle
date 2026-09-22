package com.rank.lexi.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

/**
 * Compose-friendly Start.io banner ad.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            Banner(context).apply {
                setBannerListener(object : BannerListener {
                    override fun onReceiveAd(p0: android.view.View?) {}
                    override fun onFailedToReceiveAd(p0: android.view.View?) {}
                    override fun onImpression(p0: android.view.View?) {}
                    override fun onClick(p0: android.view.View?) {}
                })
                loadAd()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
    )
}
