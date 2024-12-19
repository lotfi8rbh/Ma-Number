package fr.mastersd.sime.rabah.manumber

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import backend.svm.ListModeleSVM
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var svmListModel: ListModeleSVM


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Charger les modèles SVM
        svmListModel = ListModeleSVM(this)


        // Initialiser CameraManager
        cameraManager = CameraManager(
            context = this,
            executor = Executors.newSingleThreadExecutor()
        ) { file ->
            processCapturedImage(file)
        }

        // Vérifier et demander les permissions avant de démarrer la caméra
        if (checkPermissions()) {
            cameraManager.startCamera(binding.viewFinder.surfaceProvider)
        }

        // Configurer le bouton de capture
        binding.captureButton.setOnClickListener {
            val outputFile = File(getExternalFilesDir(null), "captured_image.jpg")
            cameraManager.captureImage(outputFile)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
            false
        } else {
            true
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap, filename: String) {
        val file = File(externalMediaDirs.first(), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Image sauvegardée à : ${file.absolutePath}")
    }

    private fun processCapturedImage(file: File) {
        if (file.exists()) {
            Log.d("MainActivity", "Fichier image trouvé : ${file.absolutePath}")
            Toast.makeText(this, "Chargement de l'image...", Toast.LENGTH_SHORT).show()

            // Charger l'image capturée
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

            // Étape 1 : Appliquer la binarisation
            val binarizedPixels = ImageUtils.processImageToFlattenedVector(bitmap)
            val binarizedBitmap = ImageUtils.binaryToBitmap(binarizedPixels, 28, 28)
            Log.d("TraitementImage", "Image binarisée et convertie en vecteur aplati.")
            Toast.makeText(this, "Image binarisée.", Toast.LENGTH_SHORT).show()

            // Afficher l'image binarisée
            runOnUiThread {
                binding.binaryImageView.setImageBitmap(binarizedBitmap)
            }

            // Étape 2 : Prédiction du chiffre
            val flattenedVector = binarizedPixels.map { it.toDouble() }.toDoubleArray()
            Toast.makeText(this, "Prédiction en cours...", Toast.LENGTH_SHORT).show()
            val predictedDigit = svmListModel.predictChiffre(flattenedVector)

            Log.d("Prédiction", "Classe prédite : $predictedDigit")
            Toast.makeText(this, "Classe prédite : $predictedDigit", Toast.LENGTH_LONG).show()

            // Afficher la prédiction
            runOnUiThread {
                binding.textPrediction.text = "Prédiction : $predictedDigit"
            }
        } else {
            Log.e("MainActivity", "Le fichier image n'existe pas.")
            Toast.makeText(this, "Erreur : Fichier introuvable.", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            cameraManager.startCamera(binding.viewFinder.surfaceProvider)
        } else {
            Toast.makeText(this, "Permissions refusées. L'application ne peut pas fonctionner.", Toast.LENGTH_LONG).show()
        }
    }
}