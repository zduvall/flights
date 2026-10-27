package com.thelightphone.flights.ui.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.flights.data.FlightsRepository
import kotlinx.coroutines.launch
import com.thelightphone.flights.data.SearchHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel containing the data and behavior for the main Flights Search Screen.
 */
class HistoryScreenViewModel(
    private val flightsRepository: FlightsRepository,
) : LightViewModel<Unit>() {

    val searchHistory: StateFlow<SearchHistory> = flightsRepository.searchHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = linkedMapOf()
        )

    /**
     * Persist the latest flight status search query to local disk.
     * REF-setLatestSearch: Duplicate code, keep in sync.
     * @param value The latest search query string to persist.
     */
    fun setLatestSearch(flightNumber: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                flightsRepository.setLatestSearch(flightNumber)
            }
        }
    }

    fun deleteSearchQuery(flightNumber: String) {
        viewModelScope.launch {
            flightsRepository.deleteSearchQuery(flightNumber)
        }
    }

}
