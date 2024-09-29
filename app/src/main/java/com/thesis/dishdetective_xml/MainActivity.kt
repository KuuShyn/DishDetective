package com.thesis.dishdetective_xml

import android.Manifest
import android.content.Intent
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
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thesis.dishdetective_xml.Constants.LABELS_PATH
import com.thesis.dishdetective_xml.Constants.MODEL_PATH
import com.thesis.dishdetective_xml.databinding.ActivityMainBinding
import com.thesis.dishdetective_xml.ui.capture.CapturedFragment
import com.thesis.dishdetective_xml.ui.profile.ProfileDetailsFragment
import com.thesis.dishdetective_xml.ui.recipe_analyzer.RecipeAnalyzerFragment
import com.thesis.dishdetective_xml.ui.recipe_analyzer.RecipeHistoryFragment
import com.thesis.dishdetective_xml.util.Debounce.setDebounceClickListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), Detector.DetectorListener,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        fetchFoodData()
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this) {
                toast(it)
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
            supportFragmentManager.addOnBackStackChangedListener {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment == null) {
                    // No fragment in the container, meaning we're back to MainActivity's default view
                    startCamera()
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        bindListeners()
        binding.isGpu.visibility = View.GONE

        pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val uri = data?.data
                uri?.let {
                    val inputStream = contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    // Detect bounding boxes on the selected image
                    detector?.detect(bitmap)

                    // Draw bounding boxes on the bitmap
                    val bitmapWithBoxes = drawBoundingBoxesOnBitmap(bitmap, binding.overlay.getBoundingBoxes())

                    // Pass the image with bounding boxes to CapturedFragment
                    val capturedFragment = CapturedFragment.newInstance(bitmapWithBoxes, binding.overlay.getBoundingBoxes())
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, capturedFragment)
                        .addToBackStack(null)
                        .commit()

                    stopCamera()
                    binding.usePhotoButton.isEnabled = true
                }
            }
        }

        binding.navView.setNavigationItemSelectedListener(this)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
        binding.viewFinder.visibility = View.VISIBLE
        binding.captureButton.visibility = View.VISIBLE
        binding.inferenceTime.visibility = View.VISIBLE
        binding.overlay.visibility = View.VISIBLE
        binding.usePhotoButton.visibility = View.VISIBLE
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        binding.viewFinder.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.inferenceTime.visibility = View.GONE
        binding.isGpu.visibility = View.GONE
        binding.overlay.visibility = View.GONE
        binding.usePhotoButton.visibility = View.GONE

    }

    private fun bindListeners() {
        binding.apply {
            captureButton.setDebounceClickListener {
                captureButton.isEnabled = false // Disable the button

                val imageCapture = imageCapture ?: return@setDebounceClickListener
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(this@MainActivity),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            val bitmap = imageProxyToBitmap(imageProxy)

                            // Apply rotation and mirroring if necessary
                            val matrix = Matrix().apply {
                                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                                if (isFrontCamera) {
                                    postScale(-1F, 1F)
                                }
                            }

                            val rotatedBitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                            )

                            detector?.detect(rotatedBitmap)

                            val bitmapWithBoxes = drawBoundingBoxesOnBitmap(
                                bitmap,
                                binding.overlay.getBoundingBoxes()
                            )

                            // Pass the image and adjusted bounding boxes to CapturedFragment
                            val capturedFragment = CapturedFragment.newInstance(
                                bitmapWithBoxes,
                                binding.overlay.getBoundingBoxes()
                            )
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, capturedFragment)
                                .addToBackStack(null)
                                .commit()

                            // Close imageProxy after processing
                            imageProxy.close()
                            stopCamera()
                            captureButton.isEnabled = true // Re-enable the button
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                            Toast.makeText(baseContext, "Capture failed", Toast.LENGTH_SHORT).show()
                            captureButton.isEnabled = true // Re-enable the button
                        }
                    }
                )
            }

            usePhotoButton.setDebounceClickListener {
                usePhotoButton.isEnabled = false // Disable the button

                val intent = Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                pickPhotoLauncher.launch(intent)
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
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 18F
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = 120f
        }
        val textBackgroundPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 120f
        }
        val bounds = Rect()

        val boundingRectTextPadding = 16
        boundingBoxes.forEach { box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            canvas.drawRect(left, top, right, bottom, paint)
            val drawableText = box.clsName
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + boundingRectTextPadding,
                top + textHeight + boundingRectTextPadding,
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Remove all fragments and go back to the default MainActivity view
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                startCamera() // Restart the camera when returning to home
            }

            R.id.nav_profile -> {
                val profileFragment = ProfileDetailsFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit()
                stopCamera() // Stop the camera when navigating to the fragment
            }

            R.id.nav_recipe_analyzer -> {
                val recipeAnalyzerFragment = RecipeAnalyzerFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, recipeAnalyzerFragment)
                    .addToBackStack(null)
                    .commit()
                stopCamera() // Stop the camera when navigating to the fragment
            }

            R.id.nav_recipe_history -> {
                val recipeHistoryFragment = RecipeHistoryFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, recipeHistoryFragment)
                    .addToBackStack(null)
                    .commit()
                stopCamera() // Stop the camera when navigating to the fragment
            }

            R.id.nav_signout -> {
                stopCamera() // Stop the camera when signing out
                firebaseAuth.signOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun fetchFoodData() {

        FoodRepository.fetchAllFoods(
            onComplete = { foodList ->
                Log.d("Firebase RDB Cache", "Total food items: ${foodList.size}")
                // Handle the retrieved food list here
            },
            onError = { errorMessage ->
                Log.e("Firebase Error", errorMessage)
                // Show a user-friendly error message (e.g., Toast)
            }
        )

        FoodRepository.fetchAllDishes(
            onComplete = { dishList ->
                Log.d("Firebase RDB Cache", "Total dish items: ${dishList.size}")
                // Handle the retrieved dish list here
            },
            onError = { errorMessage ->
                Log.e("Firebase Error", errorMessage)
                // Show a user-friendly error message (e.g., Toast)
            }
        )
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
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        // Set up ImageCapture with appropriate settings
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .setFlashMode(FLASH_MODE_OFF) // Set flash mode if needed
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
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
                imageCapture,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
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

    private fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (allPermissionsGranted()) {
            if (fragment == null) {
                startCamera() // Only start the camera if no fragments are displayed
            }
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
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
}