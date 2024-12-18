package fr.mastersd.sime.rabah.manumber.utils

import android.graphics.Bitmap
import android.graphics.Color

object ImageUtils {

    fun processImageToFlattenedVector(image: Bitmap): IntArray {
        // 1. Rotation de l'image si nécessaire
        val rotatedBitmap = rotateBitmap(image, 90f)

        // 2. Redimensionnement à 28x28
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 28, 28, true)

        // 3. Calcul du seuil d'Otsu
        val threshold = calculateOtsuThreshold(resizedBitmap)

        // 4. Binarisation de l'image
        val binarizedPixels = binarizeImage(resizedBitmap, threshold)

        // 5. Conversion en vecteur aplati
        return flattenBinarizedImage(binarizedPixels)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun binaryToBitmap(binarizedPixels: IntArray, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val color = if (binarizedPixels[index] == 1) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
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
        var maxVariance = 0.0
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
            if (variance > maxVariance) {
                maxVariance = variance.toDouble()
                threshold = i
            }
        }
        return threshold
    }

    private fun binarizeImage(image: Bitmap, threshold: Int): Array<IntArray> {
        val binarized = Array(image.height) { IntArray(image.width) }
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val gray = Color.red(image.getPixel(x, y))
                binarized[y][x] = if (gray > threshold) 1 else 0
            }
        }
        return binarized
    }

    private fun flattenBinarizedImage(binarized: Array<IntArray>): IntArray {
        return binarized.flatMap { it.toList() }.toIntArray()
    }


}
