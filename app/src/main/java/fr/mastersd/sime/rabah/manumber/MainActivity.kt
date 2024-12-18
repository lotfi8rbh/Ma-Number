package fr.mastersd.sime.rabah.manumber

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialiser CameraManager
        cameraManager = CameraManager(this, Executors.newSingleThreadExecutor()) { file ->
            processCapturedImage(file)
        }
        cameraManager.startCamera(viewBinding.viewFinder.surfaceProvider)

        // Bouton pour capturer une image
        viewBinding.captureButton.setOnClickListener {
            val file = File(externalMediaDirs.first(), "captured_image.jpg")
            cameraManager.captureImage(file)
        }
    }

    private fun processCapturedImage(file: File) {
        try {
            // Charger l'image capturée
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                Toast.makeText(this, "Erreur lors du chargement de l'image.", Toast.LENGTH_SHORT).show()
                return
            }

            // Traitement d'image pour générer une image binaire
            val flattenedVector = ImageUtils.processImageToFlattenedVector(bitmap)

            // Afficher l'image binaire
            val binaryBitmap = ImageUtils.binaryToBitmap(flattenedVector, 28, 28)
            viewBinding.binaryImageView.setImageBitmap(binaryBitmap)

            // Optionnel : afficher le vecteur aplati dans un Toast
            Toast.makeText(this, "Vecteur généré : ${flattenedVector.joinToString(", ") { it.toString() }}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
