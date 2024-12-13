import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color  // Import spécifique pour éviter les conflits

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

fun normalizeImage(image: BufferedImage): Array<DoubleArray> {
    val width = image.width
    val height = image.height
    val normalizedPixels = Array(height) { DoubleArray(width) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = Color(image.getRGB(x, y))  // Utilisation explicite de java.awt.Color
            val grayValue = color.red  // L'image est déjà en niveaux de gris, donc rouge = vert = bleu
            normalizedPixels[y][x] = grayValue / 255.0
        }
    }
    println("Image normalisée.")
    return normalizedPixels
}

fun flattenImage(normalizedPixels: Array<DoubleArray>): DoubleArray {
    val flatVector = normalizedPixels.flatMap { it.toList() }.toDoubleArray()
    println("Image aplatie en vecteur de taille : ${flatVector.size}")
    return flatVector
}

fun main() {
    val imagePath = "res/images/test1.jpg"


    try {
        val originalImage = loadImage(imagePath)
        val resizedImage = resizeImage(originalImage, 28, 28)
        val normalizedPixels = normalizeImage(resizedImage)
        val flattenedVector = flattenImage(normalizedPixels)

        println("Vecteur aplati :")
        println(flattenedVector.joinToString("  ") { String.format(java.util.Locale.US, "%.2f", it) })


    } catch (e: Exception) {
        println("Erreur : ${e.message}")

    }
}