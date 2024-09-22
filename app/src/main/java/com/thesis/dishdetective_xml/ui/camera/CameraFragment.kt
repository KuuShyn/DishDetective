package com.thesis.dishdetective_xml.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.thesis.dishdetective_xml.BoundingBox
import com.thesis.dishdetective_xml.Constants.LABELS_PATH
import com.thesis.dishdetective_xml.Constants.MODEL_PATH
import com.thesis.dishdetective_xml.Detector
import com.thesis.dishdetective_xml.OverlayView.Companion.getBoundingBoxColor
import com.thesis.dishdetective_xml.R
import com.thesis.dishdetective_xml.databinding.FragmentCameraBinding
import com.thesis.dishdetective_xml.ui.capture.CapturedFragment
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), Detector.DetectorListener {

    private var binding: FragmentCameraBinding? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var detector: Detector? = null

    companion object {
        fun newInstance() = CameraFragment()
        private const val TAG = "CameraFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentCameraBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(requireContext(), MODEL_PATH, LABELS_PATH, this)
            detector?.setup()
        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding?.captureButton?.setOnClickListener {
            capturePhoto()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val rotation = binding?.viewFinder?.display?.rotation ?: Surface.ROTATION_0
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(imageProxy)
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
        )

        detector?.detect(rotatedBitmap)
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    // Rotate the bitmap if needed
                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )

                    // Perform detection and draw bounding boxes
                    detector?.detect(rotatedBitmap)
                    val bitmapWithBoxes = drawBoundingBoxesOnBitmap(
                        rotatedBitmap,
                        binding?.overlay?.getBoundingBoxes() ?: emptyList()
                    )

                    showCapturedFragment(bitmapWithBoxes)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun drawBoundingBoxesOnBitmap(bitmap: Bitmap?, boundingBoxes: List<BoundingBox>): Bitmap {
        if (bitmap == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        val bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapCopy)
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 18F
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
        }

        boundingBoxes.forEach { box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            paint.color = getBoundingBoxColor(requireContext(), box.clsName)
            canvas.drawRect(left, top, right, bottom, paint)

            // Draw label
            val drawableText = box.clsName
            canvas.drawText(drawableText, left, top + 60, textPaint)
        }

        return bitmapCopy
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        binding = null
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.inferenceTime?.text = "${inferenceTime}ms"
            binding?.overlay?.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }

    override fun onEmptyDetect() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.overlay?.clear()
        }
    }

    private fun showCapturedFragment(bitmap: Bitmap) {
        // Hide other views
        binding?.viewFinder?.visibility = View.GONE
        binding?.overlay?.visibility = View.GONE
        binding?.captureButton?.visibility = View.GONE
        binding?.inferenceTime?.visibility = View.GONE

        val boundingBoxes = binding?.overlay?.getBoundingBoxes() ?: emptyList()
        val fragment = CapturedFragment.newInstance(bitmap, boundingBoxes)

        parentFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_up,
                R.anim.slide_out_down,
                R.anim.slide_in_up,
                R.anim.slide_out_down
            )
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
        }
    }
}
