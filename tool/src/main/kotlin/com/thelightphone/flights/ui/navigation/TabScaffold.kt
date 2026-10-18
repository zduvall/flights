package com.thelightphone.flights.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightScrollBarPosition
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * Shared scaffolding layout for top-level tab screens.
 */
@Composable
fun TabScaffold(
    title: String,
    activeTab: FlightsTab,
    onNavigate: ((SealedLightActivity) -> SimpleLightScreen<Unit>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val themeColors by LightThemeController.colors.collectAsState()
    val scrollState = rememberScrollState()

    // since it takes up space either way when using "Outside", use that setting
    // only when there is content to scroll
    val scrollBarPosition = if (scrollState.maxValue > 0) {
        LightScrollBarPosition.Outside
    } else {
        LightScrollBarPosition.Inside
    }

    LightTheme(colors = themeColors) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LightThemeTokens.colors.background)
        ) {
            LightTopBar(
                center = LightTopBarCenter.Text(title),
                modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
            )

            LightScrollView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 1f.gridUnitsAsDp()),
                scrollState = scrollState,
                scrollBarPosition = scrollBarPosition,
                content = content,
            )

            BottomBar(
                active = activeTab,
                onNavigate = onNavigate,
            )
        }
    }
}