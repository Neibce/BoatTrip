package com.boattrip.boattrip

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class PhotoGalleryFragment : Fragment() {
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultCount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var layoutSearchResults: LinearLayout

    private lateinit var photoAdapter: PhotoAdapter
    private var allPhotos = listOf<PhotoItem>() // 전체 사진 목록 (검색된 모든 사진)
    private lateinit var imageClassificationHelper: ImageClassificationHelper

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var selectedCategory = PhotoCategory.ALL

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnStartDate = view.findViewById(R.id.btn_start_date)
        btnEndDate = view.findViewById(R.id.btn_end_date)
        progressBar = view.findViewById(R.id.progress_bar)
        tvResultCount = view.findViewById(R.id.tv_result_count)
        recyclerView = view.findViewById(R.id.recycler_view_photos)
        chipGroupCategory = view.findViewById(R.id.chip_group_category)
        layoutSearchResults = view.findViewById(R.id.layout_search_results)

        // 이미지 분류 헬퍼 초기화
        imageClassificationHelper = ImageClassificationHelper(requireContext())

        photoAdapter = PhotoAdapter(emptyList())
        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = photoAdapter
        }

        setupClickListeners()
        setupCategorySelection()

        // 카테고리를 "전체"로 초기화
        chipGroupCategory.check(R.id.chip_all)
        selectedCategory = PhotoCategory.ALL

        if (!hasStoragePermission())
            requestStoragePermission()
    }

    private fun setupClickListeners() {
        btnStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                btnStartDate.text = dateFormat.format(date.time)
                checkAndAutoSearch()
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                btnEndDate.text = dateFormat.format(date.time)
                checkAndAutoSearch()
            }
        }
    }

    private fun checkAndAutoSearch() {
        if (startDate != null && endDate != null) {
            if (hasStoragePermission()) {
                searchPhotos()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun setupCategorySelection() {
        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChipId = checkedIds[0]
                selectedCategory = when (selectedChipId) {
                    R.id.chip_all -> PhotoCategory.ALL
                    R.id.chip_person -> PhotoCategory.PERSON
                    R.id.chip_food -> PhotoCategory.FOOD
                    R.id.chip_city -> PhotoCategory.CITY
                    R.id.chip_nature -> PhotoCategory.NATURE
                    R.id.chip_receipt -> PhotoCategory.RECEIPT
                    else -> PhotoCategory.ALL
                }
                // 검색된 사진이 있을 때만 필터링 적용
                if (allPhotos.isNotEmpty()) {
                    filterPhotosByCategory()
                }
            }
        }
    }

    private fun filterPhotosByCategory() {
        val filteredPhotos = if (selectedCategory == PhotoCategory.ALL) {
            allPhotos
        } else {
            // 실제 분류된 사진들로 필터링
            allPhotos.filter { it.category == selectedCategory }
        }

        photoAdapter.updatePhotos(filteredPhotos)
        updateResultCount(filteredPhotos.size, allPhotos.size)
    }

    private fun updateResultCount(filteredCount: Int, totalCount: Int) {
        val categoryText = when (selectedCategory) {
            PhotoCategory.ALL -> "전체"
            PhotoCategory.PERSON -> "사람"
            PhotoCategory.FOOD -> "음식"
            PhotoCategory.CITY -> "도시"
            PhotoCategory.NATURE -> "자연"
            PhotoCategory.RECEIPT -> "영수증"
        }

        tvResultCount.text = if (selectedCategory == PhotoCategory.ALL) {
            "총 ${totalCount}개의 사진을 찾았습니다."
        } else {
            "${categoryText} 카테고리: ${filteredCount}개 (전체 ${totalCount}개 중)"
        }
    }

    private fun showSearchResults(show: Boolean) {
        layoutSearchResults.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜 선택")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selectedDateInMillis
            onDateSelected(selectedDate)
        }

        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }

    private fun searchPhotos() {
        progressBar.visibility = View.VISIBLE
        //showSearchResults(false) // 검색 중에는 결과 섹션 숨김

        // 검색 시작 시 카테고리를 "전체"로 리셋
        chipGroupCategory.check(R.id.chip_all)
        selectedCategory = PhotoCategory.ALL

        CoroutineScope(Dispatchers.IO).launch {
            val photos = getPhotosFromGallery()

            // MLKit을 사용하여 각 사진 분류
            val classifiedPhotos = classifyPhotos(photos)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                allPhotos = classifiedPhotos

                if (classifiedPhotos.isNotEmpty()) {
                    showSearchResults(true)
                    filterPhotosByCategory()
                    Toast.makeText(
                        requireContext(),
                        "${classifiedPhotos.size}개의 사진을 분류했습니다!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showSearchResults(false)
                    Toast.makeText(requireContext(), "선택한 기간에 사진이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPhotosFromGallery(): List<PhotoItem> {
        val photos = mutableListOf<PhotoItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN, // 실제 촬영 날짜 사용
            MediaStore.Images.Media.SIZE
        )

        // 시작일과 종료일을 timestamp로 변환 (밀리초 단위)
        val startTimestamp = startDate!!.timeInMillis
        val endTimestamp = endDate!!.apply {
            // 종료일의 하루 끝까지 포함하도록 23:59:59로 설정
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // DATE_TAKEN 기준으로 필터링 (밀리초 단위)

        val selection =
            "${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} <= ? AND " +
                    "${MediaStore.Images.Media.DATE_TAKEN} IS NOT NULL"
        val selectionArgs = arrayOf(startTimestamp.toString(), endTimestamp.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        requireActivity().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateTakenColumn)
                val size = cursor.getLong(sizeColumn)

                // dateTaken이 0이 아닌 경우에만 추가 (유효한 촬영 날짜가 있는 경우)
                if (dateTaken > 0) {
                    val contentUri = android.content.ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // 초기에는 ALL 카테고리로 설정 (나중에 분류됨)
                    photos.add(PhotoItem(contentUri, dateTaken, name, size, PhotoCategory.ALL))
                }
            }
        }

        return photos
    }

    private suspend fun classifyPhotos(photos: List<PhotoItem>): List<PhotoItem> {
        val classifiedPhotos = mutableListOf<PhotoItem>()
        val total = photos.size
        var processed = 0

        // 진행률 업데이트를 위한 UI 업데이트
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
        }

        for (photo in photos) {
            try {
                val category = imageClassificationHelper.classifyImage(photo.uri)
                val classifiedPhoto = photo.copy(category = category)
                classifiedPhotos.add(classifiedPhoto)

                processed++

                // 진행률을 주기적으로 업데이트 (10장마다 또는 마지막)
                if (processed % 10 == 0 || processed == total) {
                    withContext(Dispatchers.Main) {
                        // 진행률 표시 (예시)
                        // progressBar.progress = (processed * 100) / total
                    }
                }

            } catch (e: Exception) {
                // 분류 실패시 원본 사진을 ALL 카테고리로 유지
                classifiedPhotos.add(photo)
                Log.e("PhotoClassification", "Failed to classify photo: ${photo.displayName}", e)
            }
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
        }

        return classifiedPhotos
    }
} 