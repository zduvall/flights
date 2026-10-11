package com.thelightphone.flights.ui.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.FlightsRepository
import com.thelightphone.flights.data.SettingsRepository
import com.thelightphone.flights.model.EditorRequest
import com.thelightphone.flights.ui.editor.TextInputEditorScreen
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

class SearchScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SearchScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchScreenViewModel>
        get() = SearchScreenViewModel::class.java

    override fun createViewModel(): SearchScreenViewModel {
        val flightsRepository = FlightsRepository(lightContext.dataStore)
        val settingsRepository = SettingsRepository(lightContext.dataStore)

        val apiClient = AeroDataBoxClient(settingsRepository = settingsRepository)

        return SearchScreenViewModel(flightsRepository, apiClient)
    }

    @Composable
    override fun Content() {
        val latestSearchValue by viewModel.latestSearch.collectAsState()
        val searchHistoryValue by viewModel.searchHistory.collectAsState()

        val isSearchPopulated = latestSearchValue.isNotBlank()
        val standardFlightNumber = standardizeFlightNumber(latestSearchValue)

        TabScaffold(
            title = "Search",
            activeTab = FlightsTab.Search,
            onNavigate = { navigateTo(it) },
        ) {
            // Calculate outside layout lambda b/c it isn't a composable context
            val oneGridUnit = 1f.gridUnitsAsDp()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // LightTextField internally adds 1-grid-unit top padding;
                    // counteract so spacing matches other tabs
                    // TODO: remove if LightTextField becomes more configurable
                    // https://github.com/lightphone/light-sdk/issues/277
                    .layout { measurable, constraints ->
                        val topReduction = oneGridUnit.roundToPx()
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, (placeable.height - topReduction).coerceAtLeast(0)) {
                            placeable.placeRelative(0, -topReduction)
                        }
                    }
                    .padding(bottom = 1f.gridUnitsAsDp()),
                verticalAlignment = Alignment.Bottom,
            ) {
                LightTextField(
                    label = "Flight Code:",
                    value = latestSearchValue,
                    placeholder = "",
                    onClick = {
                        val editorRequest = EditorRequest(
                            title = "Flight Code",
                            initialValue = latestSearchValue,
                        )
                        navigateTo(
                            screenFactory = { TextInputEditorScreen(it, editorRequest) },
                            resultCallback = { query ->
                                viewModel.setLatestSearch(query)
                                viewModel.handleSearchQuery(query)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                if (isSearchPopulated) {
                    LightIcon(
                        icon = LightIcons.REFRESH,
                        modifier = Modifier.lightClickable {
                            viewModel.handleSearchQuery()
                        },
                    )
                }
            }

            when {
                viewModel.isLoading || viewModel.errorMessage != null -> {
                    val displayText = if (viewModel.isLoading) "Loading..." else viewModel.errorMessage.orEmpty()

                    LightText(
                        text = displayText,
                        variant = LightTextVariant.Fine,
                        modifier = Modifier.padding(
                            start = 0.75f.gridUnitsAsDp(),
                            end = 0.75f.gridUnitsAsDp(),
                            bottom = 0.75f.gridUnitsAsDp()
                        ),
                        align = TextAlign.Justify,
                        lighten = true
                    )
                }

                else -> {
                    val flightStatus = searchHistoryValue[standardFlightNumber].orEmpty()

                    flightStatus.forEachIndexed { index, status ->
                        FlightStatusRow(status)
                        if (index < flightStatus.lastIndex) {
                            Spacer(modifier = Modifier.height(0.75f.gridUnitsAsDp()))
                        }
                    }
                }
            }
        }
    }
}
