package pk.livecaster.app.streaming.capture

import android.content.Context
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCaptureManager(private val context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentLensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageAnalysis: ImageAnalysis? = null
    var onYuvFrameAvailable: ((ByteArray) -> Unit)? = null

    private fun getExecutor(): ExecutorService {
        if (cameraExecutor.isShutdown || cameraExecutor.isTerminated) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        return cameraExecutor
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraReady: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCameraPreview(lifecycleOwner, previewView)
            onCameraReady()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startCameraPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val analysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        analysis.setAnalyzer(getExecutor()) { imageProxy ->
            try {
                val callback = onYuvFrameAvailable
                if (callback != null) {
                    val nv21 = yuv420ToNv21(imageProxy)
                    callback(nv21)
                }
            } catch (e: Exception) {
                android.util.Log.e("CameraCaptureManager", "Error processing camera frame", e)
            } finally {
                imageProxy.close()
            }
        }
        imageAnalysis = analysis

        try {
            provider.unbindAll()
            val hasBack = try { provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) } catch (_: Exception) { false }
            val hasFront = try { provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) } catch (_: Exception) { false }

            val selector = when {
                currentLensFacing == CameraSelector.LENS_FACING_BACK && hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                hasFront -> {
                    currentLensFacing = CameraSelector.LENS_FACING_FRONT
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }
                hasBack -> {
                    currentLensFacing = CameraSelector.LENS_FACING_BACK
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }
            camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
        } catch (e: Exception) {
            android.util.Log.e("CameraCaptureManager", "Error binding camera preview", e)
        }
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 2
        val nv21 = ByteArray(ySize + uvSize)

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val yRowStride = image.planes[0].rowStride
        val yPixelStride = image.planes[0].pixelStride
        var pos = 0

        if (yRowStride == width && yPixelStride == 1) {
            yBuffer.get(nv21, 0, ySize)
            pos = ySize
        } else {
            for (row in 0 until height) {
                yBuffer.position(row * yRowStride)
                yBuffer.get(nv21, pos, width)
                pos += width
            }
        }

        val uRowStride = image.planes[1].rowStride
        val uPixelStride = image.planes[1].pixelStride
        val vRowStride = image.planes[2].rowStride
        val vPixelStride = image.planes[2].pixelStride

        val uvHeight = height / 2
        val uvWidth = width / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * uRowStride + col * uPixelStride
                nv21[pos++] = vBuffer.get(vIndex)
                nv21[pos++] = uBuffer.get(uIndex)
            }
        }
        return nv21
    }

    fun switchCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView): Boolean {
        val provider = cameraProvider
        if (provider != null) {
            val targetFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            val targetSelector = if (targetFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            val hasTarget = try { provider.hasCamera(targetSelector) } catch (_: Exception) { false }
            if (hasTarget) {
                currentLensFacing = targetFacing
                startCameraPreview(lifecycleOwner, previewView)
            }
        }
        return currentLensFacing == CameraSelector.LENS_FACING_FRONT
    }

    fun toggleTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    fun unbind() {
        cameraProvider?.unbindAll()
        camera = null
    }

    fun release() {
        unbind()
        cameraExecutor.shutdown()
    }
}
