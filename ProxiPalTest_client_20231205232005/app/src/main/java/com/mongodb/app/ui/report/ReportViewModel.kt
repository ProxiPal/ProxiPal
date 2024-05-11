package com.mongodb.app.ui.report

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SubscriptionType
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.presentation.tasks.AddItemEvent
import com.mongodb.app.presentation.tasks.AddItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

//Vichet Chim
class ReportViewModel(
    private val repository: SyncRepository
) : ViewModel() {

    // function to add a report to database
    fun addReport(userReported: String, reasons: List<String>, comment: String) {
        CoroutineScope(Dispatchers.IO).launch(){
            runCatching {
                repository.addReport(reportedUser = userReported, reasonsList = reasons,comment =comment)

            }.onSuccess { Log.d("test", "working") }
                .onFailure { Log.d("test", "not working") }}
    }
    companion object {
        fun factory(
            repository: SyncRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return ReportViewModel(repository) as T
                }
            }
        }
    }
}