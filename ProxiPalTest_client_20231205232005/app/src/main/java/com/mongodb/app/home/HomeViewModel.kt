package com.mongodb.app.home

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import android.graphics.Bitmap


class HomeViewModel : ViewModel() {
    // State for the list of images, initialized empty and observed by Compose
    var imagesList = mutableStateOf<List<Bitmap?>>(listOf())
        private set // Make the setter private

    fun updateImagesList(newList: List<Bitmap?>) {
        imagesList.value = newList
    }

    fun addImage(bitmap: Bitmap) {
        updateImagesList(imagesList.value + listOf(bitmap))
    }

    fun replaceImage(index: Int, newBitmap: Bitmap) {
        val mutableList = imagesList.value.toMutableList()
        mutableList[index] = newBitmap
        updateImagesList(mutableList)
    }
}