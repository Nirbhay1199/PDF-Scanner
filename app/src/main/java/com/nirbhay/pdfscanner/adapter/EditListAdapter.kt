package com.nirbhay.pdfscanner.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.nirbhay.pdfscanner.activities.EditActivity
import com.nirbhay.pdfscanner.R
import com.nirbhay.pdfscanner.model.ImagesListViewModel.Companion.imageUris

class EditListAdapter(private val context: Context, resource: Int):
    ArrayAdapter<String>(context, resource)
{
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var editActivity: EditActivity


    override fun getCount(): Int {
        return imageUris.size
    }

    override fun getItem(position: Int): String {
        return imageUris[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: layoutInflater.inflate(R.layout.edit_activity_listview, parent, false)

        val imageView: ImageView = view.findViewById(R.id.view)
        val page: TextView = view.findViewById(R.id.page)
        val delete: ImageView = view.findViewById(R.id.delete)



        val currentItem = getItem(position)
        val pageCount = count
        page.text = "${position + 1} of $pageCount"


        Glide.with(context)
            .load(Uri.parse(currentItem))
            .into(imageView)

        if (context is EditActivity) {
            editActivity = context
        }

        delete.setOnClickListener {
            if (count != 1) {
                imageUris.removeAt(position)
                notifyDataSetChanged()
            } else {
                imageUris.removeAt(position)
                editActivity.finish()
            }
        }

        return view
    }



}