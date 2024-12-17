package fr.mastersd.sime.rabah.manumber.utils

import android.content.Context
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File


class CameraManager(
    private val context: Context, // Le contexte pour accéder aux fonctionnalités Android
    private val onImageCaptured: (String) -> Unit // Callback appelé quand l'image est capturée
) {
    private lateinit var imageCapture: ImageCapture

    // Fonction pour démarrer la caméra
    fun startCamera(surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configuration de la prévisualisation (flux vidéo de la caméra)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            // Configuration de l'objet ImageCapture pour capturer des images
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Ratio d'aspect de l'image
                .build()

            // Relier le cycle de vie de l'application à CameraX
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA, // Utilisation de la caméra arrière
                preview, imageCapture
            )
        }, ContextCompat.getMainExecutor(context)) // Exécution sur le thread principal
    }

    // Fonction pour capturer une image
    fun captureImage(outputFile: File) {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onImageCaptured(outputFile.absolutePath) // Appel du callback avec le chemin de l'image
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Erreur : ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

