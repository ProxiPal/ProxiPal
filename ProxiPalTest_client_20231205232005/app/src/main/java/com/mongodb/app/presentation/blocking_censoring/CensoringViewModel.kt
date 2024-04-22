package com.mongodb.app.presentation.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.blocking_censoring.IBlockingCensoringRealm
import com.mongodb.app.data.toObjectId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.regex.Pattern


private const val CENSORED_CHAR = '*'


// region Extensions
/**
 * Censors a string using a list of text to hide from the original string
 */
fun String.censor(censoredTextList: MutableList<String>): String{
    val stringBuilder = StringBuilder()
    for (index in indices){
        stringBuilder.append(this[index])
    }

    for (keyText in censoredTextList){
        stringBuilder.censorShort(keyText)
        stringBuilder.censorLong(keyText)
    }
    Log.i(
        TAG(),
        "Censored \"${this}\" to \"${stringBuilder}\""
    )
    return stringBuilder.toString()
}

/**
 * Checks if a string is equal to some offending text followed by and following any amount of spaces
 */
private fun StringBuilder.censorShort(textToCensor: String){
    val zeroOrMoreWhitespaces = "\\s*"
    val zeroOrMoreWhitespacesPattern = Pattern.compile(
        zeroOrMoreWhitespaces + textToCensor + zeroOrMoreWhitespaces
    )
    // Ignore casing when doing the pattern matching
    val lowercase = this.toString().lowercase()
    val zeroOrMoreWhitespacesMatcher = zeroOrMoreWhitespacesPattern.matcher(lowercase)
    val doesMatchZeroOrMoreWhitespacesPattern = zeroOrMoreWhitespacesMatcher.matches()

    if (doesMatchZeroOrMoreWhitespacesPattern){
        val startIndex = lowercase.indexOf(textToCensor)
        val endIndex = startIndex + textToCensor.length - 1
        this.replace(startIndex, endIndex, CENSORED_CHAR)
    }
}

/**
 * Checks if a string contains some offending text followed by and following a non-alphabet char
 */
private fun StringBuilder.censorLong(textToCensor: String){
    if (!this.contains(textToCensor)){
        return
    }

    val nonAlphabetChar = "[^a-z]"
    val nonAlphabetCharPattern = Pattern.compile(
        nonAlphabetChar + textToCensor + nonAlphabetChar
    )
    val nonAlphabetCharFirstPattern = Pattern.compile(
        textToCensor + nonAlphabetChar
    )
    val nonAlphabetCharLastPattern = Pattern.compile(
        nonAlphabetChar + textToCensor
    )

    // Ignore casing when doing the pattern matching
    var lowercase = this.toString().lowercase()

    // Check every substring for the offending text
    var startIndex = 0
    // Key text length + 2 surrounding characters - 1 for indexing
    var endIndex = textToCensor.length + 1

    val loopAmount = lowercase.length - endIndex
    for (i in 0..<loopAmount){
        val substring = lowercase.substring(startIndex, endIndex + 1)
        val nonAlphabetCharMatcher = nonAlphabetCharPattern.matcher(substring)
        val doesMatchNonAlphabetCharPattern = nonAlphabetCharMatcher.matches()

//        Log.i(
//            TAG(),
//            "\"$substring\" =?= \"${nonAlphabetCharPattern.pattern()}\""
//        )
        if (doesMatchNonAlphabetCharPattern){
            this.replace(startIndex, endIndex, CENSORED_CHAR)
        }


        val substringFirst = lowercase.substring(startIndex, endIndex)
        val substringLast = lowercase.substring(startIndex + 1, endIndex + 1)
//        Log.i(
//            TAG(),
//            "\"$substringFirst\" ?= \"${nonAlphabetCharFirstPattern.pattern()}\""
//        )
//        Log.i(
//            TAG(),
//            "\"$substringLast\" =? \"${nonAlphabetCharLastPattern.pattern()}\""
//        )
        // For 1st iteration, instead check (keyText) + (nonAlphabetChar)
        if (i == 0 && nonAlphabetCharFirstPattern.matcher(substringFirst).matches()){
            this.replace(startIndex, endIndex - 1, CENSORED_CHAR)
        }
        // For last iteration, instead check (nonAlphabetChar) + (keyText)
        if (i == loopAmount - 1 && nonAlphabetCharLastPattern.matcher(substringLast).matches()){
            this.replace(startIndex + 1, endIndex, CENSORED_CHAR)
        }

        startIndex += 1
        endIndex += 1
    }
}

/**
 * Replaces the characters at indexes [startIndex] inclusive to [endIndex] inclusive
 * with the specified characters
 */
private fun StringBuilder.replace(startIndex: Int, endIndex: Int, replacement: Char){
    // The substring is not valid with the given indexes
    if (startIndex > endIndex){
        return
    }
    for (i in startIndex..endIndex){
        // Only replace/censor non-whitespace characters
        if (this[i] != ' '){
            this.setCharAt(i, replacement)
        }
    }
}
// endregion Extensions


class CensoringViewModel (
    private var repository: SyncRepository,
    private var blockingCensoringRealm: IBlockingCensoringRealm,
    private val shouldReadCensoredTextOnInit: Boolean
) : ViewModel(){
    // region Variables
    private val _censoredTextList: MutableList<String> = mutableListOf()
    private val _isCensoringText = mutableStateOf(false)
    // endregion Variables


    // region Properties
    val censoredTextList
        get() = _censoredTextList
    val isCensoringText
        get() = _isCensoringText
    // endregion Properties


    init{
        // This boolean condition is only to allow previews to work
        // ... (Error is due to an illegal thread state)
        if (shouldReadCensoredTextOnInit){
            readCensoredTextList()
        }
    }


    // region Functions
    fun updateRepositories(
        newRepository: SyncRepository,
        newBlockingCensoringRealm: IBlockingCensoringRealm
    ){
        repository = newRepository
        blockingCensoringRealm = newBlockingCensoringRealm
    }

    fun readCensoredTextList(){
        censoredTextList.clear()
        FetchCensoredTextThread.getInstance().start()
        viewModelScope.launch {
            var shouldKeepReReading = true
            val loopLimit = 10
            var loopIter = 0
            while (shouldKeepReReading && loopIter < loopLimit){
                while (!FetchCensoredTextThread.getInstance().isDoneFetchingData.value){
                    Log.i(
                        TAG(),
                        "CensoringViewModel: Waiting for fetched data"
                    )
                    delay(1000)
                }
                // List of censored words has been read successfully
                if (FetchCensoredTextThread.getInstance().data.size > 0){
                    shouldKeepReReading = false
                }
                else{
                    Log.i(
                        TAG(),
                        "CensoringViewModel: Unsuccessful, trying again"
                    )
                }
                loopIter++
            }
            // Do not use .addAll(), it adds all elements as a single element to the end
            for (datum in FetchCensoredTextThread.getInstance().data){
                censoredTextList.add(datum)
            }
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
            "CensoringViewModel: Start of text censoring tests"
        )
        val testList: List<String> = listOf(
            "ass",
            " password ",
            " assassin ",
            " grass ",
            " Ass ",
            "ass ass bass asss basss .ass ass. .ass. 0ass ass0 0ass0 assass aassss ass",
            "ass. password assassin grass Ass"
        )
        for (test in testList) {
            test.censor(censoredTextList)
        }
        Log.i(
            TAG(),
            "CensoringViewModel: End of text censoring tests"
        )
    }

    fun updateShouldCensorTextState(){
        viewModelScope.launch {
            readShouldCensorTextState()
        }
    }

    private suspend fun readShouldCensorTextState(){
        repository.readUserProfile(repository.getCurrentUserId())
            .first{
                if (it.list.size > 0){
                    isCensoringText.value = it.list[0].hasTextCensoringEnabled
                }
                true
            }
    }

    fun toggleShouldCensorText(){
        viewModelScope.launch {
            blockingCensoringRealm.updateTextCensoringState(
                repository.getCurrentUserId().toObjectId()
            )
            readShouldCensorTextState()
        }
    }
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
            repository: SyncRepository,
            blockingCensoringRealm: IBlockingCensoringRealm,
            shouldReadCensoredTextOnInit: Boolean,
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
                    return CensoringViewModel (repository, blockingCensoringRealm, shouldReadCensoredTextOnInit) as T
                }
            }
        }
    }
}