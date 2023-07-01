package com.nirbhay.pdfscanner.model

import androidx.lifecycle.ViewModel

class ImagesListViewModel : ViewModel() {
    companion object{
        lateinit var imageUris: ArrayList<String>
    }

    fun setList() {
        imageUris = ArrayList()
    }

    fun getList(): ArrayList<String> {
        return imageUris
    }

}