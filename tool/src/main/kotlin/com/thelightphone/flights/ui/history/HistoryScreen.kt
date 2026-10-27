package com.thelightphone.flights.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.thelightphone.flights.data.FlightsRepository
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.flights.ui.search.SearchScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable


class HistoryScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, HistoryScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HistoryScreenViewModel>
        get() = HistoryScreenViewModel::class.java

    override fun createViewModel(): HistoryScreenViewModel {
        val flightsRepository = FlightsRepository(lightContext.dataStore)
        return HistoryScreenViewModel(flightsRepository)
    }

    @Composable
    override fun Content() {
        val searchHistory = viewModel.searchHistory.collectAsState()

        TabScaffold(
            title = "Search History",
            activeTab = FlightsTab.History,
            onNavigate = { navigateTo(it) },
        ) {
            for (flightNumber in searchHistory.value.keys.reversed()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 1f.gridUnitsAsDp()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LightText(
                        text = flightNumber,
                        variant = LightTextVariant.Subheading,
                        modifier = Modifier.lightClickable {
                            viewModel.setLatestSearch(flightNumber)
                            navigateTo(::SearchScreen)
                        }
                    )
                    LightIcon(
                        icon = LightIcons.DELETE,
                        modifier = Modifier.lightClickable {
                            viewModel.deleteSearchQuery(flightNumber)
                        },
                        size = 1.5f
                    )
                }
            }
        }
    }
}
