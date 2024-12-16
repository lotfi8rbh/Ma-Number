package fr.mastersd.sime.rabah.manumber

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private val handler = Handler(Looper.getMainLooper())
    private var lastCaptureTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialiser CameraX
        startCamera()

        // Initialiser un exécuteur de thread
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Construire l'objet Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Configurer ImageAnalysis pour traiter les frames en temps réel
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(224, 224)) // Résolution cible (peut être ajustée)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { image ->
                        val currentTime = System.currentTimeMillis()
                        // Limiter le traitement à toutes les 3 secondes
                        if (currentTime - lastCaptureTime >= 3000) {
                            lastCaptureTime = currentTime
                            val vector = processImageFrame(image)
                            if (vector != null) {
                                println("Vecteur généré : ${vector.joinToString(" ")}")
                            }
                        }
                        image.close() // Toujours fermer l'image après traitement
                    })
                }

            // Associer les cas d'utilisation au cycle de vie de l'activité
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Erreur lors de l'initialisation de la caméra.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveBitmapToStorage(bitmap: Bitmap, filename: String) {
        val file = File(externalMediaDirs.first(), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Image sauvegardée à : ${file.absolutePath}")
    }

    private fun calculateOtsuThreshold(image: Bitmap): Int {
        val histogram = IntArray(256)

        // Calculer l'histogramme des niveaux de gris
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val grayValue = Color.red(image.getPixel(x, y))
                histogram[grayValue]++
            }
        }

        val totalPixels = image.width * image.height
        var sum = 0
        for (i in histogram.indices) {
            sum += i * histogram[i]
        }

        var sumB = 0
        var wB = 0
        var maximumVariance = 0.0
        var threshold = 0

        for (i in histogram.indices) {
            wB += histogram[i]
            if (wB == 0) continue
            val wF = totalPixels - wB
            if (wF == 0) break

            sumB += i * histogram[i]
            val mB = sumB / wB
            val mF = (sum - sumB) / wF

            val variance = wB * wF * (mB - mF) * (mB - mF)
            if (variance > maximumVariance) {
                maximumVariance = variance.toDouble()
                threshold = i
            }
        }
        return threshold
    }

    private fun processImageFrame(image: ImageProxy): IntArray? {
        val bitmap = imageProxyToBitmap(image) ?: return null

        // Corriger l'orientation
        val rotatedBitmap = rotateBitmap(bitmap, 90f)

        // Redimensionner l'image
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 28, 28, true)

        // Sauvegarder l'image redimensionnée pour débogage
        saveBitmapToStorage(resizedBitmap, "/debug_resized.png")

        // Calculer le seuil dynamique
        val otsuThreshold = calculateOtsuThreshold(resizedBitmap)

        // Binariser l'image
        val binarizedPixels = binarizeImage(resizedBitmap, otsuThreshold)

        // Aplatir l'image
        val vector = flattenBinarizedImage(binarizedPixels)

        // Afficher le vecteur pour vérification
        println("Vecteur généré : ${vector.joinToString(" ")}")

        return vector
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)

        val yuvImage = android.graphics.YuvImage(
            bytes,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height),
            100,
            out
        )
        val byteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun binarizeImage(image: Bitmap, threshold: Int): Array<IntArray> {
        val width = image.width
        val height = image.height
        val binarizedPixels = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = image.getPixel(x, y)
                val grayValue = (pixel shr 16 and 0xFF) // Red channel as grayscale
                binarizedPixels[y][x] = if (grayValue > threshold) 1 else 0
            }
        }
        return binarizedPixels
    }

    private fun flattenBinarizedImage(binarizedPixels: Array<IntArray>): IntArray {
        return binarizedPixels.flatMap { it.toList() }.toIntArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
