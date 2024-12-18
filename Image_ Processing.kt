import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color

fun loadImage(imagePath: String): BufferedImage {
    val imageFile = File(imagePath)
    return ImageIO.read(imageFile) ?: throw IllegalArgumentException("Erreur de chargement de l'image.")
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
            binarizedPixels[y][x] = if (grayValue > threshold) 1 else 0
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
    val imagePath = "res/images/test1.png"

    try {
        val originalImage = loadImage(imagePath)
        val resizedImage = resizeImage(originalImage, 28, 28)

        // Calculer le seuil optimal avec Otsu
        val otsuThresholdValue = otsuThreshold(resizedImage)

        // Binariser l'image avec le seuil trouvé par Otsu
        val binarizedPixels = binarizeImage(resizedImage, otsuThresholdValue)
        val flattenedVector = flattenBinarizedImage(binarizedPixels)

        println("Vecteur aplati :")
        println(flattenedVector.joinToString(" ") { it.toString() })

    } catch (e: Exception) {
        println("Erreur : ${e.message}")
    }
}

