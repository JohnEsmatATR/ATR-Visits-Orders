package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersUploadImages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.bumptech.glide.Glide
import java.io.FileNotFoundException

class PromoterImageRecyclerAdapter(
    private var imageList: List<String>,
    private var context: Context?
) : RecyclerView.Adapter<PromoterImageRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = imageList[position]
        try {
            holder.bindView(context!!, image)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.iv_image)


        @Throws(FileNotFoundException::class)
        fun bindView(context: Context, image: String?) {
            Glide.with(context).load(image)
                .into(imageView)
        }
    }

}