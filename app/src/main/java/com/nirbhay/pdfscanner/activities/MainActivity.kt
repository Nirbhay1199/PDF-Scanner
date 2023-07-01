package com.nirbhay.pdfscanner.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.nirbhay.pdfscanner.R
import com.nirbhay.pdfscanner.adapter.ListAdapter
import com.nirbhay.pdfscanner.databinding.ActivityMainBinding
import com.nirbhay.pdfscanner.model.ImagesListViewModel
import com.nirbhay.pdfscanner.model.ImagesListViewModel.Companion.imageUris
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var list: ListView
    private lateinit var appFolder: File
    private lateinit var imagesListViewModel: ImagesListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagesListViewModel = ViewModelProvider(this)[ImagesListViewModel::class.java]
        imagesListViewModel.setList()

        list = binding.list

        binding.galleryBtn.setOnClickListener {
            openGallery()
        }

        binding.cameraBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        appFolder = File(downloadDirectory ,"PDF Scanner")
        if (!appFolder.exists()){
            appFolder.mkdirs()
        }

        showPDFFiles(appFolder)


    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        launcher.launch(intent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                val clipData = data?.clipData

                if (clipData != null){
                    for (i in 0 until clipData.itemCount){
                        val uri = clipData.getItemAt(i).uri
                        imageUris.add(uri.toString())
                    }
                }else if (data?.data !=null){
                    val uri = data.data
                    imageUris.add(uri.toString())
                }

                val editActivity = Intent(this, EditActivity::class.java)
                startActivity(editActivity)
            }

        }


    private fun showPDFFiles(directory: File) {

        val pdfFiles = getPDFFiles(directory)

        if (pdfFiles.isEmpty()) {
            binding.text.visibility = View.VISIBLE
            return
        }else{
            binding.text.visibility = View.GONE
            val adapter = ListAdapter(this, R.layout.listview, pdfFiles)
            list.adapter = adapter
        }

    }

    private fun getPDFFiles(directory: File): List<String> {
        val pdfFiles: MutableList<String> = ArrayList()
        val files = directory.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    pdfFiles.addAll(getPDFFiles(file))
                } else if (file.name.endsWith(".pdf")) {
                    pdfFiles.add(file.path)
                }
            }
        }

        return pdfFiles
    }


    override fun onResume() {
        super.onResume()
        showPDFFiles(appFolder)
    }


}