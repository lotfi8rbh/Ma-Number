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
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            Log.d("MainActivity", "Image capturée : ${file.absolutePath}")

            // Charger l'image capturée
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

            // Traiter l'image en binaire
            val binarizedPixels = ImageUtils.processImageToFlattenedVector(bitmap)

            // Convertir les pixels binaires en un Bitmap
            val binarizedBitmap = ImageUtils.binaryToBitmap(binarizedPixels, 28, 28)

            // Afficher l'image binarisée dans l'ImageView
            runOnUiThread {
                binding.binaryImageView.setImageBitmap(binarizedBitmap)
            }
        } else {
            Log.e("MainActivity", "Le fichier capturé n'existe pas.")
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