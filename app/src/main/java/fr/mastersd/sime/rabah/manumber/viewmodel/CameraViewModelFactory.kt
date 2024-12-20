package fr.mastersd.sime.rabah.manumber.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.mastersd.sime.rabah.manumber.CameraViewModel

class CameraViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
