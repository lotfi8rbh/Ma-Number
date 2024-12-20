package fr.mastersd.sime.rabah.manumber

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.mastersd.sime.rabah.manumber.backend.svm.ListModeleSVM
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import kotlinx.coroutines.*
import java.io.File

class CameraViewModel(private val context: Context) : ViewModel() {

    private val _binarizedBitmap = MutableLiveData<Bitmap?>()
    val binarizedBitmap: LiveData<Bitmap?> get() = _binarizedBitmap

    private val _predictedDigit = MutableLiveData<Int?>()
    val predictedDigit: LiveData<Int?> get() = _predictedDigit

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var svmListModel: ListModeleSVM

    init {
        svmListModel = ListModeleSVM(context)
    }

    fun processCapturedImage(file: File) {
        if (!file.exists()) return

        coroutineScope.launch {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

            // Étape 1 : Binarisation
            val binarizedPixels = ImageUtils.processImageToFlattenedVector(resizedBitmap)
            val binarizedBitmap = ImageUtils.binaryToBitmap(binarizedPixels, 28, 28)
            _binarizedBitmap.postValue(binarizedBitmap)

            // Étape 2 : Prédiction
            val flattenedVector = binarizedPixels.map { it.toDouble() }.toDoubleArray()
            val predictedDigit = svmListModel.predictChiffre(flattenedVector)
            _predictedDigit.postValue(predictedDigit)
        }
    }

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}
