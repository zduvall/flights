package com.thelightphone.flights.ui.history

import androidx.lifecycle.viewModelScope
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

    fun deleteSearchQuery(flightNumber: String) {
        viewModelScope.launch {
            flightsRepository.deleteSearchQuery(flightNumber)
        }
    }

}
