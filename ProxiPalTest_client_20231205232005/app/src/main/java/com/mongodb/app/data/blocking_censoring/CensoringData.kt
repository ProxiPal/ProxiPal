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
    }
}