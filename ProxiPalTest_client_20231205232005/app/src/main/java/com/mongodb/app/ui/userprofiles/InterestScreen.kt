package com.mongodb.app.ui.userprofiles

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

import com.mongodb.app.ui.theme.MyApplicationTheme
// navigation details


// data class for item data: id of image , and name
data class GridItemData(val imageId: Int, val text: Int) // list of items, contains image, item name

val interestGridItems = listOf(
    GridItemData(R.drawable.culture, R.string.art_culture),
    GridItemData(R.drawable.food, R.string.food_drink),
    GridItemData(R.drawable.game, R.string.gaming),
    GridItemData(R.drawable.music, R.string.music),
    GridItemData(R.drawable.nature, R.string.nature),
    GridItemData(R.drawable.sport, R.string.activity),
    GridItemData(R.drawable.fashion, R.string.fashion),
    GridItemData(R.drawable.technology, R.string.technology),
    GridItemData(R.drawable.travel, R.string.travel),
)

// top bar, contains title and sub heading
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(heading:Int, subHeading:Int){
    TopAppBar(modifier = Modifier.padding(10.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = heading),
                    color = Color.Black,
                    fontSize= 30.sp)
                Text(text = stringResource(id = subHeading),
                    color = Color.Gray,
                    fontSize = 15.sp)
            }
        }
    )
}

// 3x3 grid of items from gridItems
@Composable
fun Grid(gridItems: List<GridItemData>,  userProfileViewModel: UserProfileViewModel, userInterests: List<String>) {
    LazyVerticalGrid(
        modifier= Modifier
            .fillMaxSize()
            .padding(5.dp),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(gridItems.size) { index ->
            GridItem(gridItems[index], userProfileViewModel , userInterests)
        }
    }
}

@Composable
fun GridItemText(resourceId: Int): String {
    val context = LocalContext.current
    return context.resources.getString(resourceId)
}

// item in grid
@Composable
fun GridItem(gridItemData: GridItemData, userProfileViewModel: UserProfileViewModel, userInterests: List<String>) {
    val itemText = GridItemText(gridItemData.text)
    var isSelected by remember { mutableStateOf(userInterests.contains(itemText)) }
    Log.d("isSelected", "isSelected:$isSelected")


    // button that displays the image and name of corresponding item , button changes color when clicked to show selected
    Button(
        onClick = {isSelected = !isSelected ;userProfileViewModel.toggleInterest(itemText);


                  },
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier.size(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.LightGray else Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()

        ) {
            Image(
                painter = painterResource(id = gridItemData.imageId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(150.dp)
            )
            Text(
                text = stringResource(id = gridItemData.text),
                color = if (isSelected) Color.White else Color.Black,
                textAlign = TextAlign.Center, fontSize = 12.sp
            )
        }
    }

}

// bottom bar, contains previous buttons, next button, and display of which page user is on
@Composable
fun PreviousNextBottomAppBar(
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    currentPage: Int,
    totalPages: Int,
) {
    BottomAppBar() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPreviousClicked){
                Icon(Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint =
                    if (currentPage!=1) {
                        Color(0xFFEF8524)
                    }
                    else {
                            Color.Transparent
                    })
                Text(text = stringResource(id = R.string.back),
                    color =
                    if (currentPage!=1) {
                            Color(0xFFEF8524)
                        }
                        else{
                            Color.Transparent
                        }, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            // loop to create indication of which page user is on
                repeat(totalPages) { index ->
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_circle_24),
                        contentDescription = null,
                        tint = if (index == currentPage -1) Color(0xFFEF8524) else Color.Gray,
                        modifier= Modifier.size(10.dp)
                    )
                }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onNextClicked){
                Text(text = "Next",
                    color = Color(0xFFEF8524),
                    fontSize = 20.sp)
                Icon(Icons.Default.ArrowForward,
                    contentDescription = stringResource(id = R.string.next),
                    tint = Color(0xFFEF8524))
            }
        }
    }
}

// scaffold of interest screen
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InterestScreen(userProfileViewModel: UserProfileViewModel, onPreviousClicked: () -> Unit, onNextClicked: () -> Unit) {
    val userInterests = userProfileViewModel.userProfileInterests.toMutableList()
    Log.d("userinterest", "userinterest:$userInterests")
    MyApplicationTheme {
        Scaffold(
            topBar ={ ProfileTopAppBar(R.string.interest_heading, R.string.interest_subheading)},
            bottomBar = { PreviousNextBottomAppBar(
                onPreviousClicked = onPreviousClicked,
                onNextClicked = onNextClicked,
                currentPage = 2,
                totalPages = 3
            )}
        ){
            innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                Grid(interestGridItems,  userProfileViewModel, userInterests = userInterests)
            }
        }
    }
}