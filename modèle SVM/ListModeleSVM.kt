import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.util.ArrayList
import javax.imageio.ImageIO



class ListModeleSVM {

    inner class ModeleSVM(
        fichierSupportVecteurs: String,
        fichierCoefficients: String,
        fichierBiais: String
    ) {

        private var vecteursSupport: Array<DoubleArray> // Matrice de vecteurs de support pour chaque classe
        private var coefficients: DoubleArray // Tableau des coefficients (alpha) pour chaque classe
        private var biais: Double // Biais
        private val gamma: Double = 0.001 // Paramètre gamma pour le noyau RBF

        init {
            this.vecteursSupport = chargerVecteursSupports(fichierSupportVecteurs)
            this.coefficients = chargerCoefficients(fichierCoefficients)
            this.biais = chargerBiais(fichierBiais)
        }

        @Throws(IOException::class)
        private fun chargerCoefficients(file: String): DoubleArray {
            BufferedReader(FileReader(file)).use { br ->
                val line = br.readLine()

                // Vérification si la ligne est vide
                if (line.isNullOrBlank()) {
                    throw IOException("Le fichier est vide ou ne contient pas de coefficients.")
                }

                // Diviser la ligne par les espaces, virgules ou points-virgules
                val values = line.trim().split("[\\s,;]+".toRegex())
                return DoubleArray(values.size) { i ->
                    try {
                        values[i].toDouble()
                    } catch (e: NumberFormatException) {
                        throw IOException("Erreur de format à la position $i: ${values[i]}", e)
                    }
                }
            }
        }

        @Throws(IOException::class)
        private fun chargerVecteursSupports(fichierSupportVecteurs: String): Array<DoubleArray> {
            val vecteursSupport = mutableListOf<DoubleArray>()
            BufferedReader(FileReader(fichierSupportVecteurs)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    val values = line!!.split("\\s+".toRegex())
                    val row = DoubleArray(values.size) { i -> values[i].toDouble() }
                    vecteursSupport.add(row)
                }
            }
            return vecteursSupport.toTypedArray()
        }

        @Throws(IOException::class)
        private fun chargerBiais(fichierBiais: String): Double {
            BufferedReader(FileReader(fichierBiais)).use { br ->
                val biais = br.readLine()
                return biais.toDouble()
            }
        }

        // Convertit un vecteur d'entiers en un vecteur de doubles
        private fun convertirIntArrayEnDoubleArray(vecteurInt: IntArray): DoubleArray {
            return DoubleArray(vecteurInt.size) { i -> vecteurInt[i].toDouble() }
        }

        private fun noyauRbf(vecteurImage: IntArray, vecteurSupport: DoubleArray): Double {
            val vecteurImageDouble = convertirIntArrayEnDoubleArray(vecteurImage)
            var sum = 0.0
            for (i in vecteurImageDouble.indices) {
                sum += (vecteurImageDouble[i] - vecteurSupport[i]).let { it * it }
            }
            return kotlin.math.exp(-this.gamma * sum)
        }

        fun predictScore(vecteurImage: IntArray): Double {
            var score = 0.0

            for (i in vecteursSupport.indices) {
                val valeurNoyau = noyauRbf(vecteurImage, vecteursSupport[i])
                score += coefficients[i] * valeurNoyau
            }

            // Ajouter le biais
            score += biais

            return score
        }
    }

    private val listesDeModeleSVM = ArrayList<ModeleSVM>()

    init {
        for (i in 0..9) {
            val cheminBiais = "data/biais_$i.txt"
            val cheminVecteurs = "data/vecteur_support_$i.txt"
            val cheminCoef = "data/coefficients_$i.txt"

            val modelsvm = ModeleSVM(cheminVecteurs, cheminCoef, cheminBiais)
            listesDeModeleSVM.add(modelsvm)
        }
    }

    fun predictChiffre(vecteurImage: IntArray): Int {
        val scores = DoubleArray(10)
        for ((i, modele) in listesDeModeleSVM.withIndex()) {
            scores[i] = modele.predictScore(vecteurImage)
        }

        var classePredite = 0
        var scoreMax = scores[0]
        for (c in 1..9) {
            if (scores[c] > scoreMax) {
                scoreMax = scores[c]
                classePredite = c
            }
        }

        return classePredite
    }
}



fun loadImage(imagePath: String): BufferedImage {
    val imageFile = File(imagePath)
    return ImageIO.read(imageFile) ?: throw IllegalArgumentException("Erreur de chargement de l'image.")
}

fun applyGaussianBlur(image: BufferedImage): BufferedImage {
    val kernel = arrayOf(
        floatArrayOf(1f, 4f, 7f, 4f, 1f),
        floatArrayOf(4f, 16f, 26f, 16f, 4f),
        floatArrayOf(7f, 26f, 41f, 26f, 7f),
        floatArrayOf(4f, 16f, 26f, 16f, 4f),
        floatArrayOf(1f, 4f, 7f, 4f, 1f)
    )
    val kernelSum = kernel.flatMap { it.asList() }.sum()

    val width = image.width
    val height = image.height
    val outputImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)

    for (y in 2 until height - 2) {
        for (x in 2 until width - 2) {
            var weightedSum = 0f

            for (ky in 0 until kernel.size) {
                for (kx in 0 until kernel[ky].size) {
                    val pixelX = x + kx - kernel.size / 2
                    val pixelY = y + ky - kernel.size / 2
                    val grayValue = Color(image.getRGB(pixelX, pixelY)).red
                    weightedSum += grayValue * kernel[ky][kx]
                }
            }

            val newGrayValue = (weightedSum / kernelSum).toInt().coerceIn(0, 255)
            outputImage.setRGB(x, y, Color(newGrayValue, newGrayValue, newGrayValue).rgb)
        }
    }
    return outputImage
}

fun resizeImage(image: BufferedImage, width: Int, height: Int): BufferedImage {
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(image, 0, 0, width, height, null)
    graphics.dispose()
    return resizedImage
}

fun otsuThreshold(image: BufferedImage): Int {
    val histogram = IntArray(256)

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))
            val grayValue = color.red
            histogram[grayValue]++
        }
    }

    val totalPixels = image.width * image.height
    var sum = 0
    for (i in 0..255) {
        sum += i * histogram[i]
    }

    var sumB = 0
    var wB = 0
    var wF: Int
    var maximumVariance = 0.0
    var threshold = 0

    for (i in 0..255) {
        wB += histogram[i]
        if (wB == 0) continue

        wF = totalPixels - wB
        if (wF == 0) break

        sumB += i * histogram[i]
        val mB = sumB / wB
        val mF = (sum - sumB) / wF

        val betweenVariance = wB * wF * (mB - mF) * (mB - mF)

        if (betweenVariance > maximumVariance) {
            maximumVariance = betweenVariance.toDouble()
            threshold = i
        }
    }

    return threshold
}

fun binarizeImage(image: BufferedImage, threshold: Int): Array<IntArray> {
    val width = image.width
    val height = image.height
    val binarizedPixels = Array(height) { IntArray(width) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = Color(image.getRGB(x, y))
            val grayValue = color.red
            binarizedPixels[y][x] = if (grayValue > threshold) 0 else 1
        }
    }
    return binarizedPixels
}

fun flattenBinarizedImage(binarizedPixels: Array<IntArray>): IntArray {
    val flatVector = binarizedPixels.flatMap { it.toList() }.toIntArray()
    return flatVector
}

fun saveBinarizedImage(binarizedPixels: Array<IntArray>, outputPath: String) {
    val height = binarizedPixels.size
    val width = binarizedPixels[0].size
    val outputImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = if (binarizedPixels[y][x] == 1) Color.WHITE else Color.BLACK
            outputImage.setRGB(x, y, color.rgb)
        }
    }

    ImageIO.write(outputImage, "png", File(outputPath))
}

fun main() {
    val imagePath = "images/test22.jpeg"
    val outputPath = "images/testbinaire0.png"
    try {
        val modeleSVM = ListModeleSVM()
        val originalImage = loadImage(imagePath)

        val blurredImage = applyGaussianBlur(originalImage)

        val resizedImage = resizeImage(blurredImage, 28, 28)

        val otsuThresholdValue = otsuThreshold(resizedImage)

        val binarizedPixels = binarizeImage(resizedImage, otsuThresholdValue)

        val flattenedVector = flattenBinarizedImage(binarizedPixels)
        saveBinarizedImage(binarizedPixels, outputPath)

       // val vecteurImage = intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
        val predictedClass = modeleSVM.predictChiffre(flattenedVector)

        // Afficher la classe prédite
        println("Classe prédite : $predictedClass")

    } catch (e: IOException) {
        println("Erreur lors du chargement des fichiers : ${e.message}")
    } catch (e: Exception) {
        println("Une erreur est survenue : ${e.message}")
    }
}
