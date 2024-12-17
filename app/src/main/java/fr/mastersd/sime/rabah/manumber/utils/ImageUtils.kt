package fr.mastersd.sime.rabah.manumber.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color

object ImageUtils {

    fun loadImage(imagePath: String): Bitmap {
        val options = BitmapFactory.Options()
        val originalBitmap = BitmapFactory.decodeFile(imagePath, options)
        return if (originalBitmap != null) {
            // Corrige l'orientation si nécessaire
            rotateBitmap(originalBitmap, 0) // 0 degrés pour garder sans rotation
        } else {
            throw IllegalArgumentException("Erreur de chargement de l'image.")
        }
    }

    // Méthode pour corriger la rotation de l'image
    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    fun resizeImage(image: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun binarizeImage(image: Bitmap, threshold: Int): Array<IntArray> {
        val binarized = Array(image.height) { IntArray(image.width) }
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val gray = Color.red(image.getPixel(x, y))
                binarized[y][x] = if (gray > threshold) 0 else 1
            }
        }
        return binarized
    }

    fun flattenImage(binarized: Array<IntArray>): IntArray {
        return binarized.flatMap { it.toList() }.toIntArray()
    }
}
