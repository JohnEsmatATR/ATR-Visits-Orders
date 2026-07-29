package com.akhnaton.foodvisits.ui.home.tickets

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.getFileName

class AttachmentAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    private val items = mutableListOf<Uri>()

    interface OnItemClickListener {
        fun onRemove(item: Uri)
    }

    fun setList(list: List<Uri>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri = items[position]

        holder.tvName.text = if (getFileName(
                holder.itemView.context,
                uri
            ).length <= 12
        ) getFileName(holder.itemView.context, uri)
        else "${getFileName(holder.itemView.context, uri).substring(0, 12)}..."

        holder.ivClose.setOnClickListener {
            listener.onRemove(uri)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivClose: ImageView = itemView.findViewById(R.id.ivClose)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }
}