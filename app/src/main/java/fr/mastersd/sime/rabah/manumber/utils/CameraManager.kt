package fr.mastersd.sime.rabah.manumber.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.concurrent.ExecutorService

class CameraManager(
    private val context: Context,
    private val executor: ExecutorService,
    private val onImageSaved: (File) -> Unit
) {
    private lateinit var imageCapture: ImageCapture

    /**
     * Vérifie les permissions nécessaires et les demande si elles ne sont pas accordées.
     */
    fun checkPermissions(activity: AppCompatActivity): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val permissionsToRequest = mutableListOf<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                1 // Request code
            )
            false
        } else {
            true
        }
    }

    /**
     * Démarre la caméra et configure l'aperçu et la capture.
     */
    fun startCamera(surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configurer l'aperçu de la caméra
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }
            Log.d("CameraManager", "Prévisualisation caméra configurée.")

            // Configurer la capture d'image
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                // Associer les cas d'utilisation au cycle de vie de l'activité
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(context, "Erreur lors de l'initialisation de la caméra.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Capture une image et enregistre le fichier résultant.
     */
    fun captureImage(outputFile: File) {
        if (!::imageCapture.isInitialized) {
            Toast.makeText(context, "Erreur : La caméra n'est pas encore prête.", Toast.LENGTH_SHORT).show()
            Log.e("CameraManager", "imageCapture n'est pas initialisé")
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        Log.d("CameraManager", "Capture d'image démarrée avec le fichier : ${outputFile.absolutePath}")

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraManager", "Image sauvegardée : ${outputFile.absolutePath}")
                    Toast.makeText(context, "Image capturée et sauvegardée.", Toast.LENGTH_SHORT).show()
                    onImageSaved(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Erreur lors de la capture d'image : ${exception.message}")
                    Toast.makeText(context, "Erreur de capture : ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
