package fr.mastersd.sime.rabah.manumber.backend.svm

import android.content.Context

class ModeleSVM(
    context: Context,
    fichierSupportVecteurs: String,
    fichierCoefficients: String,
    fichierBiais: String
) {
    private var vecteursSupport: Array<DoubleArray>
    private var coefficients: DoubleArray
    private var biais: Double
    private val gamma: Double = 0.001

    init {
        vecteursSupport = chargerVecteursSupports(context, fichierSupportVecteurs)
        coefficients = chargerCoefficients(context, fichierCoefficients)
        biais = chargerBiais(context, fichierBiais)
    }

    fun predictScore(vecteurImage: DoubleArray): Double {
        var score = 0.0
        for (i in vecteursSupport.indices) {
            val valeurNoyau = noyauRbf(vecteurImage, vecteursSupport[i])
            score += coefficients[i] * valeurNoyau
        }
        return score + biais
    }

    private fun noyauRbf(vecteurImage: DoubleArray, vecteurSupport: DoubleArray): Double {
        val diff = vecteurImage.zip(vecteurSupport) { a, b -> (a - b).let { it * it } }
        return kotlin.math.exp(-gamma * diff.sum())
    }

    private fun chargerVecteursSupports(context: Context, fichier: String): Array<DoubleArray> {
        val vecteursSupport = mutableListOf<DoubleArray>()
        context.assets.open(fichier).bufferedReader().use { br ->
            br.lineSequence().forEach { line ->
                val values = line.split("\\s+".toRegex()).map { it.toDouble() }.toDoubleArray()
                vecteursSupport.add(values)
            }
        }
        return vecteursSupport.toTypedArray()
    }

    private fun chargerCoefficients(context: Context, fichier: String): DoubleArray {
        return context.assets.open(fichier).bufferedReader().use { br ->
            br.readLine().split("\\s+".toRegex()).map { it.toDouble() }.toDoubleArray()
        }
    }

    private fun chargerBiais(context: Context, fichier: String): Double {
        return context.assets.open(fichier).bufferedReader().use { br ->
            br.readLine().toDouble()
        }
    }
}
