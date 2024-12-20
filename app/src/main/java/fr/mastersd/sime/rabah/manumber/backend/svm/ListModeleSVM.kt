package fr.mastersd.sime.rabah.manumber.backend.svm

import android.content.Context

class ListModeleSVM(context: Context) {

    private val listesDeModeleSVM = ArrayList<ModeleSVM>()

    init {
        for (i in 0..9) {
            val cheminBiais = "data/biais_$i.txt"
            val cheminVecteurs = "data/vecteur_support_$i.txt"
            val cheminCoef = "data/coefficients_$i.txt"
            val modele = ModeleSVM(context, cheminVecteurs, cheminCoef, cheminBiais)
            listesDeModeleSVM.add(modele)
        }
    }

    fun predictChiffre(vecteurImage: DoubleArray): Int {
        val scores = listesDeModeleSVM.map { it.predictScore(vecteurImage) }
        return scores.indices.maxByOrNull { scores[it] } ?: -1
    }

}
