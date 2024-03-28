package com.mongodb.app.home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import kotlinx.coroutines.launch


//ADDED BY GEORGE FU


//created a Filter Screen
@Composable
fun FilterScreen(navController: NavHostController, viewModel: UserProfileViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val interests = listOf("Gaming", "Music", "Activity", "Fashion", "Nature", "Food/Drinks", "Technology", "Arts/Culture", "Travel")
    val industries = listOf("Technology", "Healthcare", "Finance", "Education", "Service Industry", "Retail", "Manufacturing", "Arts/Entertainment")

    // State initialization
    val selectedInterests = remember { mutableStateOf(viewModel.selectedInterests.value.toSet()) }
    val selectedIndustries = remember { mutableStateOf(viewModel.selectedIndustries.value.toSet()) }
    var otherChips = remember { mutableStateOf(viewModel.otherFilters.value) }



    Scaffold(
        topBar = { FilterTopAppBar { navController.navigateUp() } },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                // SearchBar for adding other chips dynamically
                SearchBar(
                    onDone = { inputText ->
                        if (inputText.isNotBlank() && !otherChips.value.contains(inputText)) {
                            otherChips.value = otherChips.value + listOf(inputText)
                        }
                    },
                    isInputEnabled = otherChips.value.size < 8 // Limit to 8
                )

                // Interests Section
                Text("Interests", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                DefaultChipsLayout(
                    chips = interests,
                    selectedChips = selectedInterests.value,
                    onChipClick = { chip ->
                        val currentSelection = selectedInterests.value.toMutableSet()
                        if (currentSelection.contains(chip)) {
                            currentSelection.remove(chip)
                        } else {
                            currentSelection.add(chip)
                        }
                        selectedInterests.value = currentSelection
                    }
                )

                // Industries Section
                Text("Industries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                DefaultChipsLayout(
                    chips = industries,
                    selectedChips = selectedIndustries.value,
                    onChipClick = { chip ->
                        val currentSelection = selectedIndustries.value.toMutableSet()
                        if (currentSelection.contains(chip)) {
                            currentSelection.remove(chip)
                        } else {
                            currentSelection.add(chip)
                        }
                        selectedIndustries.value = currentSelection
                    }
                )

                // Others Section for dynamic chip addition based on user input
                Text("Others", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                ChipsLayout(chips = otherChips.value, onDeleteChip = { index ->
                    otherChips.value = otherChips.value.toMutableList().also { it.removeAt(index) }

                })
                Spacer(modifier = Modifier.weight(1f))


                // Apply Filter button
                Button(
                    onClick = {
                        viewModel.saveUserFilterSelections(selectedInterests.value, selectedIndustries.value, otherChips.value)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Filters saved")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Apply Filter")
                }

                // Cancel button
                OutlinedButton(
                    onClick = {
                        viewModel.clearUserFilterSelections()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Filters Unsaved, Let's Apply Some Filters")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun DefaultChipsLayout(chips: List<String>, selectedChips: Set<String>, onChipClick: (String) -> Unit) {
    val rows = chips.chunked(3)
    Column {
        rows.forEach { rowChips ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                rowChips.forEach { chipLabel ->
                    DefaultChip(
                        label = chipLabel,
                        isSelected = selectedChips.contains(chipLabel),
                        onClick = { onChipClick(chipLabel) }
                    )
                }
            }
        }
    }
}
//this is the UI for creating each chip in the filter screen
@Composable
fun DefaultChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.DarkGray else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) Color.Yellow else MaterialTheme.colorScheme.onSurface
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = textColor
        )
    )
}
//this just ensures that on each row there are only 4 chips
@Composable
fun ChipsLayout(chips: List<String>, onDeleteChip: (Int) -> Unit) {
    val rows = chips.chunked(size = 4)
    Column {
        rows.forEachIndexed { rowIndex, rowChips ->
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowChips.forEachIndexed { columnIndex, chipLabel ->
                    ChipWithDelete(
                        label = chipLabel,
                        onDelete = { onDeleteChip(rowIndex * 4 + columnIndex) }
                    )
                }
            }
        }
    }
}
//if the user clicks on the "x", the user can delete the selected chip
@Composable
fun ChipWithDelete(label: String, onDelete: () -> Unit) {
    val backgroundColor = Color.DarkGray
    val textColor = Color.Yellow

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable { }
            .padding(8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
            modifier = Modifier.padding(end = 4.dp)
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Delete",
            modifier = Modifier
                .size(16.dp)
                .clickable { onDelete() },
            tint = Color.White
        )
    }
}
//this shows up on the top of the appscreen
@Composable
fun FilterTopAppBar(onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFAA33))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Filter",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}
//created a function for the searchbar
@Composable
fun SearchBar(onDone: (String) -> Unit, isInputEnabled: Boolean) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    if (isInputEnabled) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                decorationBox = { innerTextField ->
                    Row(
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(Modifier.weight(1f)) {
                            if (text.text.isEmpty()) {
                                Text(
                                    "Search",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onDone(text.text)
                    text = TextFieldValue("") // Clear the text field
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}