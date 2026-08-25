package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.databinding.ItemAddImageBinding
import com.akhnaton.foodvisits.databinding.ItemSelectedImageBinding
import com.bumptech.glide.Glide

class SelectedImagesAdapter(
    private val onAddMoreClick: () -> Unit,
    private val onRemoveClick: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_IMAGE = 1
    }

    private val images = mutableListOf<Uri>()

    fun setImages(newImages: List<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    fun getImages(): List<Uri> {
        return images.toList()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_ADD
        } else {
            TYPE_IMAGE
        }
    }

    override fun getItemCount(): Int {
        // +1 because position 0 is "Add More"
        return images.size + 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_ADD) {

            val binding = ItemAddImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            AddImageViewHolder(binding)

        } else {

            val binding = ItemSelectedImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            ImageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        if (holder is AddImageViewHolder) {

            holder.binding.root.setOnClickListener {
                onAddMoreClick()
            }

        } else if (holder is ImageViewHolder) {

            // Position 0 = Add More
            // Therefore image position = RecyclerView position - 1

            val image = images[position - 1]

            Glide.with(holder.binding.root.context)
                .load(image)
                .centerCrop()
                .into(holder.binding.ivImage)

            holder.binding.ivClose.setOnClickListener {

                val adapterPosition = holder.adapterPosition

                if (adapterPosition != RecyclerView.NO_POSITION) {

                    val imagePosition = adapterPosition - 1

                    if (imagePosition in images.indices) {
                        onRemoveClick(imagePosition)
                    }
                }
            }
        }
    }

    class AddImageViewHolder(
        val binding: ItemAddImageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class ImageViewHolder(
        val binding: ItemSelectedImageBinding
    ) : RecyclerView.ViewHolder(binding.root)
}