package com.levy.danaloca.utils

import android.content.Context
import android.graphics.BitmapFactory

object AssetUtils {
    fun getDefaultAvatarBase64(context: Context): String {
        return try {
            val inputStream = context.assets.open("default_avatar.JPG")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ImageUtils.bitmapToBase64(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}