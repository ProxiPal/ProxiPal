package com.mongodb.app.presentation.blocking_censoring

import android.app.Activity
import android.util.Log
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


@Deprecated(
    message = "Reading file from weblink might be better than reading from .csv file in project" +
            " (does not seem to work anyway)"
)
class CSVFileReader : Activity(){
    // region Variables
    private val _csvData: MutableList<String> = mutableListOf()
    private val _censoredTextListCsvName = "profanity_en.csv"
    // endregion Variables


    // region Functions
    fun readCsvFile(){
        try{
            val path: Path = Paths.get(_censoredTextListCsvName)
            val file = File(_censoredTextListCsvName)
            Log.i(
                "TAG()",
                "CSVFileReader: Path = \"$path\"; Full = \"${file.absolutePath}\""
            )
            val content = file.readText()
            _csvData.add(content)
            Log.i(
                "TAG()",
                "CSVFileReader: Data = \"${_csvData}\""
            )
            return


//            val inputStream: InputStream = applicationContext.assets.open(_censoredTextListCsvName)
//            val inputStreamReader: InputStreamReader = InputStreamReader(inputStream)
//            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
//            val line = mutableStateOf("")
//            while(bufferedReader.readLine().also { line.value = it } != null){
//                val row: List<String> = line.value.split(",")
//                _csvData.add(row)
//            }
//            Log.i(
//                "TAG()",
//                "FetchCensoredTextThread: Data = \"${_csvData}\""
//            )
//            return


//            val fileReader: FileReader = FileReader(_censoredTextListCsvName)
//            val csvReader: CSVReader = CSVReader(fileReader)
//
//            var nextLine = csvReader.readNext()
//            while (nextLine != null){
//                Log.i(
//                    "TAG()",
//                    "FetchCensoredTextThread: Next line = \"${nextLine}\""
//                )
////                _csvData.add(nextLine)
//                nextLine = csvReader.readNext()
//            }
        }
        catch (e: Exception){
            Log.e(
                "TAG()",
                "FetchCensoredTextThread: Caught exception \"$e\" while reading .csv file"
            )
        }
    }
    // endregion Functions
}