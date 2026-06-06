package com.example.moodmusicapp

import android.util.Log
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object CloudinaryRepository {
    private const val CLOUD_NAME = "di86poxdl"
    private const val UPLOAD_PRESET = "arbitify_uploads"
    private const val UPLOAD_URL =
        "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    private val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(
        context: android.content.Context,
        imageUri: android.net.Uri,
        folder: String
    ): String? {
        return try {
            val uid = auth.currentUser?.uid
            Log.d("CLOUDINARY", "=== UPLOAD START ===")
            Log.d("CLOUDINARY", "UID: $uid")
            Log.d("CLOUDINARY", "Upload URL: $UPLOAD_URL")
            Log.d("CLOUDINARY", "Preset: $UPLOAD_PRESET")
            Log.d("CLOUDINARY", "Folder: arbitify/$uid/$folder")

            if (uid == null) {
                Log.e("CLOUDINARY", "FAILED: User not logged in")
                return null
            }

            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("CLOUDINARY", "FAILED: Cannot open image URI")
                return null
            }

            val imageBytes = inputStream.readBytes()
            inputStream.close()
            Log.d("CLOUDINARY", "Image size: ${imageBytes.size} bytes")

            if (imageBytes.isEmpty()) {
                Log.e("CLOUDINARY", "FAILED: Image bytes are empty")
                return null
            }

            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "image.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", "arbitify/$uid/$folder")
                .build()

            Log.d("CLOUDINARY", "Request built, sending...")

            val request = okhttp3.Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""

            Log.d("CLOUDINARY", "Response code: $responseCode")
            Log.d("CLOUDINARY", "Response body: $responseBody")

            if (response.isSuccessful) {
                val json = org.json.JSONObject(responseBody)
                val secureUrl = json.optString("secure_url")
                Log.d("CLOUDINARY", "secure_url: $secureUrl")
                if (secureUrl.isNotBlank()) secureUrl else null
            } else {
                Log.e("CLOUDINARY", "UPLOAD FAILED - Code: $responseCode")
                Log.e("CLOUDINARY", "Error body: $responseBody")
                null
            }
        } catch (e: Exception) {
            Log.e("CLOUDINARY", "EXCEPTION: ${e.javaClass.simpleName}: ${e.message}")
            Log.e("CLOUDINARY", "Stack trace:", e)
            null
        }
    }

    suspend fun saveUrlToFirestore(field: String, url: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid)
                .set(
                    mapOf(field to url),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            Log.d("CLOUDINARY", "Saved $field to Firestore: $url")
        } catch (e: Exception) {
            Log.e("CLOUDINARY", "Firestore save error: ${e.message}", e)
        }
    }

    suspend fun uploadProfilePicture(
        context: android.content.Context,
        uri: android.net.Uri
    ): String? {
        val url = uploadImage(context, uri, "profile") ?: return null
        saveUrlToFirestore("profilePictureUrl", url)
        return url
    }

    suspend fun uploadCoverPhoto(
        context: android.content.Context,
        uri: android.net.Uri
    ): String? {
        val url = uploadImage(context, uri, "cover") ?: return null
        saveUrlToFirestore("coverPhotoUrl", url)
        return url
    }

    suspend fun getUserPhotoUrls(): Pair<String?, String?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Pair(null, null)
            val doc = db.collection("users").document(uid).get().await()
            val profileUrl = doc.getString("profilePictureUrl")
            val coverUrl = doc.getString("coverPhotoUrl")
            Log.d("CLOUDINARY", "Fetched URLs - profile: $profileUrl, cover: $coverUrl")
            Pair(profileUrl, coverUrl)
        } catch (e: Exception) {
            Log.e("CLOUDINARY", "Fetch error: ${e.message}", e)
            Pair(null, null)
        }
    }
}
