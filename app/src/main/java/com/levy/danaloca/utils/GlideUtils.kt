package com.levy.danaloca.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.levy.danaloca.R

object GlideUtils {
    fun loadPostImage(imageView: ImageView, url: String?) {
        if (!url.isNullOrBlank()) {
            Glide.with(imageView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .override(1080, 1080) // Square post images
                .centerCrop()
                .into(imageView)
        }
    }

    fun loadProfileImage(imageView: ImageView, url: String?) {
        if (!url.isNullOrBlank()) {
            Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .override(480, 480) // Profile image size
                .circleCrop()
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.default_avatar)
        }
    }
}