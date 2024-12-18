import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color

fun loadImage(imagePath: String): BufferedImage {
    val imageFile = File(imagePath)
    return ImageIO.read(imageFile) ?: throw IllegalArgumentException("Erreur de chargement de l'image.")
}
fun applyGaussianBlur(image: BufferedImage): BufferedImage {
    // Matrice gaussienne 5x5
    val kernel = arrayOf(
        floatArrayOf(1f, 4f, 7f, 4f, 1f),
        floatArrayOf(4f, 16f, 26f, 16f, 4f),
        floatArrayOf(7f, 26f, 41f, 26f, 7f),
        floatArrayOf(4f, 16f, 26f, 16f, 4f),
        floatArrayOf(1f, 4f, 7f, 4f, 1f)
    )

    // Flatten le noyau et somme des valeurs
    val kernelSum = kernel.flatMap { it.asList() }.sum() // Correctement plat et somme des coefficients

    val width = image.width
    val height = image.height
    val outputImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)

    // Appliquer la convolution sur chaque pixel (en ignorant les bords)
    for (y in 2 until height - 2) {
        for (x in 2 until width - 2) {
            var weightedSum = 0f

            // Appliquer le noyau (convolution)
            for (ky in 0 until kernel.size) {
                for (kx in 0 until kernel[ky].size) {
                    val pixelX = x + kx - kernel.size / 2
                    val pixelY = y + ky - kernel.size / 2
                    val grayValue = Color(image.getRGB(pixelX, pixelY)).red
                    weightedSum += grayValue * kernel[ky][kx]
                }
            }

            // Normaliser le résultat
            val newGrayValue = (weightedSum / kernelSum).toInt().coerceIn(0, 255)
            outputImage.setRGB(x, y, Color(newGrayValue, newGrayValue, newGrayValue).rgb)
        }
    }

    println("Filtre gaussien appliqué avec succès.")
    return outputImage
}

fun applySobelEdgeDetection(image: BufferedImage): BufferedImage {
    val width = image.width
    val height = image.height
    val outputImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)

    // Filtres Sobel pour détecter les bords (horizontal et vertical)
    val sobelX = arrayOf(
        intArrayOf(-1, 0, 1),
        intArrayOf(-2, 0, 2),
        intArrayOf(-1, 0, 1)
    )
    val sobelY = arrayOf(
        intArrayOf(-1, -2, -1),
        intArrayOf(0, 0, 0),
        intArrayOf(1, 2, 1)
    )

    // Appliquer la convolution pour détecter les bords
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var gx = 0
            var gy = 0

            // Appliquer les filtres Sobel (horizontal et vertical)
            for (ky in 0 until 3) {
                for (kx in 0 until 3) {
                    val color = Color(image.getRGB(x + kx - 1, y + ky - 1))
                    val grayValue = color.red // L'image est en niveaux de gris
                    gx += grayValue * sobelX[ky][kx]
                    gy += grayValue * sobelY[ky][kx]
                }
            }

            // Calculer l'intensité du bord (magnitude)
            val magnitude = Math.sqrt((gx * gx + gy * gy).toDouble()).toInt().coerceIn(0, 255)

            // Mettre à jour l'image de sortie avec la valeur de bord
            val newColor = Color(magnitude, magnitude, magnitude)
            outputImage.setRGB(x, y, newColor.rgb)
        }
    }

    println("Détection des contours avec Sobel effectuée.")
    return outputImage
}




fun resizeImage(image: BufferedImage, width: Int, height: Int): BufferedImage {
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(image, 0, 0, width, height, null)
    graphics.dispose()
    println("Image redimensionnée à : ${width}x${height}")
    return resizedImage
}

fun otsuThreshold(image: BufferedImage): Int {
    val histogram = IntArray(256)

    // Calculer l'histogramme des pixels
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))
            val grayValue = color.red // L'image est en niveaux de gris
            histogram[grayValue]++
        }
    }

    // Total des pixels dans l'image
    val totalPixels = image.width * image.height

    // Variables pour Otsu
    var sum = 0
    for (i in 0..255) {
        sum += i * histogram[i]
    }

    var sumB = 0
    var wB = 0
    var wF: Int
    var maximumVariance = 0.0
    var threshold = 0

    // Appliquer l'algorithme d'Otsu pour trouver le seuil optimal
    for (i in 0..255) {
        wB += histogram[i] // Poids de la classe de fond
        if (wB == 0) continue

        wF = totalPixels - wB // Poids de la classe d'avant-plan
        if (wF == 0) break

        sumB += i * histogram[i] // Somme des pixels dans la classe de fond
        val mB = sumB / wB // Moyenne des pixels dans la classe de fond
        val mF = (sum - sumB) / wF // Moyenne des pixels dans la classe d'avant-plan

        // Calculer la variance entre les classes
        val betweenVariance = wB * wF * (mB - mF) * (mB - mF)

        // Trouver le seuil qui maximise la variance entre les classes
        if (betweenVariance > maximumVariance) {
            maximumVariance = betweenVariance.toDouble()
            threshold = i
        }
    }

    println("Seuil optimal trouvé par Otsu : $threshold")
    return threshold
}

fun binarizeImage(image: BufferedImage, threshold: Int): Array<IntArray> {
    val width = image.width
    val height = image.height
    val binarizedPixels = Array(height) { IntArray(width) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = Color(image.getRGB(x, y))
            val grayValue = color.red // L'image est déjà en niveaux de gris
            binarizedPixels[y][x] = if (grayValue > threshold) 0 else 1
        }
    }
    println("Image binarisée avec un seuil de $threshold.")
    return binarizedPixels
}

fun flattenBinarizedImage(binarizedPixels: Array<IntArray>): IntArray {
    val flatVector = binarizedPixels.flatMap { it.toList() }.toIntArray()
    println("Image aplatie en vecteur de taille : ${flatVector.size}")
    return flatVector
}

fun main() {
    val imagePath = "res/images/test9.jpg"

    try {
        val originalImage = loadImage(imagePath)
        // Appliquer un filtre gaussien pour réduire le bruit
        val blurredImage = applyGaussianBlur(originalImage)
        //val edgeDetectedImage = applySobelEdgeDetection(originalImage)

        // Redimensionner l'image après réduction du bruit
        val resizedImage = resizeImage(blurredImage, 28, 28)

        // Calculer le seuil optimal avec Otsu
        val otsuThresholdValue = otsuThreshold(resizedImage)

        // Binariser l'image avec le seuil trouvé par Otsu
        val binarizedPixels = binarizeImage(resizedImage, otsuThresholdValue)
        val flattenedVector = flattenBinarizedImage(binarizedPixels)

        println("Vecteur aplati :")
        println(flattenedVector.joinToString(",") { it.toString() })

    } catch (e: Exception) {
        println("Erreur : ${e.message}")
    }
}

