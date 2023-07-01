package com.nirbhay.pdfscanner.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import com.nirbhay.pdfscanner.R
import java.io.File

class ListAdapter(private val context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: layoutInflater.inflate(R.layout.listview, parent, false)

        val fileNameTextView: TextView = view.findViewById(R.id.nameView)
        val share: ImageView = view.findViewById(R.id.share)

        val fileName = getItem(position)
        fileNameTextView.text = fileName!!.substring(fileName.lastIndexOf("/")+1, fileName.length)


        fileNameTextView.setOnClickListener {
            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", File(getItem(position)!!))
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }


        share.setOnClickListener {
            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", File(getItem(position)!!))
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }

        return view
    }
}