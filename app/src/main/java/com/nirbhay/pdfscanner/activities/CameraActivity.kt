package com.nirbhay.pdfscanner.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nirbhay.pdfscanner.R
import com.nirbhay.pdfscanner.databinding.ActivityCameraBinding
import com.nirbhay.pdfscanner.fragments.ImageFragment
import java.io.File

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()

        if (cameraPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                1199
            )
        }

        binding.shutter.setOnClickListener {
            takePhoto()
        }

        binding.close.setOnClickListener {
            finish()
        }

        if (binding.shutter.visibility == View.VISIBLE){
            binding.removeImage.visibility = View.GONE
        }
    }

    private fun getOutputDirectory(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(
                        binding.display.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )
            }catch (e: Exception){
                Log.d("startCam", "startCamera: fail")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(){

        val imageCapture = imageCapture?: return

        val photoFile = File.createTempFile("new_img",".jpg",outputDirectory)

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object: ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val image: File = photoFile

                    val uri = Uri.fromFile(image)


                    val imageFragment = ImageFragment.newInstance(uri)

                    binding.display.visibility = View.GONE
                    binding.removeImage.visibility = View.VISIBLE

                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout2, imageFragment)
                        addToBackStack("tag")
                        commit()
                    }

                    binding.l4.visibility = View.GONE
                    binding.cdv.visibility = View.GONE

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("imgCapErr",
                        "onError: ${exception.message}",
                        exception)
                }

            }

        )
    }




    private fun cameraPermission() =
        arrayOf(Manifest.permission.CAMERA).all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1199){
            if (cameraPermission()){
                startCamera()
            }else{
                Toast.makeText(this@CameraActivity, "Camera Permission Not Granted !", Toast.LENGTH_SHORT).show()
            }
        }
    }

}