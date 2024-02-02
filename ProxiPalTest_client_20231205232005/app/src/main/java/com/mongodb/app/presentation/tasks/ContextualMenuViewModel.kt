package com.mongodb.app.presentation.tasks
// TODO Might need to move this class to a new package to avoid confusion being in the "tasks" package

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mongodb.app.data.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class ContextualMenuViewModel constructor(
    private val repository: SyncRepository
) : ViewModel() {

    private val _visible: MutableState<Boolean> = mutableStateOf(false)
    val visible: State<Boolean>
        get() = _visible

    fun open() {
        _visible.value = true
    }

    fun close() {
        _visible.value = false
    }
}
