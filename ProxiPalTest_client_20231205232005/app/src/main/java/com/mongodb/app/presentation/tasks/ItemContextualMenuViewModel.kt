package com.mongodb.app.presentation.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ItemContextualMenuViewModel constructor(
    private val repository: SyncRepository,
    private val taskViewModel: TaskViewModel
) : ContextualMenuViewModel(repository = repository) {

    fun deleteTask(task: Item) {
        CoroutineScope(Dispatchers.IO).launch {
            if (repository.isTaskMine(task)) {
                runCatching {
                    repository.deleteTask(task)
                }
            } else {
                taskViewModel.showPermissionsMessage()
            }
        }
        close()
    }
}
