package fr.mastersd.sime.rabah.manumber

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import fr.mastersd.sime.rabah.manumber.databinding.FragmentCameraBinding
import fr.mastersd.sime.rabah.manumber.utils.CameraManager
import fr.mastersd.sime.rabah.manumber.viewmodel.CameraViewModelFactory
import java.io.File
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels { CameraViewModelFactory(requireContext()) }
    private lateinit var cameraManager: CameraManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser CameraManager
        cameraManager = CameraManager(
            context = requireContext(),
            executor = Executors.newSingleThreadExecutor()
        ) { file ->
            viewModel.processCapturedImage(file)
        }

        // Observer les LiveData pour afficher les données
        viewModel.binarizedBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            if (bitmap != null) {
                binding.binaryImageView.setImageBitmap(bitmap)
            }
        })

        viewModel.predictedDigit.observe(viewLifecycleOwner, Observer { digit ->
            if (digit != null) {
                binding.predictionTextView.text = "Prédiction : $digit"
            }
        })

        // Démarrer la caméra
        cameraManager.startCamera(binding.cameraPreviewView.surfaceProvider)

        // Capture du bouton
        binding.captureButton.setOnClickListener {
            val outputFile = File(requireContext().externalCacheDir, "captured_image.jpg")
            cameraManager.captureImage(outputFile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
