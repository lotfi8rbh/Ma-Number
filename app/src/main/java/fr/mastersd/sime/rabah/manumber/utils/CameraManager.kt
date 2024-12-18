package fr.mastersd.sime.rabah.manumber.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService

class CameraManager(
    private val context: Context,
    private val executor: ExecutorService,
    private val onImageSaved: (File) -> Unit
) {
    private lateinit var imageCapture: ImageCapture

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
                .build()

            try {
                // Associer les cas d'utilisation au cycle de vie de l'activité
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(context, "Erreur lors de l'initialisation de la caméra.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun captureImage(outputFile: File) {
        if (!::imageCapture.isInitialized) {
            Toast.makeText(context, "La caméra n'est pas encore prête.", Toast.LENGTH_SHORT).show()
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        Log.d("CameraManager", "Capture d'image démarrée.")
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraManager", "Image capturée : ${outputFile.absolutePath}")
                    onImageSaved(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Erreur de capture : ${exception.message}")
                    Toast.makeText(context, "Erreur : ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
