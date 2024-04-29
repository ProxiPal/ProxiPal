package com.mongodb.app.ui.events

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    private val _eventName: MutableState<String> = mutableStateOf("")
    private val _eventDescription: MutableState<String> = mutableStateOf("")
    private val _eventTime: MutableState<String> = mutableStateOf("")
    private val _eventDate: MutableState<String> = mutableStateOf("")
    private val _eventLocation: MutableState<String> = mutableStateOf("")
    private val _eventDuration: MutableState<String> = mutableStateOf("")


    val eventName: State<String>
        get() = _eventName

    val eventDescription: State<String>
        get() = _eventDescription

    val eventTime: State<String>
        get() = _eventTime

    val eventDate: State<String>
        get() = _eventDate

    val eventLocation: State<String>
        get() = _eventLocation

    val eventDuration: State<String>
        get() = _eventDuration

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
    fun updateEvent(eventId:String, name: String, description: String, date: String, time: String, duration:String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.updateEvent(
                    eventId = eventId,
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

    fun joinEvent(eventId:String){
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.joinEvent(
                    eventId = eventId
                )
            }.onSuccess { Log.d("events", "working") }
                .onFailure { Log.d("events", "not working") }
        }
    }

    fun leaveEvent(eventId:String){
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.leaveEvent(
                    eventId = eventId
                )
            }.onSuccess { Log.d("events", "working") }
                .onFailure { Log.d("events", "not working") }
        }
    }

    fun isCurrentUserEventAttendee(event: Event) : Boolean = repository.isEventAttendee(event)


    suspend fun getEventById(eventId: String?): Flow<Event?> {
        return if (eventId != null) {
            repository.getEventById(eventId).map { it.list.firstOrNull() }
        } else {
            flowOf(null)
        }
    }

    fun getEventAttendeesList(eventId: String?): List<UserProfile> {
        var attendeesList = emptyList<UserProfile>()
        viewModelScope.launch{
            runCatching {
                if (eventId != null) {
                    attendeesList = repository.getEventAttendees(eventId)
                }
            }
        }
        return attendeesList
    }
    fun isCurrentUserEventOwner(event: Event): Boolean  = repository.isEventOwner(event)

    fun addAnnouncement
                (eventId:String?, announcement:String) {
        CoroutineScope(Dispatchers.IO).launch{
            runCatching {
                if (eventId != null) {
                    repository.addAnnouncement(
                        eventId=eventId,
                        newAnnouncement = announcement
                    )
                }
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