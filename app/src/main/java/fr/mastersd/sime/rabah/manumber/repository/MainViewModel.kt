package fr.mastersd.sime.rabah.manumber.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _predictedDigit = MutableLiveData<String>()
    val predictedDigit: LiveData<String> get() = _predictedDigit

    fun setPredictedDigit(digit: String) {
        _predictedDigit.value = digit
    }
}
