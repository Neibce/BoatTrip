package com.boattrip.boattrip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageClassificationHelper(private val context: Context) {

    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
        ImageLabeling.getClient(options)
    }

    suspend fun classifyImage(imageUri: Uri): PhotoCategory = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmapFromUri(imageUri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            val labels = suspendCancellableCoroutine { continuation ->
                imageLabeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        continuation.resume(labels)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            Log.d("ImageClassification", "classified: ${labels.map { it.text }}")
            val category = determineCategory(labels.map {
                Pair<String, Float>(
                    it.text.lowercase(),
                    it.confidence
                )
            })

            Log.d("ImageClassification", "classified: $category")
            Log.d(
                "ImageClassification",
                "Labels: ${labels.map { "${it.text}: ${it.confidence}" }}"
            )

            category
        } catch (e: Exception) {
            Log.e("ImageClassification", "Error")
            PhotoCategory.ALL
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalArgumentException("Cannot load bitmap from URI: $uri")
    }

    private fun determineCategory(labels: List<Pair<String, Float>>): PhotoCategory {
        // 사람 관련 키워드
        val personKeywords = listOf(
            "person", "human", "people", "man", "woman", "child", "baby", "face",
            "smile", "portrait", "selfie", "group", "family", "wedding", "party",
            "jacket", "outerwear", "presentation"
        )

        // 음식 관련 키워드
        val foodKeywords = listOf(
            "food", "meal", "dish", "plate", "bowl", "cup", "drink", "beverage",
            "bread", "cake", "dessert", "fruit", "vegetable", "meat", "fish",
            "pizza", "pasta", "soup", "salad", "sandwich", "burger", "noodle",
            "rice", "coffee", "tea", "wine", "beer", "juice", "cooking", "kitchen",
            "restaurant", "dining", "breakfast", "lunch", "dinner"
        )

        // 도시 관련 키워드
        val cityKeywords = listOf(
            "building", "architecture", "city", "urban", "street", "road",
            "bridge", "tower", "skyscraper", "house", "home", "window", "door",
            "car", "vehicle", "bus", "train", "subway", "traffic", "sign",
            "shop", "store", "cafe", "hotel", "office", "sidewalk", "intersection",
            "downtown", "highway", "apartment", "construction", "mall", "airport"
        )

        // 자연 관련 키워드
        val natureKeywords = listOf(
            "landscape", "mountain", "hill", "forest", "tree", "grass", "field",
            "sky", "cloud", "sunset", "sunrise", "beach", "ocean", "sea", "lake",
            "river", "water", "nature", "outdoor", "scenery", "valley", "cliff",
            "flower", "plant", "garden", "park", "wildlife", "animal", "bird",
            "rock", "stone", "desert", "snow", "ice", "pond", "stream", "waterfall"
        )

        // 영수증/문서 관련 키워드
        val receiptKeywords = listOf(
            "text", "document", "paper", "receipt", "bill", "invoice", "ticket",
            "menu", "sign", "poster", "book", "magazine", "newspaper", "card",
            "label", "tag", "barcode", "qr code"
        )

        // 각 카테고리별 점수 계산
        val categoryScores = mutableMapOf<PhotoCategory, Float>()

        for (label in labels) {
            when {
                personKeywords.any { keyword -> label.first.contains(keyword) } -> {
                    categoryScores[PhotoCategory.PERSON] =
                        (categoryScores[PhotoCategory.PERSON] ?: 0f) + label.second
                }

                foodKeywords.any { keyword -> label.first.contains(keyword) } -> {
                    categoryScores[PhotoCategory.FOOD] =
                        (categoryScores[PhotoCategory.FOOD] ?: 0f) + label.second
                }

                cityKeywords.any { keyword -> label.first.contains(keyword) } -> {
                    categoryScores[PhotoCategory.CITY] =
                        (categoryScores[PhotoCategory.CITY] ?: 0f) + label.second
                }

                natureKeywords.any { keyword -> label.first.contains(keyword) } -> {
                    categoryScores[PhotoCategory.NATURE] =
                        (categoryScores[PhotoCategory.NATURE] ?: 0f) + label.second
                }

                receiptKeywords.any { keyword -> label.first.contains(keyword) } -> {
                    categoryScores[PhotoCategory.RECEIPT] =
                        (categoryScores[PhotoCategory.RECEIPT] ?: 0f) + label.second
                }
            }
        }

        return categoryScores.maxByOrNull { it.value }?.key ?: PhotoCategory.ALL
    }
} 