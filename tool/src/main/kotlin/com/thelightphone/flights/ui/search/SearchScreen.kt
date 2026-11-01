package com.thelightphone.flights.ui.search

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.Instant
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
                    .padding(bottom = 0.25f.gridUnitsAsDp()),
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
                    val lastSearch = searchHistoryValue[latestSearchValue]

                    // Display the time since the last search for the current flight number
                    val searchedAtUtc = lastSearch?.searchedAtUtc
                    if (searchedAtUtc != null) {
                        val searchedAt = remember(searchedAtUtc) { Instant.parse(searchedAtUtc) }
                        RefreshedAgoText(searchedAt)
                    }

                    val flightStatus = lastSearch?.flights.orEmpty()
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

/**
 * Displays and periodically refreshes the relative time since [searchedAt].
 */
@Composable
private fun RefreshedAgoText(searchedAt: Instant) {
    val timeAgo by produceState(
        initialValue = makeTimeAgo(searchedAt),
        key1 = searchedAt,
    ) {
        // Auto-update timeAgo on a regular interval
        value = makeTimeAgo(searchedAt)
        while (true) {
            delay(30.seconds)
            value = makeTimeAgo(searchedAt)
        }
    }

    LightText(
        text = "Refreshed $timeAgo",
        variant = LightTextVariant.Detail,
        modifier = Modifier.padding(bottom = 0.75f.gridUnitsAsDp()),
        lighten = true,
    )
}

/* 
 * Returns human-readable relative time string for the given Instant.
 */
fun makeTimeAgo(searchedAt: Instant): CharSequence {
    val timeAgo = DateUtils.getRelativeTimeSpanString(
        searchedAt.toEpochMilli(),
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    )
    if (timeAgo == "0 minutes ago") {
        return "moments ago"
    }
    return timeAgo
}