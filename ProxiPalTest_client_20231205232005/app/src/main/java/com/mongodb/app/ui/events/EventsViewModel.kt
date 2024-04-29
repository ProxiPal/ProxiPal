package com.mongodb.app.ui.events

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SubscriptionType
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.Event
import com.mongodb.app.domain.Item
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.tasks.AddItemEvent
import com.mongodb.app.presentation.tasks.AddItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId


class EventsViewModel(
    private val repository: SyncRepository
) : ViewModel() {

    private val _myEventList = MutableStateFlow<List<Event>>(emptyList())
    val myEventList: StateFlow<List<Event>> = _myEventList.asStateFlow()

    private val _otherEventList = MutableStateFlow<List<Event>>(emptyList())
    val otherEventList: StateFlow<List<Event>> = _otherEventList.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMyEventList().collect { resultChange ->
                _myEventList.value = resultChange.list
            }
        }
        viewModelScope.launch {
            repository.getOtherEventList().collect { resultChange ->
                _otherEventList.value = resultChange.list
            }
        }
    }
    fun addEvent(name: String, description: String, date: String, time: String, duration:String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addEvent(
                    eventName = name,
                    eventDescription = description,
                    eventDate = date,
                    eventTime = time,
                    eventDuration = duration,
                    eventLocation = location
                )
            }.onSuccess { Log.d("events", "working") }
                .onFailure { Log.d("events", "not working") }
        }
    }

    suspend fun getEventById(eventId: String?): Flow<Event?> {
        return if (eventId != null) {
            repository.getEventById(eventId).map { it.list.firstOrNull() }
        } else {
            flowOf(null)
        }
    }

    fun isTaskMine(task: Item): Boolean = repository.isTaskMine(task)
    fun isCurrentUserEventOwner(event: Event): Boolean  = repository.isEventOwner(event)





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