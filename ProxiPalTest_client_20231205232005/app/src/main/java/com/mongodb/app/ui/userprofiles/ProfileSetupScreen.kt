import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.userprofiles.PreviousNextBottomAppBar
import com.mongodb.app.ui.userprofiles.UserProfileBody

//profile setup screen, used parts of kevin's userprofile components
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileSetupScaffold(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    navController: NavHostController,
    onPreviousClicked: () -> Unit,
    onNextClicked:() -> Unit,

    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TaskAppToolbar(viewModel = toolbarViewModel, navController = navController)
        },
        bottomBar = { PreviousNextBottomAppBar(
            onPreviousClicked = onPreviousClicked,
            onNextClicked =onNextClicked,
            currentPage = 1,
            totalPages = 3
        )
        },
        modifier = modifier
    ) { innerPadding ->
        Column {
            UserProfileBody(
                contentPadding = innerPadding,
                userProfileViewModel = userProfileViewModel
            )
        }
    }
}