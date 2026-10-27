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
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.FlightsRepository
import com.thelightphone.flights.data.SettingsRepository
import com.thelightphone.flights.model.EditorRequest
import com.thelightphone.flights.ui.components.StatusMessage
import com.thelightphone.flights.ui.editor.TextInputEditorScreen
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextFieldUnderlineStyle
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

@InitialScreen
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

        TabScaffold(
            title = "Search",
            activeTab = FlightsTab.Search,
            onNavigate = { navigateTo(it) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                                val normalizedFN = normalizeFlightNumber(query)
                                viewModel.setLatestSearch(normalizedFN)
                                viewModel.handleSearchQuery(normalizedFN)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    labelModifier = Modifier, // Override default top padding on label
                    underlineStyleOverrides = LightTextFieldUnderlineStyle(
                        widthFraction = 0.95f,
                    )
                )

                if (isSearchPopulated) {
                    // Refresh search for the current flight number
                    LightIcon(
                        icon = LightIcons.REFRESH,
                        modifier = Modifier.lightClickable {
                            viewModel.handleSearchQuery(latestSearchValue)
                        },
                    )
                }
            }

            when {
                viewModel.isLoading || viewModel.errorMessage != null -> {
                    val statusMessage = if (viewModel.isLoading) "Loading..." else viewModel.errorMessage.orEmpty()
                    StatusMessage(statusMessage)
                }

                else -> {
                    val flightStatus = searchHistoryValue[latestSearchValue].orEmpty()

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

/**
 * Normalizes flight number by removing whitespace & converting to uppercase.
 */
fun normalizeFlightNumber(flightNumber: String): String {
    return flightNumber.replace("\\s".toRegex(), "").uppercase()
}
