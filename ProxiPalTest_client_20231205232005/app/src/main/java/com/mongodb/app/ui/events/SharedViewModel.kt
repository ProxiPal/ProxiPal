package com.mongodb.app.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mongodb.app.domain.Event

class SharedViewModel: ViewModel(){
    var event by mutableStateOf<Event?>(null)
        private set

    fun addEvent(newEvent: Event){
        event = newEvent
    }
}