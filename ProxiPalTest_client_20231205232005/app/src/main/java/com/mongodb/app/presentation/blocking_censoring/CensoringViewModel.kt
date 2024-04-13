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
import java.util.regex.Pattern


// region Extensions
/**
 * Censors a string using a list of text to hide from the original string
 */
fun String.censor(censoredTextList: MutableList<String>): String{
    val censoredChar = '*'
    val stringBuilder = StringBuilder()
    val lowercaseBuilder = StringBuilder()
    for (index in indices){
        stringBuilder.append(this[index])
        lowercaseBuilder.append(this[index].lowercaseChar())
    }

    for (keyText in censoredTextList){
        var shouldCensor = true
        // While the current string still contains the text to censor
        while (lowercaseBuilder.indexOf(keyText) != -1 && shouldCensor){
            // If the current string matches the censored text with any amount of spaces
            // ... in front and behind it, censor it
            val zeroOrMoreWhitespacesPattern = "\\s*"
            val exactTextSurroundedPattern = Pattern.compile(
                zeroOrMoreWhitespacesPattern +
                        keyText +
                zeroOrMoreWhitespacesPattern
            )
            // Check if the string matches the pattern (whiteSpace)* + keyText + (whiteSpace)*
            val exactTextSurroundedMatcher = exactTextSurroundedPattern.matcher(lowercaseBuilder)
            val doesMatchExactTextSurroundedPattern = exactTextSurroundedMatcher.matches()
            shouldCensor = doesMatchExactTextSurroundedPattern

            // Replace the offending text with censored characters
            if (shouldCensor){
                val startIndex = lowercaseBuilder.indexOf(keyText)
                val endIndex = startIndex + keyText.length - 1
                for (i in startIndex..endIndex){
                    stringBuilder.setCharAt(i, censoredChar)
                    lowercaseBuilder.setCharAt(i, censoredChar)
                }
            }
        }
    }
    Log.i(
        TAG(),
        "Censored \"${this}\" to \"${stringBuilder}\""
    )
    return stringBuilder.toString()
}
// endregion Extensions


class CensoringViewModel (
    private var repository: SyncRepository
) : ViewModel(){
    // region Variables
    private val _censoredTextList: MutableList<String> = mutableListOf()
    // endregion Variables


    // region Properties
    val censoredTextList
        get() = _censoredTextList
    // endregion Properties


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
                "CensoringViewModel: Done waiting for fetched data; Size = " +
                        "${censoredTextList.size}"
            )
            if (censoredTextList.size > 0){
                Log.i(
                    TAG(),
                    "CensoringViewModel: 1st = \"${censoredTextList[0]}\"; " +
                            "Last = \"${censoredTextList[censoredTextList.size - 1]}\""
                )
            }
        }
    }

    fun testTextCensoring(){
        Log.i(
            TAG(),
            "Start -> End"
        )
        val toCensor = "What is zoophilia? I don't know"
        val censored = toCensor.censor(censoredTextList)
        Log.i(
            TAG(),
            "$toCensor -> $censored"
        )
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