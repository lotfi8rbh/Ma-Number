# Intégration de CameraX dans une Application Android en Kotlin

Ce guide explique comment intégrer et configurer **CameraX** pour capturer un flux vidéo en temps réel dans une application Android Kotlin.

---

## Prérequis

Avant de commencer, assurez-vous de disposer des éléments suivants :

- **Android Studio** (version Arctic Fox ou plus récente recommandée)
- **Kotlin** configuré comme langage principal
- Un appareil Android ou un émulateur prenant en charge CameraX
- Permissions configurées pour accéder à la caméra de l'appareil

---

## Étape 1 : Configuration du Projet

1. **Ajoutez les dépendances CameraX dans le fichier `build.gradle` :**

   Dans le fichier `app/build.gradle`, ajoutez les lignes suivantes :

   ```gradle
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
   ```

2. **Ajoutez les permissions nécessaires dans AndroidManifest.xml :**

   ```xml
    <uses-feature android:name="android.hardware.camera.any" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
   ```

   Cela garantit que l'application peut utiliser la caméra.

---

## Étape 2 : Création de l'Interface Utilisateur

Ajoutez un PreviewView et un TextView dans votre fichier de mise en page :

Dans `activity_main.xml` :

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.camera.view.PreviewView
        android:id="@+id/viewFinder"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <TextView
        android:id="@+id/predictedDigit"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="24sp"
        android:textColor="@android:color/black"
        android:layout_gravity="center_horizontal"
        android:padding="16dp"
        android:text="Predicted Digit: -" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## Étape 3 : Configuration de CameraX dans Kotlin

Créez une classe principale `MainActivity` :

Voici un exemple complet de configuration de CameraX :

```kotlin
import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var predictedDigitTextView: TextView

    
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModel.predictedDigit.observe(this, Observer { digit ->
            viewBinding.predictedDigit.text = digit
        })

        if (!hasPermissions(baseContext)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val previewView: PreviewView = viewBinding.viewFinder
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false) {
                    permissionGranted = false
                }
                if (!permissionGranted) {
                    Toast.makeText(this, "Permission request denied.", Toast.LENGTH_LONG).show()
                } else {
                    startCamera()
                }
            }
        }
    companion object {
        private const val TAG = "MaNumber"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
```

---

## Étape 4 : Tester l'Application

Exécutez votre projet sur un appareil ou un émulateur Android :

- Le flux vidéo en direct devrait s'afficher dans le PreviewView.

Résolution des problèmes éventuels :

- Si le flux vidéo ne s'affiche pas, assurez-vous que les permissions caméra ont été accordées.
- Vérifiez la compatibilité de l'appareil avec CameraX.

---

## Étape 5 : Étapes Supplémentaires

- Ajouter une gestion avancée des permissions :
  Utilisez ActivityResultContracts pour gérer les permissions de manière moderne.
- Intégrer un modèle de prédiction (ex. SVM) :
  Traitez les images capturées en temps réel et affichez les résultats.

---

## Image processing
Le fichier `Image_Processing.kt` contient une série de fonctions en Kotlin permettant de réaliser différentes opérations de traitement d'images, telles que le filtrage, la détection de contours, le redimensionnement et la binarisation. Ces opérations sont essentielles pour préparer les images récupérées par CameraX de l'application mobile. Une fois préparées, ces images sont transformées en vecteurs pour être transmises au modèle d'apprentissage automatique afin d'effectuer des prédictions.

---
**`loadImage(imagePath: String): BufferedImage`**

**Description :** Charge une image depuis le chemin spécifié.

**Utilisation :**
```kotlin
val image = loadImage("res/images/test0.jpeg")
```
---
**`applyGaussianBlur(image: BufferedImage): BufferedImage`**

**Description :** Applique un filtre gaussien pour réduire le bruit de l'image.

**Détails :** Utilise une matrice de convolution 5x5 pour le lissage.

**Utilisation :**
```kotlin
val blurredImage = applyGaussianBlur(image)
```
---
**`applySobelEdgeDetection(image: BufferedImage): BufferedImage`**

**Description :** Applique le filtre Sobel pour détecter les contours horizontaux et verticaux.

**Remarque :** Dans ce projet, nous n'appliquons pas le filtre Sobel car nous avons constaté que les chiffres n'étaient pas bien récupérés après cette étape de détection des contours. Le lissage par filtre gaussien suivi de la binarisation donne de meilleurs résultats pour notre cas d'usage.

**Utilisation :**
```kotlin
val edgeDetectedImage = applySobelEdgeDetection(image)
```
---
**`resizeImage(image: BufferedImage, width: Int, height: Int): BufferedImage`**

**Description :** Redimensionne l'image aux dimensions spécifiées.

**Utilisation :**
```kotlin
val resizedImage = resizeImage(image, 28, 28)
```
---
**`otsuThreshold(image: BufferedImage): Int`**

**Description :** Calcule le seuil optimal de binarisation en utilisant la méthode d'Otsu.

**Utilisation :**
```kotlin
val threshold = otsuThreshold(image)
```
---
**`binarizeImage(image: BufferedImage, threshold: Int): Array<IntArray>`**

**Description :** Binarise l'image en utilisant le seuil spécifié.

**Utilisation :**
```kotlin
val binarizedPixels = binarizeImage(image, threshold)
```
---
**`flattenBinarizedImage(binarizedPixels: Array<IntArray>): IntArray`**   
**Description :** Aplatie l'image binarisée en un vecteur unidimensionnel.  
**Utilisation :**
```kotlin
val flattenedVector = flattenBinarizedImage(binarizedPixels)
```
---

Le fichier **`Centralisation.kt`** : est une version améliorée de **`Image_Processing.kt`**.

Dans cette version, nous avons abandonné l'utilisation du filtre gaussien. À la place, l'accent est mis sur la détection du chiffre présent dans l'image. Une fois le chiffre détecté, l'image est rognée pour ne conserver que la zone pertinente, puis elle est transformée en vecteur afin d'être préparée pour le modèle d'apprentissage automatique.

---

**`cropImage(pixels: Array<IntArray>): Array<IntArray>`**

**Description :**  Cette fonction rogne l'image binarisée en éliminant les lignes et colonnes contenant uniquement des 0 (celles qui sont vides). Elle ajoute également une bordure de 64 pixels (de 0) tout autour de l'image, permettant ainsi de se concentrer uniquement sur la zone contenant le chiffre à traiter, en éliminant les parties non pertinentes.   
**Utilisation**  
```kotlin
val croppedPixels = cropImage(binarizedPixels)
```
---
**`resizeAndFlattenImage(image: BufferedImage, width: Int, height: Int): List<Int>`** 
**Description** Redimensionne l'image aux dimensions spécifiées et la transforme en un vecteur unidimensionnel.  
**Utilisation**  
```kotlin
val resizedVector = resizeAndFlattenImage(image, 28, 28)
```
---
## Entrainement du model SVM en Python
### 1. Entraînement et optimisation du modèle SVM
L'entraînement du modèle SVM a été réalisé en Python à l'aide de la bibliothèque `scikit-learn`. Voici les principales étapes du pipeline d'entraînement :
- L’importation des bibliothèques (numpy, sklearn, seaborn)
- Le téléchargement des données d’entrainement (mnist_784).
- Prétraitement des données : binarisation des images, réduction des données 700 observations par classe.
- Séparation des données d’entrainement et de test.

1. Entrainement des 10 modèles One-vs-All SVM avec les paramètres suivants :
- Noyau : rbf
- C = 10
- Gamma = 0.001
**Décision finale** : La décision est prise en utilisant la fonction `decision_function()` qui pour chaque valeur calcul le distance de chaque échantillon par rapport aux hyperplan SVM.
Les distances sont utilisées comme une mesure de confiance plus la valeur est grande plus le chiffre appartient à cette classe.

```Python
def predict_ova(models, X):
    decision_values = np.array([model.decision_function(X) for model in models])
    return np.argmax(decision_values, axis=0)
```
2. Calcul de précision en utilisant `accuracy_score`, `confusion_matrix` et optimisation en utilisant `GridSearchCV` pour trouver les meilleurs paramètres, les paramètres testés sont :
- C = [0.1, 1, 10]
- Gamma = [0.01, 0.001, 0.0001]
Le code pour l'entraînement se trouve dans le fichier `Model_SVM.ipynb`.

**Modèle entraîné**
Le modèle est basé sur la méthode **One-vs-All**. Pour chaque classe, un modèle SVM est entraîné pour différencier cette classe des autres. Au total, 10 modèles (un pour chaque chiffre) ont été entraînés.

---
### 2. Extraction des paramètres du modèle
Après l'entraînement, les paramètres nécessaires ont été extraits pour être utilisés dans Java:
- **Vecteurs supports** : Les vecteurs utilisés par le SVM pour définir l'hyperplan.
- **Coefficients** : Les poids associés aux vecteurs supports.
- **Biais** : La constante ajoutée à la fonction de décision. 

Ces paramètres ont été exportés dans des fichiers distincts au format texte pour chaque modèle SVM :
- **`support_vectors_class_<i>.txt`**: Contient les vecteurs supports pour
la classe i.
- **`coefficients_class_<i>.txt`** : Contient les coefficients associés.
- **`bias_class_<i>.txt`** : Contient le biais du modèle

Les fichiers générés sont stockés dans le dossier `dev_ml/modèle SVM/data` pour recréer le modèle SVM en kottlin.Le chemin du fichier qui permet la lecture de ces fichiers `/dev_ml /modèleSVM/ListModeleSVM.kt` .

## Instructions pour l'utilisation
1. **Entraînez le modèle en Python** : Utilisez le notebook fourni (`Model_SVM.ipynb`)
2. **Générez les fichiers nécessaires** : Les paramètres sont automatiquement exportés lors de l'exécution.
3. **Utilisez les fichiers en Java** :
- Implémentez la classe SVMModel en Java (exemple fourni).
- Fournissez les fichiers .txt comme entrée pour reconstruire le modèle.
- oUtilisez la fonction  predict_ova pour tester et effectuer des prédictions


---
## Implémentation du SVM avec le noyau RBF en Java
Après programmation du SVM en Python avec `scikit-learn`, les vecteurs de support, les coefficients, et les biais associés à chaque classe (les 10 classes, de 0 à 9) ont été extraits.

La fonction de décision SVM suivante a ensuite été implémentée en Java :
$$
f(x) = \text{sign} \left( \sum_{i=1}^n \alpha_i y_i K(x_i, x) + b \right)
$$

Où :
- \( x \) : le vecteur image.
- \( x_i \) : les vecteurs supports.
- \( \alpha_i \) : les coefficients associés aux vecteurs supports.
- \( b \) : le biais appris.
- \( \gamma \) : le paramètre du noyau.

Le noyau utilisé dans cet algorithme est le `noyau gaussien (RBF)`, défini par :
$$ K(x_i, x) = \exp \left( -\gamma \|x_i - x\|^2 \right)
$$


Les scores d'appartenance de l'image à chaque classe sont prédits, et la classe ayant le score le plus élevé est sélectionnée comme classe finale prédite.

Pour réaliser cette prédiction, le code a été structuré de la manière suivante :

---
### 1. **Classe `ModeleSVM`**

cette classe représente le modèle SVM avec un noyau RBF. 

#### Attributs principaux :
- **`vecteursSupport`** : Matrice contenant les vecteurs de support de la classe.
- **`coefficients`** : Coefficients \( \alpha_i \) associés aux vecteurs de support.
- **`biais`** : Le biais \( b \).
- **`gamma`** : Paramètre du noyau RBF (défini à 0.001 par défaut).

#### Méthodes principales :
1. **`ModeleSVM(String fichierSupportVecteurs, String fichierCoefficients, String fichierBiais)`**
   - Constructeur qui initialise le modèle en chargeant les paramètres depuis les fichiers en spécifiant le chemin des fichiers.
2. **`chargerVecteursSupports(String fichierSupportVecteurs)`**
   - Charge les vecteurs supports depuis un fichier.
3. **`chargerCoefficients(String fichierCoefficients)`**
   - Charge les coefficients \( \alpha_i \) depuis un fichier.
4. **`chargerBiais(String fichierBiais)`**
   - Charge le biais \( b \) depuis un fichier.
5. **`noyau_rbf(double[] vecteur_image, double[] vecteurs_support)`**
   - Calcule la valeur du noyau RBF entre un vecteur d'entrée et un vecteur support.
6. **`predict_score(double[] vecteur_image)`**
   - Calcule le score pour une classe donnée en fonction du vecteur image.

---
### 2. **Classe `ListModeleSVM`**
Elle contient la liste des 10 modèles SVM des 10 classes.

#### Attributs principaux :
- **`listesDeModeleSVM`** : Une liste contenant 10 instances de `ModeleSVM`.

#### Méthodes principales :
1. **`ListModeleSVM()`**
   - Constructeur qui initialise la liste en chargeant les paramètres de chaque modèle depuis les fichiers correspondants.
2. **`predict_chiffre(double[] vecteur_image)`**
   - Prédit la classe d'un vecteur d'entrée en calculant les scores pour les 10 classes et retourne la classe avec le score maximal.

---
# Interface Utilisateur
L'interface utilisateur a pour objectif de fournir un environnement intuitif et interactif permettant de :  
- Afficher le flux vidéo de la caméra en direct
- Visualiser les prédictions du chiffre manuscrit détecté
- Monter les étapes de traitement de l'image en temps réel

L'interface est structurée autour d'une page d'accueil qui redirige vers une page dédiée à la prédiction via la caméra. Pour cela, deux fragments ont été implémentés et utilisés pour faciliter la navigation. Des classes spécifiques ont été développées pour gérer ces fragments et assurer une expérience utilisateur fluide.

## Navigation
### Au niveau du Layout
1. **`app/src/main/res/layout/fragment_welcome.xml`** 
    Ce fragment configure la page d'accueil de l'application. Elle contient un logo, un message de bienvenue et un bouton "Start" permettant à l'utilisateur de démarrer le processus de reconnaissance des chiffres manuscrits.
2. **`app/src/main/res/layout/fragment_camera.xml`**
    Dans cette partie, la bibliothèque `CameraX` a été installée pour gérer le flux vidéo en direct. Un `TextView` a également été ajouté afin d'afficher la valeur prédite par le modèle SVM.
### Au niveau de l'actvité
1. **Classe `app/src/main/java/fr/mastersd/sime/rabah/manumber/Welcomefragment.kt`** : 
    Cette classe gère la navigation vers le fragment de la caméra lorsque l'utilisateur appuie sur le bouton **Start**.
2. **Classe `app/src/main/java/fr/mastersd/sime/rabah/manumber/Camerafragment.kt`** :
    Cette classe charge le fichier `fragment_camera.xml` à l'aide de `ViewBinding`. Elle contient également la logique nécessaire pour mettre à jour l'interface utilisateur avec les prédictions via la fonction **updateUI**.
### Au niveau de la navigation   
Un fichier de navigation `nav_graph.xml` a été créé dans `app/src/main/res/navigation/nav_graph.xml` pour gérer la navigation entre les deux fragments. Ce fichier définit explicitement l'action permettant de passer du fragment d'accueil (`FragmentWelcome`) au fragment de caméra (`CameraFragment`).
## Résultat attendu 
L'interface finale comprend :
- Une zone principale affichant le flux vidéo
- Une zone encadrée pour les prédictions
- Une zone encadrée pour afficher les étapes de traitement

Cette structure offre une interface claire, simple et professionnelle pour l'utilisateur.
---
## **Détails des Fichiers Clés et de Leur Fonctionnalité**

Cette section documente ma contribution au projet, qui inclut l'intégration du fragment de la caméra (`CameraFragment`), le traitement d'image, et l'intégration avec le modèle SVM pour la prédiction des chiffres manuscrits.

---

## **Fichiers Clés et Leur Fonctionnalité**

### 1. **`CameraFragment`**
**Emplacement** : `fr/mastersd/sime/rabah/manumber/CameraFragment.kt`
**Description** : Ce fichier implémente le fragment principal pour capturer une image via CameraX, appliquer un traitement d'image, et utiliser le modèle SVM pour prédire le chiffre manuscrit.

#### Fonctionnalités Principales :
- **Initialisation de la caméra** : Utilise la classe `CameraManager` pour démarrer la caméra et capturer une image.
- **Traitement d'image** : Charge l'image capturée, la redimensionne, et applique une binarisation.
- **Prédiction SVM** : Convertit les pixels binarisés en vecteur aplati pour les passer au modèle SVM.

#### Extrait de Code Clé :

```kotlin
private fun processCapturedImage(file: File) {
    if (!file.exists()) {
        Toast.makeText(requireContext(), "Erreur : Fichier introuvable.", Toast.LENGTH_SHORT).show()
        return
    }

    coroutineScope.launch {
        val startTime = System.currentTimeMillis()

        // Charger et redimensionner l'image
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

        // Binarisation
        val binarizedPixels = withContext(Dispatchers.Default) {
            ImageUtils.processImageToFlattenedVector(resizedBitmap)
        }
        val binarizedBitmap = ImageUtils.binaryToBitmap(binarizedPixels, 28, 28)
        binding.binaryImageView.setImageBitmap(binarizedBitmap)

        // Prédiction
        val flattenedVector = binarizedPixels.map { it.toDouble() }.toDoubleArray()
        val predictedDigit = withContext(Dispatchers.Default) {
            svmListModel.predictChiffre(flattenedVector)
        }

        binding.predictionTextView.text = "Prédiction : $predictedDigit"
        val elapsedTime = System.currentTimeMillis() - startTime
        Log.d("Performance", "Temps total : $elapsedTime ms")
    }
}
```

---

### 2. `ImageUtils`

**Emplacement** : `fr/mastersd/sime/rabah/manumber/utils/ImageUtils.kt`

**Description** : Ce fichier contient des fonctions utilitaires pour le traitement d'image, notamment la rotation, la redimension, la binarisation, et la transformation en vecteur aplati.

#### Fonctionnalités Clés :

- **Rotation de l'image** : Aligne l'image avant le traitement.
- **Binarisation** : Convertit les pixels en noir et blanc à l'aide du seuil d'Otsu.
- **Flattening** : Transforme l'image en un tableau unidimensionnel.

#### Extrait de Code Clé :

```kotlin
fun processImageToFlattenedVector(image: Bitmap): IntArray {
    val rotatedBitmap = rotateBitmap(image, 90f)
    val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 28, 28, true)

    val threshold = calculateOtsuThreshold(resizedBitmap)
    val binarizedPixels = binarizeImage(resizedBitmap, threshold)

    return flattenBinarizedImage(binarizedPixels)
}
```

---

### 3. `CameraManager`

**Emplacement** : `fr/mastersd/sime/rabah/manumber/utils/CameraManager.kt`
**Description** : Cette classe encapsule la logique de gestion de la caméra, y compris le démarrage de la caméra et la capture des images.

#### Fonctionnalités Clés :

- **Gestion des Permissions** : Vérifie et demande les permissions nécessaires.
- **Capture d'image** : Sauvegarde les images capturées dans un fichier local.

#### Extrait de Code Clé :

```kotlin
fun startCamera(surfaceProvider: Preview.SurfaceProvider) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(context, "Erreur lors de l'initialisation de la caméra.", Toast.LENGTH_SHORT).show()
        }
    }, ContextCompat.getMainExecutor(context))
}
```

---

## **Processus de Fonctionnement**

1. **Capture d'Image** :
   - L'utilisateur clique sur le bouton de capture dans l'interface utilisateur.
   - `CameraManager` enregistre l'image capturée dans un fichier temporaire.

2. **Traitement** :
   - `ImageUtils` effectue les étapes de traitement (rotation, redimension, binarisation).

3. **Prédiction** :
   - Le vecteur aplati est envoyé au modèle SVM via `ListModeleSVM` pour prédire le chiffre.

4. **Affichage** :
   - L'image binarisée et la prédiction sont affichées sur l'interface utilisateur.

---

## **Améliorations Futures**

- **Optimisation des performances** : Réduire le temps de traitement des images.
- **Validation des prédictions** : Ajouter un mécanisme pour détecter et signaler les erreurs de prédiction.
- Améliorer la convergence des modèles en utilisant des techniques d'initialisation en utilisant des méthodes de 
clustering comme k-means pour initialiser les vecteurs supports.
- Appliquer une méthode de réduction de dimensionnalité comme PCA avant l'entraînement pour réduire la complexité.
- Améliorer le traitement d'image pour garantir que les chiffres soient bien récupérés dans leur intégralité et restent connectés, évitant ainsi des erreurs où des chiffres mal segmentés seraient mal interprétés.

---


## Gestion de Projet et Outils Utilisés
### Méthodologie Agile
Le projet a suivi la méthode Agile, avec des sprints et des réunions quotidiennes pour ajuster les priorités et assurer une collaboration continue.

### Outils de Collaboration
- **GitLab** : Gestion de version du code, avec branches et demandes de fusion pour un travail collaboratif fluide.
- **Trello** : Suivi des tâches et de l’avancement du projet, avec des cartes pour chaque fonctionnalité et des priorités claires.
- **Slack** : Communication en temps réel, avec des canaux dédiés aux discussions techniques et à l'intégration de notifications GitLab et Trello.

## Références

- Documentation officielle de CameraX
- Guide Android Developers

---

## Auteur

**Rania BOUZROUD**

**Harouna NIANG**

**Nariman LALOUCHE**

**Vinette Marcy GADEU MONTHE**

**Lotfi Abdelkadir RABAH**

Projet réalisé dans le cadre de **Manumber**.
