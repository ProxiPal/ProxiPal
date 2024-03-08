package com.mongodb.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight


/*
Contributions:
- Kevin Kubota (entire file)
 */


/*
===== Functions =====
 */
/**
 * Displays a row with a single button in the center
 */
@Composable
fun SingleButtonRow(
    onButtonClick: (() -> Unit),
    @StringRes
    textId: Int,
    modifier: Modifier = Modifier
){
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = { onButtonClick() }
        ) {
            Text(
                text = stringResource(
                    id = textId
                )
            )
        }
    }
}

/**
 * Displays a row with some text in the center
 */
@Composable
fun SingleTextRow(
    @StringRes
    textId: Int,
    isTextBold: Boolean,
    modifier: Modifier = Modifier
){
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(
                id = textId
            ),
            fontWeight = if (isTextBold) FontWeight.Bold else null
        )
    }
}