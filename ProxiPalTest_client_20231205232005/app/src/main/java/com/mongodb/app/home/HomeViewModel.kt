package com.mongodb.app.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import androidx.lifecycle.viewModelScope
import com.mongodb.app.data.SyncRepository

class HomeViewModel(private val repository: SyncRepository) : ViewModel() {
    // Change to store List<Bitmap> to reflect the actual data type used for UI updates
    var imagesList = mutableStateOf<List<Bitmap?>>(listOf())
        private set

    init {
        viewModelScope.launch {
            repository.getUserProfilePhotos().collect { photoList ->
                // Convert each Base64 string back to Bitmap and update UI
                imagesList.value = photoList.mapNotNull { it.toBitmapOrNull() }
            }
        }
    }

    fun addImage(bitmap: Bitmap) {
        // Convert Bitmap to Base64 String
        val base64Image = bitmap.toBase64String()
        viewModelScope.launch {
            repository.updateUserProfilePhotos(imagesList.value.map { it?.toBase64String() ?: "" } + base64Image)
            // Directly add Bitmap to imagesList for UI updates
            imagesList.value = imagesList.value + bitmap
        }
    }

    fun replaceImage(index: Int, newBitmap: Bitmap) {
        // Convert Bitmap to Base64 String
        val base64Image = newBitmap.toBase64String()
        val mutableList = imagesList.value.toMutableList()
        mutableList[index] = newBitmap // Replace with new Bitmap
        viewModelScope.launch {
            repository.updateUserProfilePhotos(mutableList.map { it?.toBase64String() ?: "" })
            imagesList.value = mutableList
        }
    }
}

fun Bitmap.toBase64String(): String {
    ByteArrayOutputStream().apply {
        compress(Bitmap.CompressFormat.JPEG, 100, this)
        return Base64.encodeToString(toByteArray(), Base64.DEFAULT)
    }
}

fun String.toBitmapOrNull(): Bitmap? {
    return try {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: IllegalArgumentException) {
        // Handle error or return null if the string cannot be decoded to a Bitmap
        null
    }
}