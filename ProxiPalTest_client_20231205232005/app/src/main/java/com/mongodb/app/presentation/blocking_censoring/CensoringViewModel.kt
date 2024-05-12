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
import com.mongodb.app.data.blocking_censoring.CensoringData
import com.mongodb.app.data.blocking_censoring.CensoringData.Companion.fileReadingDelayMs
import com.mongodb.app.data.blocking_censoring.IBlockingCensoringRealm
import com.mongodb.app.data.toObjectId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.util.regex.Pattern


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


// region Extensions
val Char.Companion.empty: Char
    get() { return ' ' }
val Char.Companion.censored: Char
    get() { return '*' }

/**
 * Censors a string using a list of text to hide from the original string
 */
@VisibleForTesting
fun String.censor(profanityList: MutableList<String>): String{
    val stringBuilder = StringBuilder()
    for (index in indices){
        stringBuilder.append(this[index])
    }

    for (profanityPhrase in profanityList){
        // Do some precondition checks to help reduce the time it takes to censor text

        // Profanity phrase does not fit in current string, so the string can never contain the phrase
        if (profanityPhrase.length > stringBuilder.length){
            continue
        }
        // Current string does not contain the profanity phrase
        if (!stringBuilder.contains(profanityPhrase)){
            continue
        }
        stringBuilder.censorShort(profanityPhrase)
        stringBuilder.censorLong(profanityPhrase)
    }
    return stringBuilder.toString()
}

/**
 * Checks if a string is equal to some offending text followed by and following any amount of spaces
 */
private fun StringBuilder.censorShort(profanityPhrase: String){
    // Ignore casing when doing pattern matching
    val lowercase = this.toString().lowercase()

    val zeroOrMoreWhitespaces = "\\s*"
    val zeroOrMoreWhitespacesPattern = Pattern.compile(
        zeroOrMoreWhitespaces + profanityPhrase + zeroOrMoreWhitespaces
    )
    val zeroOrMoreWhitespacesMatcher = zeroOrMoreWhitespacesPattern.matcher(lowercase)

    // String matches pattern "0OrMoreSpaces + profanityPhrase + 0OrMoreSpaces"
    if (zeroOrMoreWhitespacesMatcher.matches()){
        val startIndex = lowercase.indexOf(profanityPhrase)
        val endIndex = startIndex + profanityPhrase.length - 1
        this.censorReplace(startIndex, endIndex)
    }
}

/**
 * Checks if a string contains some offending text followed by and following a non-alphabet char
 */
private fun StringBuilder.censorLong(profanityPhrase: String){
    // Ignore casing when doing pattern matching
    val lowercase = this.toString().lowercase()

    val nonAlphabetChar = "[^a-z]"
    val nonAlphabetCharPattern = Pattern.compile(
        nonAlphabetChar + profanityPhrase + nonAlphabetChar
    )
    val nonAlphabetCharFirstPattern = Pattern.compile(
        profanityPhrase + nonAlphabetChar
    )
    val nonAlphabetCharLastPattern = Pattern.compile(
        nonAlphabetChar + profanityPhrase
    )

    // Check every substring for the offending text
    // Index of the first character in the substring to check
    var startIndex = 0
    // Index of the last character in the substring to check
    // Profanity phrase length + 1 leading character + 1 trailing character - 1 for indexing
    var endIndex = profanityPhrase.length + 1

    val loopAmount = lowercase.length - endIndex
    for (i in 0..<loopAmount){
        when(i){
            // Start of string, check for pattern "profanityPhrase + nonAlphabetChar"
            0 -> {
                val substringFirst = lowercase.substring(startIndex, endIndex)
                if (nonAlphabetCharFirstPattern.matcher(substringFirst).matches()){
                    this.censorReplace(startIndex, endIndex - 1)
                }
            }
            // End of string, check for pattern "nonAlphabetChar + profanityPhrase"
            loopAmount - 1 -> {
                val substringLast = lowercase.substring(startIndex + 1, endIndex + 1)
                if (nonAlphabetCharLastPattern.matcher(substringLast).matches()){
                    this.censorReplace(startIndex + 1, endIndex)
                }
            }
            // Middle of string, check for pattern "nonAlphabetChar + profanityPhrase + nonAlphabetChar"
            else -> {
                val substringMiddle = lowercase.substring(startIndex, endIndex + 1)
                if (nonAlphabetCharPattern.matcher(substringMiddle).matches()){
                    this.censorReplace(startIndex, endIndex)
                }
            }
        }

        startIndex += 1
        endIndex += 1
    }
}

/**
 * Replaces the characters at indexes [startIndex] inclusive to [endIndex] inclusive
 * with the censored character
 */
private fun StringBuilder.censorReplace(startIndex: Int, endIndex: Int){
    // The substring is not valid with the given indexes
    if (startIndex > endIndex){
        return
    }
    for (i in startIndex..endIndex){
        // Only replace/censor non-whitespace characters
        if (this[i] != Char.empty){
            this.setCharAt(i, Char.censored)
        }
    }
}
// endregion Extensions


class CensoringViewModel (
    private var repository: SyncRepository,
    private var blockingCensoringRealm: IBlockingCensoringRealm,
    shouldReadCensoredTextOnInit: Boolean
) : ViewModel(){
    // region Variables
    private val _profanityListAll: MutableList<String> = mutableListOf()
    private val _isCensoringText = mutableStateOf(false)
    // endregion Variables


    // region Properties
    val profanityListAll
        get() = _profanityListAll
    val isCensoringText
        get() = _isCensoringText
    // endregion Properties


    init{
        // This boolean condition is only to allow previews to work (error is an illegal thread state)
        // Only read if the current list of profanity has not already been read and loaded in
        // This fixes Thread related errors when logging out then logging back in
        if (shouldReadCensoredTextOnInit && profanityListAll.size == 0){
            try{
                readCensoredTextList()
            }
            catch (exception: IllegalThreadStateException){
                Log.e(
                    "CensoringViewModel",
                    "There was an error while reading profanity lists from GitHub repositories"
                )
            }
        }
        else{
            Log.i(
                TAG(),
                "Current profanity size is > 0; Skipping profanity reading"
            )
        }
    }


    // region Functions
    /**
     * Updates the necessary variables during a recomposition (eg: screen orientation change)
     */
    fun updateRepositories(
        newRepository: SyncRepository,
        newBlockingCensoringRealm: IBlockingCensoringRealm
    ){
        repository = newRepository
        blockingCensoringRealm = newBlockingCensoringRealm
    }

    /**
     * Attempts to read a list of keyphrases to censor messages in the messages screen
     */
    private fun readCensoredTextList(){
        viewModelScope.launch {
            FetchCensoredTextThread.getInstance().start()
            profanityListAll.clear()
            var shouldKeepReReading = true
            val loopLimit = 10
            var loopIter = 0
            while (shouldKeepReReading && loopIter < loopLimit){
                delay(fileReadingDelayMs)
                while (!FetchCensoredTextThread.getInstance().isDoneFetchingData.value){
                    Log.i(
                        TAG(),
                        "CensoringViewModel: Waiting for fetched data"
                    )
                    delay(fileReadingDelayMs)
                }
                // Lists of profanity from .txt and .csv have been read successfully
                if (FetchCensoredTextThread.getInstance().dataTxt.size > 0
                    && FetchCensoredTextThread.getInstance().dataCsv.size > 0){
                    shouldKeepReReading = false
                }
                else{
                    Log.i(
                        TAG(),
                        "CensoringViewModel: Unsuccessful reading, trying again"
                    )
                }
                loopIter++
            }
            // Do not use .addAll(), it adds all elements as a single element to the end
            for (datum in FetchCensoredTextThread.getInstance().dataTxt){
                profanityListAll.add(datum)
            }
            for (datum in FetchCensoredTextThread.getInstance().dataCsv){
                profanityListAll.add(datum)
            }
            val sizeTxt = FetchCensoredTextThread.getInstance().dataTxt.size
            val sizeCsv = FetchCensoredTextThread.getInstance().dataCsv.size
            Log.i(
                TAG(),
                "CensoringViewModel: Done waiting for fetched data; " +
                        ".txt size = ${sizeTxt}; " +
                        ".csv size = ${sizeCsv}; " +
                        "all size = ${profanityListAll.size}"
            )
            if (profanityListAll.size > FetchCensoredTextThread.getInstance().dataTxt.size){
                Log.i(
                    TAG(),
                    "CensoringViewModel: Start of .txt = \"${profanityListAll[0]}\"; " +
                            "End of .txt = \"${profanityListAll[sizeTxt - 1]}\"; " +
                            "Start of .csv = \"${profanityListAll[sizeTxt]}\"; " +
                            "End of .csv = \"${profanityListAll[sizeTxt + sizeCsv - 1]}\""
                )
            }
//            testTextCensoring()
            FetchCensoredTextThread.endThread()
        }
    }

    /**
     * Used for testing the string censoring algorithm on various test strings
     */
    @Deprecated(
        message = "For testing purposes only; Do not use in the final product"
    )
    fun testTextCensoring(){
        Log.i(
            TAG(),
            "CensoringViewModel: Start of text censoring tests"
        )
        for (test in CensoringData.testListStringsToCensor) {
            test.censor(profanityListAll)
        }
        Log.i(
            TAG(),
            "CensoringViewModel: End of text censoring tests"
        )
    }

    /**
     * Updates a local variable to be the same as that in the database for whether a user wants messages censored
     */
    fun updateShouldCensorTextState(){
        viewModelScope.launch {
            readShouldCensorTextState()
        }
    }

    /**
     * Reads the value for whether a user wants to have messages censored or not
     */
    private suspend fun readShouldCensorTextState(){
        repository.readUserProfile(repository.getCurrentUserId())
            .first{
                if (it.list.size > 0){
                    isCensoringText.value = it.list[0].hasTextCensoringEnabled
                }
                true
            }
    }

    /**
     * Updates a user's preferences of if they want to censor messages
     */
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