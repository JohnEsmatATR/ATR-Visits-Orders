package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.databinding.ItemSelectedImageBinding
import com.bumptech.glide.Glide

class SelectedImagesAdapter(
    private val onRemoveClick: (position: Int) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    private val images = mutableListOf<Uri>()

    fun setImages(newImages: List<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    fun getImages(): List<Uri> = images.toList()

    override fun getItemCount(): Int = images.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.binding.root.context)
            .load(images[position])
            .centerCrop()
            .into(holder.binding.ivImage)

        holder.binding.ivClose.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onRemoveClick(adapterPosition)
            }
        }
    }

    class ImageViewHolder(
        val binding: ItemSelectedImageBinding
    ) : RecyclerView.ViewHolder(binding.root)
}