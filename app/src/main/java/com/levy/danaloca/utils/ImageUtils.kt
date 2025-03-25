package com.levy.danaloca.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ImageUtils {
    suspend fun uploadImage(
        context: Context, 
        bitmap: Bitmap, 
        folder: String
    ): String = suspendCoroutine { continuation ->
        try {
            // Convert bitmap to byte array
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()

            // Upload to Cloudinary
            MediaManager.get().upload(byteArray)
                .option("folder", folder)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("ImageUtils", "Upload started with requestId: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes)
                        Log.d("ImageUtils", "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            Log.d("ImageUtils", "Upload successful. URL: $url")
                            continuation.resume(url)
                        } else {
                            continuation.resumeWithException(Exception("Failed to get URL from response"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("ImageUtils", "Upload error: ${error.description}")
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("ImageUtils", "Upload rescheduled: ${error.description}")
                        continuation.resumeWithException(Exception(error.description))
                    }
                })
                .dispatch(context)

        } catch (e: Exception) {
            Log.e("ImageUtils", "Error during upload", e)
            continuation.resumeWithException(e)
        }
    }

    fun decodeImage(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error decoding image", e)
            null
        }
    }
}