package com.thesis.dishdetective_xml

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
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.thesis.dishdetective_xml.Constants.LABELS_PATH
import com.thesis.dishdetective_xml.Constants.MODEL_PATH
import com.thesis.dishdetective_xml.OverlayView.Companion.getBoundingBoxColor
import java.util.concurrent.ExecutorService
import com.thesis.dishdetective_xml.databinding.ActivityMainBinding
import com.thesis.dishdetective_xml.ui.capture.CapturedFragment
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    internal lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

//        binding.viewFinder.visibility = View.GONE
        binding.isGpu.visibility = View.GONE
//        binding.inferenceTime.visibility = View.GONE
//        binding.captureButton.visibility = View.GONE

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
            detector?.setup()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        bindListeners()
        loadUserProfile()

    }

    private fun bindListeners() {
        binding.apply {
//            is gpu
            isGpu.setOnCheckedChangeListener { buttonView, isChecked ->
                cameraExecutor.submit {
                    detector?.setup(isGpu = isChecked)
                }
                if (isChecked) {
                    buttonView.setBackgroundColor(
                        ContextCompat.getColor(
                            baseContext,
                            R.color.orange
                        )
                    )
                } else {
                    buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.gray))
                }
            }
            // Capture Button Listener
            captureButton.setOnClickListener {
                val imageCapture = imageCapture ?: return@setOnClickListener

                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(this@MainActivity),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            var bitmap = imageProxyToBitmap(image)
                            image.close()
                            // Create a matrix and rotate it
                            val matrix = Matrix().apply {
                                postRotate(image.imageInfo.rotationDegrees.toFloat())
                            }
//
                            // Create a new bitmap with the rotated matrix
                            bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )

                            detector?.detect(bitmap)

                            // Draw bounding boxes on the bitmap
                            val bitmapWithBoxes = drawBoundingBoxesOnBitmap(
                                bitmap,
                                binding.overlay.getBoundingBoxes()
                            )

                            // Now you can use the bitmap
                            showCapturedFragment(bitmapWithBoxes)

                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                        }
                    }
                )
            }
        }
    }

    private fun drawBoundingBoxesOnBitmap(
        bitmap: Bitmap?,
        boundingBoxes: List<BoundingBox>
    ): Bitmap {
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
            style = Paint.Style.FILL
            textSize = 60f
        }

        val textBackgroundPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 60f
        }

        val bounds = Rect()

        boundingBoxes.forEach { box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            paint.color = getBoundingBoxColor(this, box.clsName)

            canvas.drawRect(left, top, right, bottom, paint)

            val drawableText = box.clsName
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
        binding.viewFinder.visibility = View.VISIBLE
    }

    private fun bindCameraUseCases() {
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build()
            )
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageCapture = ImageCapture.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build()
            )

            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

            .setTargetRotation(rotation)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            detector?.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    private fun loadUserProfile() {
        // This is a mock setup. In a real scenario, you might load this from a database or API.
        val userProfile = UserPro(
            allergies = listOf("pork_adobo"), // Example allergy
            diabeticSafeFoods = listOf("chicken_adobo") // Example diabetic-safe food
        )
        UserProfileManager.setUserProfile(userProfile)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.CAMERA] == true) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
//        }
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            binding.overlay.clear()
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            "${inferenceTime}ms".also { binding.inferenceTime.text = it }
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }


    private fun showCapturedFragment(bitmap: Bitmap) {
        // Hide other views
        binding.viewFinder.visibility = View.GONE
        binding.overlay.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.inferenceTime.visibility = View.GONE

        // Add other views you want to hide here

        // Get the bounding boxes
        val boundingBoxes = binding.overlay.getBoundingBoxes()

        val fragment = CapturedFragment.newInstance(bitmap, boundingBoxes)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_up,
            R.anim.slide_out_down,
            R.anim.slide_in_up,
            R.anim.slide_out_down
        )
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()


    }
}