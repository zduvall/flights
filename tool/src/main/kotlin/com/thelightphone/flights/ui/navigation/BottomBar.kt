package com.thelightphone.flights.ui.navigation

import androidx.compose.runtime.Composable
import com.thelightphone.flights.ui.history.HistoryScreen
import com.thelightphone.flights.ui.search.SearchScreen
import com.thelightphone.flights.ui.settings.SettingsScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightBottomBarItem
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons

private data class TabConfig(
    val tab: FlightsTab,
    val icon: LightIconConfiguration,
    val title: String,
    val screenFactory: (SealedLightActivity) -> SimpleLightScreen<Unit>,
)

private val tabs = listOf(
    TabConfig(FlightsTab.Settings, LightIcons.SETTINGS, "Settings", ::SettingsScreen),
    TabConfig(FlightsTab.Search, LightIcons.AIRPLANE, "Search", ::SearchScreen),
    TabConfig(FlightsTab.History, LightIcons.LIST, "History", ::HistoryScreen),
)

@Composable
fun BottomBar(
    active: FlightsTab,
    onNavigate: ((SealedLightActivity) -> SimpleLightScreen<Unit>) -> Unit,
) {
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

    LightBottomBar(items = items)
}
