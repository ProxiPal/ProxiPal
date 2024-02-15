package com.mongodb.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.R
import com.mongodb.app.ui.theme.MyApplicationTheme


/*
Contributions:
- Kevin Kubota (entire file)
 */


/**
 * Used for displaying a single line of text (without text wrapping)
 */
@Composable
fun SingleLineText(
    @StringRes textResourceId: Int,
    modifier: Modifier = Modifier,
    isItalic: Boolean = false,
){
    SingleLineText(
        text = stringResource(id = textResourceId),
        isItalic = isItalic,
        modifier = modifier
    )
}

/**
 * Used for displaying a single line of text (without text wrapping)
 */
@Composable
fun SingleLineText(
    text: String,
    modifier: Modifier = Modifier,
    isItalic: Boolean = false
){
    Text(
        text = text,
        fontStyle = if(isItalic) FontStyle.Italic else FontStyle.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = true,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SingleLineTextPreview(){
    MyApplicationTheme {
        SingleLineText(textResourceId = R.string.user_profile_test_string)
    }
}

/**
 * Used for displaying multiple lines of text (with text wrapping)
 */
@Composable
fun MultiLineText(
    @StringRes textResourceId: Int,
    modifier: Modifier = Modifier,
    isItalic: Boolean = false
){
    MultiLineText(
        text = stringResource(id = textResourceId),
        isItalic = isItalic,
        modifier = modifier
    )
}

/**
 * Used for displaying multiple lines of text (with text wrapping)
 */
@Composable
fun MultiLineText(
    text: String,
    modifier: Modifier = Modifier,
    isItalic: Boolean = false
){
    Text(
        text = text,
        fontStyle = if(isItalic) FontStyle.Italic else FontStyle.Normal,
        overflow = TextOverflow.Clip,
        softWrap = true,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MultiLineTextPreview(){
    MyApplicationTheme() {
        MultiLineText(textResourceId = R.string.user_profile_test_string)
    }
}