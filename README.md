# Manumber: Handwritten Digit Recognition via CameraX and SVM

## 1. Project Overview

This project, realized as an **Undergraduate Final Year Project (FYP)**, implements a real-time system for **Handwritten Digit Recognition**. The application uses the device's camera to capture a live video stream, processes the captured image, and utilizes a **Support Vector Machine (SVM)** model, re-implemented in Kotlin, to predict the recognized digit.

### 1.1 Key Technical Goals

* Integrate and configure the CameraX library for real-time video stream capture.
* Develop an efficient image processing pipeline to clean and prepare captured images (binarization, resizing).
* Train and optimize a **One-vs-All SVM** model in Python, and accurately port its parameters to a Kotlin implementation.
* Design an intuitive Android interface using Fragments and `ViewBinding` for a seamless user experience.

## 2. Technical Architecture and Stack

The solution is divided into three main components: the Android Frontend, the Image Processing pipeline, and the Kotlin-based SVM Model.

### 2.1 Technology Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Mobile OS** | **Android** | Target platform for the application. |
| **Language** | **Kotlin** | Primary language for Android development and SVM implementation. |
| **Camera** | **CameraX** | Library for reliable, easy-to-use camera access and lifecycle management. |
| **ML Model** | **Support Vector Machine (SVM) / RBF Kernel** | Core classification model for digit prediction. |
| **ML Training** | **Python (scikit-learn)** | Used for model training, optimization, and parameter extraction. |
| **Dependencies** | `androidx.camera.*`, `Coroutines`, `ViewBinding` | Core Android and utility libraries. |

## 3. Core Functionalities and Architecture

### 3.1 User Interface (UI)

The UI provides an intuitive and interactive environment designed to: display the live camera feed, visualize the predicted handwritten digit, and showcase the real-time image processing steps.

#### Layout Structure
* **`fragment_welcome.xml`**: The application's landing page, featuring a welcome message, a logo, and a "Start" button to initiate the recognition process.
* **`fragment_camera.xml`**: The main interface for prediction, containing the **`PreviewView`** (CameraX feed), a **`TextView`** for the predicted digit, and views to display processing results (e.g., the binarized image).

#### Navigation and Flow
* **`nav_graph.xml`**: Defines the navigation flow between `WelcomeFragment` and `CameraFragment`.
* **`WelcomeFragment.kt`**: Manages navigation to the camera fragment upon pressing the "Start" button.
* **`CameraFragment.kt`**: Main controller for the prediction view. It loads `fragment_camera.xml`, initializes the camera, triggers image processing, and calls the `updateUI` logic to display predictions.

---

### 3.2 Image Processing Pipeline

The `Image_Processing.kt` (and its improved version, `Centralization.kt`) contains the image manipulation logic essential for preparing raw camera frames for the SVM model.

#### Pipeline Steps

| File / Function | Description | Key Focus |
| :--- | :--- | :--- |
| **`ImageUtils.kt`** | Utility functions for rotation, resizing, and vector flattening. | Prepare image for binarization and vectorization. |
| **`calculateOtsuThreshold`** | Computes the optimal binarization threshold using Otsu's method. | Adaptive thresholding. |
| **`binarizeImage`** | Converts the image to a binary (black and white) format. | Noise reduction and foreground/background separation. |
| **`cropImage` (Centralization)** | Crops the binarized image to focus only on the content (the digit), removing empty margins. | Isolation of the Region of Interest (ROI). |
| **`flattenBinarizedImage`** | Transforms the final 28x28 binarized image into a 784-dimensional feature vector. | Input preparation for the SVM model. |

---

### 3.3 SVM Model Implementation in Kotlin

The classification model is a **One-vs-All Support Vector Machine (O-v-A SVM)** using the **Radial Basis Function (RBF) kernel**.

#### Training (Python - `Model_SVM.ipynb`)
1.  **Data Preparation**: MNIST dataset loaded, images binarized, and data volume reduced (700 observations per class).
2.  **Model Training**: Ten O-v-A SVM models are trained with optimized hyperparameters: **Kernel: RBF**, **C = 10**, and **Gamma = 0.001**.
3.  **Parameter Extraction**: Support vectors, coefficients, and biases are exported from `scikit-learn` into separate text files (`support_vectors_class_i.txt`, etc.).

#### Kotlin Implementation

1.  **`SVMModel.kt` Class**:
    * Represents a single SVM model (one class).
    * Implements the **RBF kernel** function: $K(x_i, x) = \exp \left( -\gamma \|x_i - x\|^2 \right)$.
    * Implements the **decision function**: $f(x) = \sum_{i=1}^n \alpha_i K(x_i, x) + b$.

2.  **`SVMListModel.kt` Class**:
    * Holds the list of all 10 `SVMModel` instances.
    * **`predictChiffre(vector: DoubleArray)`**: Predicts the final digit by calculating the score for all 10 classes and selecting the one with the maximum score (highest confidence).

---

## 4. Getting Started

### 4.1 Prerequisites

* **Android Studio** (Arctic Fox or newer)
* **Kotlin** and **Gradle** configured.
* Android device or emulator with camera support.
* **Python** environment (for model training).

### 4.2 Project Setup

1.  **Add CameraX Dependencies (`app/build.gradle`)**:
    ```gradle
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    ```

2.  **Add Permissions (`AndroidManifest.xml`)**:
    ```xml
    <uses-feature android:name="android.hardware.camera.any" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    ```

### 4.3 Installation and Running

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/lotfi8rbh/Ma-Number
    cd MediCall
    ```
2.  **Train and Export Model Parameters (Python)**
    * Execute the steps in `Model_SVM.ipynb` to train the models and automatically export the parameters to the required `.txt` files in the `dev_ml/modèle SVM/data` directory.
3.  **Run the Android Application**
    * Open the project in Android Studio.
    * Build and run the app on a connected device or emulator. The app will request camera permissions and then display the welcome screen.

---

## 5. Project Management and Tools

The project adopted the **Agile methodology**, featuring regular sprints and daily meetings to ensure continuous collaboration and priority alignment.

* **Version Control**: **GitLab** was used for source code management, branch control, and merge requests.
* **Task Tracking**: **Trello** facilitated task tracking and progress monitoring with dedicated cards for features and clear priorities.
* **Communication**: **Slack** served as the real-time communication platform, integrated with GitLab and Trello for notifications.

---

## 6. Future Enhancements

* **Performance Optimization**: Reduce image processing time for lower latency predictions.
* **Model Improvement**: Enhance model convergence by using clustering techniques (e.g., K-Means) for support vector initialization.
* **Image Preprocessing**: Improve image segmentation logic to ensure handwritten digits are fully captured and connected, preventing misinterpretation.
* **Dimensionality Reduction**: Apply PCA (Principal Component Analysis) before training to reduce complexity.
* **Advanced UI**: Implement a `RecyclerView` to display real-time visualization of image preprocessing steps.

---

## 7. Authorship

This project was developed by the following team members as part of the **Manumber** initiative:

* **Rania BOUZROUD**
* **Harouna NIANG**
* **Nariman LALOUCHE**
* **Vinette Marcy GADEU MONTHE**
* **Lotfi Abdelkadir RABAH**

