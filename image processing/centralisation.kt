import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO



// Fonction pour charger l'image
fun loadImage(imagePath: String): BufferedImage {
    return ImageIO.read(File(imagePath)) ?: throw IllegalArgumentException("Erreur de chargement de l'image.")
}


// Calcule le seuil de binarisation optimal avec la méthode d'Otsu
fun otsuThreshold(image: BufferedImage): Int {
    val histogram = IntArray(256)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            histogram[Color(image.getRGB(x, y)).red]++
        }
    }

    val totalPixels = image.width * image.height
    var sum = histogram.indices.sumOf { it * histogram[it] }
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

    println("Seuil optimal trouvé par Otsu : $threshold")
    return threshold
}

// Binarise l'image avec le seuil spécifié
fun binarizeImage(image: BufferedImage, threshold: Int): Array<IntArray> {
    return Array(image.height) { y ->
        IntArray(image.width) { x ->
            if (Color(image.getRGB(x, y)).red < threshold) 1 else 0
        }
    }
}

// Fonction pour
fun cropImage(pixels: Array<IntArray>): Array<IntArray> {
    // Étape 1 : Supprimer les lignes contenant uniquement des 0
    val filteredRows = pixels.filter { row -> row.any { it == 1 } }.toTypedArray()

    // Étape 2 : Supprimer les colonnes contenant uniquement des 0
    val columnsToKeep = filteredRows[0].indices.filter { colIndex ->
        filteredRows.any { row -> row[colIndex] == 1 }
    }

    // Créer une nouvelle matrice en gardant seulement les colonnes valides
    val croppedMatrix = filteredRows.map { row ->
        columnsToKeep.map { colIndex -> row[colIndex] }.toIntArray()
    }.toTypedArray()

// Étape 3 : Ajouter 32 lignes de 0 en haut et en bas
    val widthWithPadding = croppedMatrix[0].size + 128
    val paddingRow = IntArray(widthWithPadding) { 0 }

    // Ajouter 32 lignes de 0 en haut
    val matrixWithPadding = mutableListOf<IntArray>()
    repeat(64) {
        matrixWithPadding.add(paddingRow)
    }

    // Ajouter les colonnes de 0 à gauche et à droite pour chaque ligne existante
    for (row in croppedMatrix) {
        val paddedRow = IntArray(widthWithPadding)
        for (i in row.indices) {
            paddedRow[i + 64] = row[i] // Décalage pour ajouter 32 colonnes de 0 à gauche
        }
        matrixWithPadding.add(paddedRow)
    }

    // Ajouter 32 lignes de 0 en bas
    repeat(64) {
        matrixWithPadding.add(paddingRow)
    }

    return matrixWithPadding.toTypedArray()
}



// Redimensionne et aplatit l'image en vecteur
fun resizeAndFlattenImage(image: BufferedImage, width: Int, height: Int): List<Int> {
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(image, 0, 0, width, height, null)
    graphics.dispose()

    return List(height * width) { i ->
        val x = i % width
        val y = i / width
        if (Color(resizedImage.getRGB(x, y)).red > 127) 1 else 0
    }
}

// Transforme une matrice en image
fun createImageFromMatrix(pixels: Array<IntArray>): BufferedImage {
    val height = pixels.size
    val width = pixels[0].size
    val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)

    for (y in pixels.indices) {
        for (x in pixels[y].indices) {
            image.setRGB(x, y, if (pixels[y][x] == 1) Color.WHITE.rgb else Color.BLACK.rgb)
        }
    }
    return image
}

// Fonction principale
fun main() {
    val imagePath = "res/images/centre.jpeg"
    val outputPath = "res/images/result.png"

    val originalImage = loadImage(imagePath)
    val threshold = otsuThreshold(originalImage)
    val binarizedPixels = binarizeImage(originalImage, threshold)
    val croppedPixels = cropImage(binarizedPixels)

    val croppedImage = createImageFromMatrix(croppedPixels)
    val resizedImage = resizeAndFlattenImage(croppedImage, 28, 28)
    println("Vecteur aplati : ${resizedImage.joinToString(",")}")

    ImageIO.write(createImageFromMatrix(Array(28) { y -> IntArray(28) { x -> resizedImage[y * 28 + x] } }), "png", File(outputPath))
    println("Image finale sauvegardée à l'emplacement : $outputPath")



}
