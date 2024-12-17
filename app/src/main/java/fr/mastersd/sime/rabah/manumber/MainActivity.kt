package fr.mastersd.sime.rabah.manumber

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.utils.ImageUtils
import fr.mastersd.sime.rabah.manumber.utils.OtsuThreshold
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Liaison de l'UI
        setContentView(binding.root)

        // Initialiser CameraManager
        cameraManager = CameraManager(this) { imagePath ->
            processImage(imagePath) // Traitement de l'image capturée
        }

        // Démarrer la caméra
        cameraManager.startCamera(binding.viewFinder.surfaceProvider)

        // Bouton pour capturer une image
        binding.captureButton.setOnClickListener {
            val file = File(externalMediaDirs.first(), "captured_image.jpg")
            cameraManager.captureImage(file)
        }
    }

    private fun processImage(imagePath: String) {
        try {
            val image = ImageUtils.loadImage(imagePath) // Charger l'image depuis le fichier
            val resizedImage = ImageUtils.resizeImage(image, 28, 28) // Redimensionner l'image
            val threshold = OtsuThreshold.calculateThreshold(resizedImage) // Calculer le seuil Otsu
            val binarized = ImageUtils.binarizeImage(resizedImage, threshold) // Binariser l'image
            val vector = ImageUtils.flattenImage(binarized) // Aplatir l'image en vecteur

            println("Vecteur aplati : ${vector.joinToString(" ")}")
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
