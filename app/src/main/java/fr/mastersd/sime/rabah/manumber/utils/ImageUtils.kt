package fr.mastersd.sime.rabah.manumber.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log

object ImageUtils {

    fun processImageToFlattenedVector(image: Bitmap): IntArray {
        val startTime = System.currentTimeMillis()

        val rotatedBitmap = rotateBitmap(image, 90f)
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 28, 28, true)

        val threshold = calculateOtsuThreshold(resizedBitmap)
        val binarizedPixels = binarizeImage(resizedBitmap, threshold)

        Log.d("Performance", "Binarisation totale : ${System.currentTimeMillis() - startTime} ms")
        return flattenBinarizedImage(binarizedPixels)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun binaryToBitmap(binarizedPixels: IntArray, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        binarizedPixels.forEachIndexed { index, value ->
            val x = index % width
            val y = index / width
            val color = if (value == 1) Color.BLACK else Color.WHITE
            bitmap.setPixel(x, y, color)
        }
        return bitmap
    }

    private fun calculateOtsuThreshold(image: Bitmap): Int {
        val histogram = IntArray(256)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val gray = Color.red(image.getPixel(x, y))
                histogram[gray]++
            }
        }

        val totalPixels = image.width * image.height
        var sum = 0
        for (i in histogram.indices) sum += i * histogram[i]

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

            val variance = wB.toDouble() * wF * (mB - mF) * (mB - mF)
            if (variance > maxVariance) {
                maxVariance = variance
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
                binarized[y][x] = if (gray < threshold) 1 else 0
            }
        }
        return binarized
    }

    private fun flattenBinarizedImage(binarized: Array<IntArray>): IntArray {
        return binarized.flatMap { it.toList() }.toIntArray()
    }
}
