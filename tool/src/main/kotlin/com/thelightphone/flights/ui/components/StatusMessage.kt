package com.thelightphone.flights.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * A composable function that displays a status message.
 *
 * @param message The message to display.
 */
@Composable
fun StatusMessage(
    message: String,
) {
    LightText(
        text = message,
        variant = LightTextVariant.Fine,
        modifier = Modifier.padding(
            start = 0.75f.gridUnitsAsDp(),
            end = 0.75f.gridUnitsAsDp(),
            bottom = 0.75f.gridUnitsAsDp()
        ),
        align = TextAlign.Justify,
        lighten = true,
    )

}
