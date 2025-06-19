package com.boattrip.boattrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class PhotoAdapter(private var photos: List<PhotoItem>) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_photo)
        val dateTextView: TextView = itemView.findViewById(R.id.text_view_date)
        val categoryTextView: TextView = itemView.findViewById(R.id.text_view_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        
        // Glide를 사용하여 이미지 로드
        Glide.with(holder.itemView.context)
            .load(photo.uri)
            .centerCrop()
            .into(holder.imageView)
        
        // 날짜 포맷팅 (dateTaken은 이미 밀리초 단위)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = Date(photo.dateTaken)
        holder.dateTextView.text = dateFormat.format(date)
        
        // 카테고리 표시 및 배경색 설정
        val (categoryText, backgroundRes) = when (photo.category) {
            PhotoCategory.ALL -> "전체" to R.drawable.rounded_square
            PhotoCategory.PERSON -> "👤 사람" to R.drawable.category_badge_person
            PhotoCategory.FOOD -> "🍽️ 음식" to R.drawable.category_badge_food
            PhotoCategory.OUTDOOR -> "🌍 아웃도어" to R.drawable.category_badge_outdoor
            PhotoCategory.RECEIPT -> "🧾 영수증" to R.drawable.category_badge_receipt
        }
        
        holder.categoryTextView.text = categoryText
        holder.categoryTextView.background = ContextCompat.getDrawable(
            holder.itemView.context,
            backgroundRes
        )
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(newPhotos: List<PhotoItem>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
} 