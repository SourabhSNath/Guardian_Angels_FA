package com.guardianangels.football.ui.gallery.addimages

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.databinding.AddGalleryImagesListItemBinding

class AddGalleryImagesAdapter(private val uriList: List<Uri>) : RecyclerView.Adapter<AddGalleryImagesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: AddGalleryImagesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.picture.load(uri)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AddGalleryImagesListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(uriList[position])
    }

    override fun getItemCount() = uriList.size
}