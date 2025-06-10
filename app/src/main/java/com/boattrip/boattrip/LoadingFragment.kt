package com.boattrip.boattrip

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class LoadingFragment : DialogFragment(R.layout.fragment_loading) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .apply { setCancelable(false) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val img = view.findViewById<ImageView>(R.id.loadingGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_dog)
            .into(img)
    }
}