package com.pollyannawu.justwoo.android.ui.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pollyannawu.justwoo.android.R

@Composable
fun JustWooLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_justwoo_wordmark),
        contentDescription = "JustWoo",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
