package fr.mastersd.sime.rabah.manumber.backend

import org.json.JSONObject
import java.nio.charset.Charset

data class SVMModel(val weights: Array<DoubleArray>, val bias: Double)

fun predictSVM(input: IntArray, model: SVMModel): Int {
    val weights = model.weights[0]
    val bias = model.bias

    // Calculer la somme pondérée
    var decision = 0.0
    for (i in input.indices) {
        decision += input[i] * weights[i]
    }
    decision += bias

    // Retourner la classe prédite
    return if (decision > 0) 1 else 0
}

fun loadSVMModel(filePath: String): SVMModel {
    // Charger le fichier JSON depuis les assets
    val json = JSONObject(
        SVMModel::class.java.classLoader!!.getResource(filePath)!!.readText(Charset.defaultCharset())
    )

    // Extraire les poids et le biais
    val weights = json.getJSONArray("weights").let { weightsArray ->
        Array(weightsArray.length()) { i ->
            weightsArray.getJSONArray(i).let { row ->
                DoubleArray(row.length()) { j -> row.getDouble(j) }
            }
        }
    }
    val bias = json.getJSONArray("bias").getDouble(0)

    return SVMModel(weights, bias)
}
