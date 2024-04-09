package com.mongodb.app.presentation.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CensoringViewModel (
    private var repository: SyncRepository
) : ViewModel(){
    // region Variables
    private val _censoredTextList: MutableList<String> = mutableListOf("")
    // endregion Variables


    // region Functions
    fun updateRepositories(newRepository: SyncRepository){
        repository = newRepository
    }

    fun readCensoredTextList(){
        _censoredTextList.clear()
        val fetchCensoredTextThread = FetchCensoredTextThread()
        fetchCensoredTextThread.start()
        viewModelScope.launch {
            while (!fetchCensoredTextThread.isDoneFetchingData.value){
                Log.i(
                    TAG(),
                    "CensoringViewModel: Waiting for fetched data"
                )
                delay(1000)
            }
            for (datum in fetchCensoredTextThread.data){
                _censoredTextList.add(datum)
            }
            // Do not use .addAll(), it adds all elements as a single element to the end
//            _censoredTextList.addAll(fetchCensoredTextThread.data)
            Log.i(
                TAG(),
                "CensoringViewModel: Done waiting for fetched data; " +
                        "It's now = \"${_censoredTextList[0]} ... " +
                        "${_censoredTextList[_censoredTextList.size - 1]}\" with size = " +
                        "${_censoredTextList.size}"
            )
        }
    }
    // endregion Functions


    // Allows instantiating an instance of itself
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
                    // Remember to change the cast to the class name this code is in
                    return CensoringViewModel (repository) as T
                }
            }
        }
    }
}