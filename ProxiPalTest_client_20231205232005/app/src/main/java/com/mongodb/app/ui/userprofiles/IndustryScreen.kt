package com.mongodb.app.ui.userprofiles

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme

//list of resources for industry
val industryGridItems = listOf(
    GridItemData(R.drawable.culture, R.string.arts_entertainment),
    GridItemData(R.drawable.food, R.string.food_drink),
    GridItemData(R.drawable.education, R.string.education),
    GridItemData(R.drawable.finance, R.string.finance),
    GridItemData(R.drawable.healthcare, R.string.healthcare),
    GridItemData(R.drawable.sport, R.string.activity),
    GridItemData(R.drawable.media, R.string.media),
    GridItemData(R.drawable.technology, R.string.technology),
    GridItemData(R.drawable.retail, R.string.retail),
)

//grid of industries
@Composable
fun IndustryGrid(gridItems: List<GridItemData>,  userProfileViewModel: UserProfileViewModel, userIndustries: List<String>) {
    LazyVerticalGrid(
        modifier= Modifier.fillMaxSize().padding(5.dp),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(gridItems.size) { index ->
            IndustryGridItem(gridItems[index], userProfileViewModel , userIndustries)
        }
    }
}
//each grid item of industry
@Composable
fun IndustryGridItem(gridItemData: GridItemData, userProfileViewModel: UserProfileViewModel, userIndustries: List<String>) {
    val itemText = GridItemText(gridItemData.text)
    var isSelected by remember { mutableStateOf(userIndustries.contains(itemText)) }
    Log.d("isSelected", "isSelected:$isSelected")


    // button that displays the image and name of corresponding item , button changes color when clicked to show selected
    Button(
        onClick = {isSelected = !isSelected ;userProfileViewModel.toggleIndustry(itemText);


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
//scaffold for industry screen
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun IndustryScreen(userProfileViewModel: UserProfileViewModel, onPreviousClicked: () -> Unit, onNextClicked: () -> Unit) {
    val userIndustries = userProfileViewModel.userProfileIndustries.toMutableList()
    Log.d("userindustry", "userindustry:$userIndustries")
    MyApplicationTheme {
        Scaffold(
            topBar ={ ProfileTopAppBar(R.string.industry_heading,R.string.interest_subheading)},
            bottomBar = { PreviousNextBottomAppBar(
                onPreviousClicked = onPreviousClicked,
                onNextClicked = onNextClicked,
                currentPage = 3,
                totalPages = 3
            )}
        ){
                innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                IndustryGrid(industryGridItems,  userProfileViewModel, userIndustries = userIndustries)
            }
        }
    }
}