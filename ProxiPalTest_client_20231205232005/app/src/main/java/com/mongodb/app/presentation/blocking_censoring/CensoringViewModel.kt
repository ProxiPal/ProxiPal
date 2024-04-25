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
import com.mongodb.app.data.blocking_censoring.IBlockingCensoringRealm
import com.mongodb.app.data.toObjectId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.regex.Pattern


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


private const val CENSORED_CHAR = '*'


// region Extensions
/**
 * Censors a string using a list of text to hide from the original string
 */
fun String.censor(profanityList: MutableList<String>): String{
    val stringBuilder = StringBuilder()
    for (index in indices){
        stringBuilder.append(this[index])
    }

    for (profanityPhrase in profanityList){
        // Profanity phrase does not fit in current string, so the string can never contain the phrase
        if (profanityPhrase.length > stringBuilder.length){
            continue
        }
        stringBuilder.censorShort(profanityPhrase)
        stringBuilder.censorLong(profanityPhrase)
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
private fun StringBuilder.censorShort(profanityPhrase: String){
    val zeroOrMoreWhitespaces = "\\s*"
    val zeroOrMoreWhitespacesPattern = Pattern.compile(
        zeroOrMoreWhitespaces + profanityPhrase + zeroOrMoreWhitespaces
    )
    // Ignore casing when doing the pattern matching
    val lowercase = this.toString().lowercase()
    val zeroOrMoreWhitespacesMatcher = zeroOrMoreWhitespacesPattern.matcher(lowercase)
    val doesMatchZeroOrMoreWhitespacesPattern = zeroOrMoreWhitespacesMatcher.matches()

    if (doesMatchZeroOrMoreWhitespacesPattern){
        val startIndex = lowercase.indexOf(profanityPhrase)
        val endIndex = startIndex + profanityPhrase.length - 1
        this.replace(startIndex, endIndex, CENSORED_CHAR)
    }
}

/**
 * Checks if a string contains some offending text followed by and following a non-alphabet char
 */
private fun StringBuilder.censorLong(profanityPhrase: String){
    if (!this.contains(profanityPhrase)){
        return
    }

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

    // Ignore casing when doing the pattern matching
    val lowercase = this.toString().lowercase()

    // Check every substring for the offending text
    var startIndex = 0
    // Key text length + 2 surrounding characters - 1 for indexing
    var endIndex = profanityPhrase.length + 1

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
 * with the specified character
 */
private fun StringBuilder.replace(startIndex: Int, endIndex: Int, characterReplacement: Char){
    // The substring is not valid with the given indexes
    if (startIndex > endIndex){
        return
    }
    for (i in startIndex..endIndex){
        // Only replace/censor non-whitespace characters
        if (this[i] != ' '){
            this.setCharAt(i, characterReplacement)
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

    /**
     * How many milliseconds to wait between attempts of reading files from URLs
     */
    private val _fileReadingDelayMs: Long = 1000
    // endregion Variables


    // region Properties
    val profanityListAll
        get() = _profanityListAll
    val isCensoringText
        get() = _isCensoringText
    // endregion Properties


    init{
        // This boolean condition is only to allow previews to work (error is an illegal thread state)
        if (shouldReadCensoredTextOnInit){
            readCensoredTextList()
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
    fun readCensoredTextList(){
        profanityListAll.clear()
        FetchCensoredTextThread.getInstance().start()
        viewModelScope.launch {
            var shouldKeepReReading = true
            val loopLimit = 10
            var loopIter = 0
            while (shouldKeepReReading && loopIter < loopLimit){
                delay(_fileReadingDelayMs)
                while (!FetchCensoredTextThread.getInstance().isDoneFetchingData.value){
                    Log.i(
                        TAG(),
                        "CensoringViewModel: Waiting for fetched data"
                    )
                    delay(_fileReadingDelayMs)
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