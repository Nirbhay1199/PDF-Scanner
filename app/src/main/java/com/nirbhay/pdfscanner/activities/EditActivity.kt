package com.nirbhay.pdfscanner.activities

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.DragEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.nirbhay.pdfscanner.R
import com.nirbhay.pdfscanner.adapter.EditListAdapter
import com.nirbhay.pdfscanner.databinding.ActivityEditBinding
import com.nirbhay.pdfscanner.model.ImagesListViewModel
import com.nirbhay.pdfscanner.model.ImagesListViewModel.Companion.imageUris
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditActivity : AppCompatActivity(), PdfCreationProgressListener {
    private var progressListener: PdfCreationProgressListener? = null
    private lateinit var binding: ActivityEditBinding
    private lateinit var listView: ListView
    private lateinit var imageListViewModel: ImagesListViewModel
    private lateinit var adapter: EditListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = binding.list

        imageListViewModel = ViewModelProvider(this)[ImagesListViewModel::class.java]

        val imageUris = imageUris


        adapter = EditListAdapter(this, R.layout.edit_activity_listview)

        listView.adapter = adapter

        listView.setOnItemLongClickListener { _, view, position, _ ->
            val shadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(null, shadowBuilder, position, 0)
            true
        }

        listView.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> true
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> true
                DragEvent.ACTION_DROP -> {
                    val sourcePosition = event.localState as Int
                    val targetPosition = listView.pointToPosition(event.x.toInt(), event.y.toInt())
                    if (sourcePosition != -1 && targetPosition != -1 && sourcePosition != targetPosition) {
                        val source = imageUris[sourcePosition]
                        imageUris.removeAt(sourcePosition)
                        imageUris.add(targetPosition, source)
                        adapter.notifyDataSetChanged()
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }


        binding.confirmButton.setOnClickListener {
            binding.dialog.visibility = View.VISIBLE
            binding.confirmButton.visibility = View.GONE
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1199)
                }
            }
        }

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        binding.saveBtn.setOnClickListener{
            val fileName = binding.pdfName.text.toString()
            if (fileName.trim() != "") {
                progressListener = this
                binding.dialog.visibility = View.GONE
                binding.progressDialog.visibility = View.VISIBLE
                generatePdfFromImages(fileName)
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }else{
                Toast.makeText(this, "Enter a valid name !", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelBtn.setOnClickListener {
            binding.dialog.visibility = View.GONE
            binding.confirmButton.visibility = View.VISIBLE
        }



    }

    override fun onBackPressed() {
        binding.cautionDialog.visibility = View.VISIBLE
        binding.yes.setOnClickListener {
            imageUris.clear()
            finish()
        }

        binding.no.setOnClickListener {
            binding.cautionDialog.visibility = View.GONE
        }

    }

    private fun generatePdfFromImages(fileName: String) {
        val document = PdfDocument()
        val a4Width = 595
        val a4Height = 842
        val imageUris = imageUris

        imageUris.forEach { imageUri->

            val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(imageUri)))

            val scaleFactor =
                (a4Width.toFloat() / imageBitmap.width).coerceAtMost(a4Height.toFloat() / imageBitmap.height)
            val scaledWidth = (imageBitmap.width * scaleFactor).toInt()
            val scaledHeight = (imageBitmap.height * scaleFactor).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, scaledWidth, scaledHeight, true)

            val pageInfo = PdfDocument.PageInfo.Builder(a4Width, a4Height, 1).create()
            val page = document.startPage(pageInfo)

            val canvas = page.canvas
            val centerX = (canvas.width -  scaledBitmap.width)/2f
            val centerY = (canvas.height - scaledBitmap.height)/2f
            canvas.drawBitmap(scaledBitmap, centerX, centerY, null)
            document.finishPage(page)
            scaledBitmap.recycle()
        }


        val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var appFolder = File(downloadDirectory ,"PDF Scanner")
        if (!appFolder.exists()){
            appFolder.mkdirs()
        }


        val file = File(appFolder,"$fileName.pdf")

        try {
            document.writeTo(FileOutputStream(file))
            document.close()
            progressListener?.onPdfCreated(file)
            imageUris.clear()
            finish()

        } catch (e: IOException) {
            e.printStackTrace()
            imageUris.clear()
            progressListener?.onPdfCreationFailed(e)
        }


    }

    override fun onProgressUpdate(progress: Int) {
        runOnUiThread {
            binding.progressBar.progress = progress
        }
    }

    override fun onPdfCreated(file: File) {
        Toast.makeText(this, "PDF Saved", Toast.LENGTH_SHORT).show()
    }

    override fun onPdfCreationFailed(e: Exception) {
        finish()
        e.printStackTrace()
        Toast.makeText(this, "Something went wrong !",Toast.LENGTH_SHORT).show()
    }


}

interface PdfCreationProgressListener {
    fun onProgressUpdate(progress: Int)
    fun onPdfCreated(file: File)
    fun onPdfCreationFailed(e: Exception)
}
