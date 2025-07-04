package com.boattrip.boattrip

import android.content.Intent
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
            PhotoCategory.ALL -> "전체" to R.drawable.category_badge_all
            PhotoCategory.PERSON -> "👤 사람" to R.drawable.category_badge_person
            PhotoCategory.FOOD -> "🍽️ 음식" to R.drawable.category_badge_food
            PhotoCategory.CITY -> "🏙️ 도시" to R.drawable.category_badge_outdoor
            PhotoCategory.NATURE -> "🌲 자연" to R.drawable.category_badge_outdoor
            PhotoCategory.RECEIPT -> "🧾 영수증" to R.drawable.category_badge_receipt
        }
        
        holder.categoryTextView.text = categoryText

        holder.categoryTextView.background = ContextCompat.getDrawable(
            holder.itemView.context,
            backgroundRes
        )

        // 사진 클릭 시 외부 앱에서 열기
        holder.imageView.setOnClickListener {
            openImageInExternalApp(holder.itemView.context, photo)
        }

        // 전체 아이템 클릭 시에도 외부 앱에서 열기
        holder.itemView.setOnClickListener {
            openImageInExternalApp(holder.itemView.context, photo)
        }
    }

    private fun openImageInExternalApp(context: android.content.Context, photo: PhotoItem) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(photo.uri, "image/*")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            // 이미지를 처리할 수 있는 앱이 있는지 확인
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // 처리할 수 있는 앱이 없을 경우 갤러리 앱으로 직접 열기 시도
                val galleryIntent = Intent(Intent.ACTION_VIEW, photo.uri).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(galleryIntent)
            }
        } catch (e: Exception) {
            // 에러 발생 시 로그 출력 (실제 앱에서는 Toast 메시지 등으로 사용자에게 알림)
            android.util.Log.e("PhotoAdapter", "Failed to open image in external app", e)
        }
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(newPhotos: List<PhotoItem>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
} 