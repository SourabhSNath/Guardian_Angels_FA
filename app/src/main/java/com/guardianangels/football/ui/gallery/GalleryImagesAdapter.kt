package com.guardianangels.football.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.data.Picture
import com.guardianangels.football.databinding.GalleryListItemBinding

class GalleryImagesAdapter(val clickListener: (Picture) -> Unit) : ListAdapter<Picture, GalleryImagesAdapter.ViewHolder>(DiffItem) {

    companion object DiffItem : DiffUtil.ItemCallback<Picture>() {
        override fun areItemsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem == newItem
        }

    }

    inner class ViewHolder(private val binding: GalleryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Picture) {
            item.imageLink?.let { binding.galleryImage.load(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImagesAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GalleryListItemBinding.inflate(layoutInflater, parent, false)
        val viewHolder = ViewHolder(binding)

        binding.root.setOnClickListener {
            val pos = viewHolder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val picture = getItem(pos) as Picture
                clickListener(picture)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: GalleryImagesAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}