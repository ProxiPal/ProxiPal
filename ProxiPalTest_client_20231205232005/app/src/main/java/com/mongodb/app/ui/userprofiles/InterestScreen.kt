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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

import com.mongodb.app.ui.theme.MyApplicationTheme
// navigation details


// data class for item data: id of image , and name
data class GridItemData(
    val imageId: Int,
    val text: String
)


// list of items, contains image, item name
val gridItems = listOf(
    GridItemData(R.drawable.culture, "Arts & Culture"),
    GridItemData(R.drawable.food, "Food & Drinks"),
    GridItemData(R.drawable.game, "Gaming"),
    GridItemData(R.drawable.music, "Music"),
    GridItemData(R.drawable.nature, "Nature"),
    GridItemData(R.drawable.sport, "Activity"),
    GridItemData(R.drawable.fashion, "Fashion"),
    GridItemData(R.drawable.technology, "Technology"),
    GridItemData(R.drawable.travel, "Travel"),

)



// top bar, contains title and sub heading
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(){
    TopAppBar(modifier = Modifier.padding(10.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Choose your Interests",
                    color = Color.Black,
                    fontSize= 30.sp)
                Text(text = "You can always change this later.",
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
        modifier= Modifier.fillMaxSize().padding(5.dp),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(gridItems.size) { index ->
            GridItem(gridItems[index], userProfileViewModel , userInterests)
        }
    }
}

// item in grid
@Composable
fun GridItem(gridItemData: GridItemData, userProfileViewModel: UserProfileViewModel, userInterests: List<String>) {
    var isSelected by remember { mutableStateOf(userInterests.contains(gridItemData.text)) }
    Log.d("isSelected", "isSelected:$isSelected")


    // button that displays the image and name of corresponding item , button changes color when clicked to show selected
    Button(
        onClick = {isSelected = !isSelected ;userProfileViewModel.toggleInterest(gridItemData.text);


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
                text = gridItemData.text,
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
                    tint = Color(0xFFEF8524))
                Text(text = "Back",
                    color = Color(0xFFEF8524), fontSize = 20.sp)
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
                    contentDescription = "Next",
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
            topBar ={ ProfileTopAppBar()},
            bottomBar = { PreviousNextBottomAppBar(
                onPreviousClicked = onPreviousClicked,
                onNextClicked = onPreviousClicked,
                currentPage = 2,
                totalPages = 3
            )}
        ){
            innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                Grid(gridItems,  userProfileViewModel, userInterests = userInterests)
            }
        }
    }
}