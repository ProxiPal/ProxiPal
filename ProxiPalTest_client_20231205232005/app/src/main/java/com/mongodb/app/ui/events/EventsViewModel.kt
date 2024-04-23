package com.mongodb.app.ui.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.presentation.tasks.AddItemEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
}