package com.boattrip.boattrip

import android.net.Uri

enum class PhotoCategory {
    ALL, PERSON, FOOD, OUTDOOR, RECEIPT
}

data class PhotoItem(
    val uri: Uri,
    val dateTaken: Long,
    val displayName: String?,
    val size: Long,
    val category: PhotoCategory = PhotoCategory.ALL
) 