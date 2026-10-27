package com.thelightphone.flights.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.flights.model.Flight
import com.thelightphone.flights.model.FlightStatus
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FLIGHT_DATE_FORMATTER = DateTimeFormatter.ofPattern("E, MMM d", Locale.ENGLISH)
private val FLIGHT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

fun parseOffsetDateTimeOrNull(raw: String?): OffsetDateTime? {
    if (raw.isNullOrBlank()) return null

    val sanitized = raw.trim().replace(' ', 'T')

    return runCatching {
        OffsetDateTime.parse(sanitized)
    }.recoverCatching {
        LocalDateTime.parse(sanitized).atOffset(ZoneOffset.UTC)
    }.getOrNull()
}

fun formatFlightDate(rawLocalTime: String?): String =
    parseOffsetDateTimeOrNull(rawLocalTime)?.format(FLIGHT_DATE_FORMATTER).orEmpty()

fun formatFlightTime24h(rawLocalTime: String?): String =
    parseOffsetDateTimeOrNull(rawLocalTime)?.format(FLIGHT_TIME_FORMATTER).orEmpty()

private fun formatLocationDate(
    municipalityName: String?,
    shortName: String?,
    name: String?,
    localTime: String?,
): String {
    val city = municipalityName ?: shortName ?: name ?: ""
    val date = formatFlightDate(localTime)

    return listOfNotNull(
        city.takeIf { it.isNotBlank() },
        date.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
}

private fun formatTerminalGate(terminal: String?, gate: String?): String {
    return listOfNotNull(
        terminal?.takeIf { it.isNotBlank() }?.let { "Terminal $it" },
        gate?.takeIf { it.isNotBlank() }?.let { "Gate $it" },
    ).joinToString(" · ")
}

@Composable
fun FlightStatusRow(
    fS: Flight,
    modifier: Modifier = Modifier,
) {
    val depLocationDate = formatLocationDate(
        municipalityName = fS.departure.airport.municipalityName,
        shortName = fS.departure.airport.shortName,
        name = fS.departure.airport.name,
        localTime = fS.departure.revisedTime?.local
            ?: fS.departure.scheduledTime?.local
            ?: fS.departure.runwayTime?.local,
    )

    val arrLocationDate = formatLocationDate(
        municipalityName = fS.arrival.airport.municipalityName,
        shortName = fS.arrival.airport.shortName,
        name = fS.arrival.airport.name,
        localTime = fS.arrival.revisedTime?.local
            ?: fS.arrival.scheduledTime?.local
            ?: fS.arrival.runwayTime?.local
            ?: fS.arrival.predictedTime?.local,
    )

    val depTermGate = formatTerminalGate(fS.departure.terminal, fS.departure.gate)
    val arrTermGate = formatTerminalGate(fS.arrival.terminal, fS.arrival.gate)

    val showTerminalGate = listOf(
        fS.departure.terminal,
        fS.departure.gate,
        fS.arrival.terminal,
        fS.arrival.gate,
    ).any { !it.isNullOrBlank() }

    val isArrived = fS.status == FlightStatus.ARRIVED
    val isDeparted = isArrived || fS.status in setOf(
        FlightStatus.DEPARTED,
        FlightStatus.EN_ROUTE,
        FlightStatus.APPROACHING,
    )

    val departureStatusLabel = if (isDeparted) "Departed" else "Scheduled"
    val arrivalStatusLabel = when {
        isArrived -> "Arrived"
        isDeparted -> "Estimated"
        else -> "Scheduled"
    }

    val departureActiveTime = if (isDeparted) {
        fS.departure.revisedTime?.local
            ?: fS.departure.runwayTime?.local
            ?: fS.departure.scheduledTime?.local
    } else {
        fS.departure.revisedTime?.local
            ?: fS.departure.predictedTime?.local
            ?: fS.departure.scheduledTime?.local
    }

    val arrivalActiveTime = if (isArrived) {
        fS.arrival.revisedTime?.local
            ?: fS.arrival.runwayTime?.local
            ?: fS.arrival.scheduledTime?.local
    } else {
        fS.arrival.revisedTime?.local
            ?: fS.arrival.predictedTime?.local
            ?: fS.arrival.scheduledTime?.local
    }

    val depTimeFormatted = formatFlightTime24h(departureActiveTime).ifEmpty { "--:--" }
    val arrTimeFormatted = formatFlightTime24h(arrivalActiveTime).ifEmpty { "--:--" }

    Column(modifier = modifier) {
        // 1. IATA codes (e.g. "JFK" or "LAX")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightText(
                fS.departure.airport.iata ?: "---",
                variant = LightTextVariant.Subtitle,
            )
            LightText(
                fS.arrival.airport.iata ?: "---",
                variant = LightTextVariant.Subtitle,
            )
        }

        // 2. Terminal and gate (when at least one is present)
        if (showTerminalGate) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LightText(depTermGate, variant = LightTextVariant.Detail, lighten = true)
                LightText(arrTermGate, variant = LightTextVariant.Detail, lighten = true)
            }
        }

        // 3. City / Municipality · Date (e.g. "New York · Sat, Oct 3")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightText(depLocationDate, variant = LightTextVariant.Detail)
            LightText(arrLocationDate, variant = LightTextVariant.Detail)
        }

        // 4. Active times and movement status labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LightText("$depTimeFormatted ", variant = LightTextVariant.Copy)
                LightText(
                    departureStatusLabel,
                    variant = LightTextVariant.Detail,
                    lighten = true,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LightText(
                    arrivalStatusLabel,
                    variant = LightTextVariant.Detail,
                    lighten = true,
                )
                LightText(" $arrTimeFormatted", variant = LightTextVariant.Copy)
            }
        }

        // 5. Crossed out scheduled time if different
        val depScheduledTimeFormatted =
            formatFlightTime24h(fS.departure.scheduledTime?.local)
        val arrScheduledTimeFormatted =
            formatFlightTime24h(fS.arrival.scheduledTime?.local)

        val showDepStrikethrough =
            depScheduledTimeFormatted.isNotEmpty() &&
                    depScheduledTimeFormatted != depTimeFormatted

        val showArrStrikethrough =
            arrScheduledTimeFormatted.isNotEmpty() &&
                    arrScheduledTimeFormatted != arrTimeFormatted

        if (showDepStrikethrough || showArrStrikethrough) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LightText(
                    text = depScheduledTimeFormatted.takeIf { showDepStrikethrough }.orEmpty(),
                    variant = LightTextVariant.Fine,
                    lighten = true,
                    strikethrough = showDepStrikethrough,
                )

                LightText(
                    text = arrScheduledTimeFormatted.takeIf { showArrStrikethrough }.orEmpty(),
                    variant = LightTextVariant.Fine,
                    lighten = true,
                    strikethrough = showArrStrikethrough,
                )
            }
        }
    }
}