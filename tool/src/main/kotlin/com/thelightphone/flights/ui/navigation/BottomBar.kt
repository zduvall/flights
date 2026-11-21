package com.thelightphone.flights.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.thelightphone.flights.ui.history.HistoryScreen
import com.thelightphone.flights.ui.search.SearchScreen
import com.thelightphone.flights.ui.settings.SettingsScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightBottomBarItem
import com.thelightphone.sdk.ui.LightColors
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightSurfaceScheme
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeTokens

private data class TabConfig(
    val tab: FlightsTab,
    val icon: LightIconConfiguration,
    val title: String,
    val screenFactory: (SealedLightActivity) -> SimpleLightScreen<Unit>,
)

private val tabs = listOf(
    TabConfig(FlightsTab.Search, LightIcons.AIRPLANE, "Search", ::SearchScreen),
    TabConfig(FlightsTab.History, LightIcons.LIST, "History", ::HistoryScreen),
    TabConfig(FlightsTab.Settings, LightIcons.SETTINGS, "Settings", ::SettingsScreen),
)

@Composable
fun BottomBar(
    active: FlightsTab,
    onNavigate: ((SealedLightActivity) -> SimpleLightScreen<Unit>) -> Unit,
) {
    val colors = LightThemeTokens.colors
    val scheme = LightThemeTokens.surfaceScheme
    val typography = LightThemeTokens.typography

    val items: List<LightBottomBarItem> = tabs.map { config ->
        val isActive = active == config.tab
        LightBarButton.LightIcon(
            icon = config.icon,
            contentDescription = config.title,
            onClick = if (isActive) null else {
                { onNavigate(config.screenFactory) }
            },
        )
    }

    Box {
        // Rendering LightBottomBar 2x in order to achieve the effect of dimming inactive icons
        // If SDK supports dimming natively in the future, this workaround can be removed.

        // All icons dimmed
        LightTheme(colors = mutedColors(colors, scheme), typography = typography, surfaceScheme = scheme) {
            LightBottomBar(items = items)
        }
        // Active icon at full intensity (inactive slots are null → invisible spacers)
        LightBottomBar(
            items = items.map { item ->
                if ((item as? LightBarButton.LightIcon)?.onClick == null) item else null
            },
        )
    }
}

private fun mutedColors(colors: LightColors, scheme: LightSurfaceScheme): LightColors {
    val alpha = if (scheme == LightSurfaceScheme.Dark) 0.4f else 0.38f
    return colors.copy(content = colors.content.copy(alpha = alpha))
}
