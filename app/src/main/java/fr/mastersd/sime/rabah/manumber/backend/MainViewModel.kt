package fr.mastersd.sime.rabah.manumber.backend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.mastersd.sime.rabah.manumber.repository.DigitRepository

class MainViewModel : ViewModel() {
    private val repository = DigitRepository()
    private val _predictedDigit = MutableLiveData<String>()
    val predictedDigit: LiveData<String> get() = _predictedDigit

    fun updatePredictedDigit() {
        _predictedDigit.value = repository.predictedDigit
    }
}