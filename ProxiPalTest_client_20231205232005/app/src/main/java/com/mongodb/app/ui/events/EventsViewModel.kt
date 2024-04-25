package com.mongodb.app.ui.events

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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


class EventsViewModel(
    private val repository: SyncRepository
) : ViewModel() {


    fun addEvent(name: String, description: String, date: String, time: String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addEvent(
                    eventName = name,
                    eventDescription = description,
                    eventDate = date,
                    eventTime = time,
                    eventLocation = location
                )
            }.onSuccess { Log.d("events", "working") }
                .onFailure { Log.d("events", "not working") }
        }
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
                    return EventsViewModel(repository) as T
                }
            }
        }
    }
}