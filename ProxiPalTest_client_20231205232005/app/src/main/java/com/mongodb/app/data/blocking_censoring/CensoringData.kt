package com.mongodb.app.data.blocking_censoring


/**
 * Class for containing test data/variables related to text censoring
 */
class CensoringData {
    // Singleton instance
    companion object{
        val testListStringsToCensor: List<String> = listOf(
            "ass",
            " password ",
            " assassin ",
            " grass ",
            " Ass ",
            "@55",
            " x@55x ",
            " @55x ",
            " x@55 ",
            " @55 ",
            "ass ass bass asss basss .ass ass. .ass. 0ass ass0 0ass0 assass aassss @55 ass",
            "ass. password assassin grass @55 Ass"
        )

        /**
         * Profanity list #1, listed in a .txt file
         */
        val urlTxt = "https://raw.githubusercontent.com/dsojevic/profanity-list/main/en.txt"

        /**
         * Profanity list #2, listed in a .csv file
         */
        val urlCsv = "https://raw.githubusercontent.com/surge-ai/profanity/main/profanity_en.csv"

        val httpUrlConnectionTimeout = 60000
        val delimitersCsv = ','

        /**
         * How many milliseconds to wait between attempts of reading files from URLs
         */
        val fileReadingDelayMs: Long = 1000
    }
}