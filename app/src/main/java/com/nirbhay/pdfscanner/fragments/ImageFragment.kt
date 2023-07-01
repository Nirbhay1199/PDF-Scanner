package com.nirbhay.pdfscanner.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.nirbhay.pdfscanner.R
import com.nirbhay.pdfscanner.activities.CameraActivity
import com.nirbhay.pdfscanner.activities.EditActivity
import com.nirbhay.pdfscanner.model.ImagesListViewModel.Companion.imageUris


class ImageFragment : Fragment(R.layout.fragment_image) {

    companion object {
        fun newInstance(data: Uri): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString("uri", data.toString())
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraActivity = requireActivity() as CameraActivity
        val takeMore : TextView = view.findViewById(R.id.takeMore)
        val createPdf: TextView = view.findViewById(R.id.createPdf)
        val capturedImg: ImageView = view.findViewById(R.id.capturedImg)


        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navigateBack(cameraActivity)

            }
        }
        cameraActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val uri = arguments?.getString("uri")!!

        Glide.with(requireContext())
            .load(Uri.parse(uri))
            .into(capturedImg)

        takeMore.setOnClickListener {
            imageUris.add(uri)
            navigateBack(cameraActivity)
            loadLastImage(cameraActivity)
        }

        createPdf.setOnClickListener {
            imageUris.add(uri)
            cameraActivity.finish()
            val intent = Intent(requireContext(), EditActivity::class.java)
            startActivity(intent)
        }

        cameraActivity.binding.removeImage.setOnClickListener {
            imageUris.remove(uri)
            navigateBack(cameraActivity)
            loadLastImage(cameraActivity)
        }


    }

    private fun loadLastImage(cameraActivity: CameraActivity){
        cameraActivity.binding.lastImg.visibility = View.VISIBLE
        cameraActivity.binding.imageCount.visibility = View.VISIBLE
        if (imageUris.isNotEmpty()) {
            Glide.with(requireContext())
                .load(Uri.parse(imageUris.last()))
                .into(cameraActivity.binding.lastImg)

            cameraActivity.binding.imageCount.text = imageUris.size.toString()
        }
    }

    private fun navigateBack(cameraActivity: CameraActivity) {
        requireActivity().supportFragmentManager.popBackStack()
        cameraActivity.binding.display.visibility = View.VISIBLE
        cameraActivity.binding.l4.visibility = View.VISIBLE
        cameraActivity.binding.cdv.visibility = View.VISIBLE
        cameraActivity.binding.removeImage.visibility = View.GONE
    }



}