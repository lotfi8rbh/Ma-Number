package fr.mastersd.sime.rabah.manumber.utils

import android.graphics.Bitmap
import android.graphics.Color

object OtsuThreshold {

    fun calculateThreshold(image: Bitmap): Int {
        val histogram = IntArray(256)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val gray = Color.red(image.getPixel(x, y))
                histogram[gray]++
            }
        }

        val total = image.width * image.height
        var sum = 0
        for (i in histogram.indices) sum += i * histogram[i]

        var sumB = 0
        var wB = 0
        var maxVariance = 0.0
        var threshold = 0

        for (i in histogram.indices) {
            wB += histogram[i]
            if (wB == 0) continue

            val wF = total - wB
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
}
