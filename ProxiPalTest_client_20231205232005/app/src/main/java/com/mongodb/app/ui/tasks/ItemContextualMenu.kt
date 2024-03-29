package com.mongodb.app.ui.tasks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.data.MockRepository
import com.mongodb.app.domain.Item
import com.mongodb.app.presentation.tasks.ItemContextualMenuViewModel
import com.mongodb.app.presentation.tasks.TaskViewModel
import com.mongodb.app.ui.components.ContextualMenu
import com.mongodb.app.ui.theme.MyApplicationTheme

@Composable
fun ItemContextualMenu(
    viewModel: ItemContextualMenuViewModel,
    task: Item
) {
    ContextualMenu(
        contextualMenuViewModel = viewModel,
        onClickContextualMenuViewModelOpen = { viewModel.open() },
        onClickContextualMenuViewModelClose = { viewModel.close() },
        onClickContextualMenuViewModelDeleteEntry = { viewModel.deleteTask(task) }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun ItemContextualMenuPreview() {
    MyApplicationTheme {
        MyApplicationTheme {
            val repository = MockRepository()
            ItemContextualMenu(
                ItemContextualMenuViewModel(
                    repository,
                    TaskViewModel(repository, mutableStateListOf())
                ),
                Item()
            )
        }
    }
}
