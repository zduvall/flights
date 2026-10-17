package com.thelightphone.flights.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thelightphone.flights.model.Flight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

typealias SearchHistory = LinkedHashMap<String, List<Flight>>

/**
 * Shared repository for flight search data.
 */
class FlightsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val latestSearchKey = stringPreferencesKey("latest_search")
    private val searchHistoryKey = stringPreferencesKey("search_history")

    val latestSearchFlow: Flow<String> = dataStore.data.map {
        it[latestSearchKey] ?: ""
    }

    val searchHistoryFlow: Flow<SearchHistory> = dataStore.data.map {
        val json = it[searchHistoryKey] ?: "{}"
        Json.decodeFromString<SearchHistory>(json)
    }

    suspend fun setLatestSearch(value: String) {
        dataStore.edit { it[latestSearchKey] = value }
    }

    suspend fun addSearchQuery(flightNumber: String, flights: List<Flight>) {
        dataStore.edit {
            val json = it[searchHistoryKey] ?: "{}"
            val searchHistory = Json.decodeFromString<SearchHistory>(json)
            searchHistory.remove(flightNumber)
            searchHistory[flightNumber] = flights
            it[searchHistoryKey] = Json.encodeToString(searchHistory)
        }
    }

    suspend fun deleteSearchQuery(flightNumber: String) {
        dataStore.edit {
            val json = it[searchHistoryKey] ?: "{}"
            val searchHistory = Json.decodeFromString<SearchHistory>(json)
            searchHistory.remove(flightNumber)
            it[searchHistoryKey] = Json.encodeToString(searchHistory)
        }
    }
}
