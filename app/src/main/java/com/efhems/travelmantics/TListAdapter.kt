package com.efhems.travelmantics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.efhems.travelmantics.model.Model
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FileDownloadTask
import com.google.android.gms.tasks.OnSuccessListener
import java.io.File


class TListAdapter(val onclickListeer: OnclickListeer) : ListAdapter<Model, TListAdapter.ItemViewholder>(DiffCallback()) {


    interface OnclickListeer{
        fun onClick(model: Model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        return ItemViewholder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TListAdapter.ItemViewholder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Model) = with(itemView) {
            val city: AppCompatTextView = this.findViewById(R.id.appCompatTextView5)
            val desc: AppCompatTextView = this.findViewById(R.id.appCompatTextView4)
            val amount: AppCompatTextView = this.findViewById(R.id.appCompatTextView6)
            val image: AppCompatImageView = this.findViewById(R.id.image)
            city.text = item.city
            desc.text = item.desc
            amount.text = item.amount.toString()

            Glide.with(context).load(item.image)
                .apply(RequestOptions.placeholderOf(R.drawable.glide_placeholder))
                .apply(RequestOptions.centerCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(image)

            setOnClickListener {
                onclickListeer.onClick(model = item)
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Model>() {
    override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
        return oldItem.id == newItem.id

    }

    override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
        return oldItem == newItem

    }
}

