package com.mongodb.app

import com.mongodb.app.presentation.blocking_censoring.censor
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StringCensoringTests {
    @Test
    fun censorString_exactWordWithoutSymbols(){
        // This is one of the profanity phrases in the .txt file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("ass")
        val uncensored = "ass"
        val expectedCensored = "***"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }

    @Test
    fun censorString_exactWordWithSymbols(){
        // This is one of the profanity phrases in the .csv file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("@55")
        val uncensored = "@55"
        val expectedCensored = "***"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }

    @Test
    fun censorString_exactWordsWithoutSymbols(){
        // This is one of the profanity phrases in the .txt file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("ass")
        val uncensored = "ass ass Ass ass"
        val expectedCensored = "*** *** *** ***"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }

    @Test
    fun censorString_exactWordsWithSymbols(){
        // This is one of the profanity phrases in the .csv file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("@55")
        val uncensored = "@55 @55 @55"
        val expectedCensored = "*** *** ***"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }

    @Test
    fun censorString_embeddedWordsWithoutSymbols(){
        // This is one of the profanity phrases in the .txt file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("ass")
        val uncensored = "assist password 0ass0 grass"
        val expectedCensored = "assist password ***** grass"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }

    @Test
    fun censorString_embeddedWordsWithSymbols(){
        // This is one of the profanity phrases in the .csv file
        // ... that would be retrieved in FetchCensoredTextThread.kt
        val profanityList: MutableList<String> = mutableListOf("@55")
        val uncensored = "@550 0@550 a@55a .@55. 0@55"
        val expectedCensored = "**** ***** a@55a ***** ****"
        val actualCensored = uncensored.censor(profanityList)
        assertEquals(expectedCensored, actualCensored)
    }
}