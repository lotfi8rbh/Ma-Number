package fr.mastersd.sime.rabah.manumber

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import backend.svm.ListModeleSVM
import fr.mastersd.sime.rabah.manumber.databinding.FragmentCameraBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraManager: CameraManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var svmListModel: ListModeleSVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser CameraManager
        cameraManager = CameraManager(
            context = requireContext(),
            executor = Executors.newSingleThreadExecutor()
        ) { file ->
            processCapturedImage(file)
        }

        svmListModel = ListModeleSVM(requireContext())

        // Démarrer la caméra
        cameraManager.startCamera(binding.cameraPreviewView.surfaceProvider)

        // Bouton de capture
        binding.captureButton.setOnClickListener {
            val outputFile = File(requireContext().externalCacheDir, "captured_image.jpg")
            cameraManager.captureImage(outputFile)
        }
    }

    private fun processCapturedImage(file: File) {
        if (!file.exists()) {
            Toast.makeText(requireContext(), "Erreur : Fichier introuvable.", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            val startTime = System.currentTimeMillis()

            // Charger et redimensionner l'image
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

            // Binarisation
            val binarizedPixels = withContext(Dispatchers.Default) {
                ImageUtils.processImageToFlattenedVector(resizedBitmap)
            }
            val binarizedBitmap = ImageUtils.binaryToBitmap(binarizedPixels, 28, 28)
            binding.binaryImageView.setImageBitmap(binarizedBitmap)

            // Prédiction
            val flattenedVector = binarizedPixels.map { it.toDouble() }.toDoubleArray()
            val predictedDigit = withContext(Dispatchers.Default) {
                svmListModel.predictChiffre(flattenedVector)
            }

            binding.predictionTextView.text = "Prédiction : $predictedDigit"
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("Performance", "Temps total : $elapsedTime ms")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        coroutineScope.cancel()
    }
}
