package com.antigravity.eframe.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class FramePhoto(
    val id: String,
    val title: String,
    val url: String,
    val directDownloadUrl: String = url
)

class DriveRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Default sample photos with high-resolution imagery for instant out-of-the-box demo
    val samplePhotos: List<FramePhoto> = listOf(
        FramePhoto(
            id = "sample_1",
            title = "Monte Fitz Roy, Patagonia",
            url = "https://images.unsplash.com/photo-1518457607834-6e8d80c183c5?auto=format&fit=crop&w=1920&q=80"
        ),
        FramePhoto(
            id = "sample_2",
            title = "Glaciar Perito Moreno",
            url = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1920&q=80"
        ),
        FramePhoto(
            id = "sample_3",
            title = "Bariloche y Lagos Andinos",
            url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1920&q=80"
        ),
        FramePhoto(
            id = "sample_4",
            title = "Cataratas del Iguazú",
            url = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1920&q=80"
        ),
        FramePhoto(
            id = "sample_5",
            title = "Cerro de los Siete Colores, Purmamarca",
            url = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1920&q=80"
        )
    )

    fun extractFolderId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // If it's already an ID with alphanumeric + _ - and at least 15 chars
        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{15,}$"))) {
            return trimmed
        }

        // Check for /folders/{id}
        val folderRegex = Pattern.compile("folders/([a-zA-Z0-9_-]+)")
        val folderMatcher = folderRegex.matcher(trimmed)
        if (folderMatcher.find()) {
            return folderMatcher.group(1)
        }

        // Check for id={id}
        val idRegex = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)")
        val idMatcher = idRegex.matcher(trimmed)
        if (idMatcher.find()) {
            return idMatcher.group(1)
        }

        return null
    }

    suspend fun fetchPhotosFromFolder(
        folderUrlOrId: String,
        apiKey: String = ""
    ): Result<List<FramePhoto>> = withContext(Dispatchers.IO) {
        val folderId = extractFolderId(folderUrlOrId)
            ?: return@withContext Result.failure(IllegalArgumentException("El link o ID de carpeta de Google Drive no es válido"))

        try {
            if (apiKey.isNotBlank()) {
                val apiResult = fetchViaDriveApi(folderId, apiKey)
                if (apiResult.isSuccess && apiResult.getOrThrow().isNotEmpty()) {
                    return@withContext apiResult
                }
            }

            // Fallback to public web parsing
            val webResult = fetchViaPublicWeb(folderId)
            if (webResult.isSuccess && webResult.getOrThrow().isNotEmpty()) {
                return@withContext webResult
            }

            // If empty, return failure so caller can keep previous or use sample
            Result.failure(Exception("No se encontraron imágenes públicas en la carpeta."))
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener fotos de Drive", e)
            Result.failure(e)
        }
    }

    private fun fetchViaDriveApi(folderId: String, apiKey: String): Result<List<FramePhoto>> {
        val url = "https://www.googleapis.com/drive/v3/files?" +
                "q='${folderId}'+in+parents+and+trashed=false+and+mimeType+contains+'image/'" +
                "&fields=files(id,name,mimeType,webContentLink,thumbnailLink)&pageSize=100&key=${apiKey}"

        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("Drive API error HTTP ${response.code}"))
            }

            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val filesArray = json.optJSONArray("files") ?: return Result.success(emptyList())

            val photos = mutableListOf<FramePhoto>()
            for (i in 0 until filesArray.length()) {
                val fileObj = filesArray.getJSONObject(i)
                val id = fileObj.getString("id")
                val name = fileObj.optString("name", "Foto")
                val cdnUrl = "https://lh3.googleusercontent.com/d/$id=w2560-h1440"
                val downloadUrl = "https://drive.google.com/uc?export=download&id=$id"
                photos.add(FramePhoto(id = id, title = name, url = cdnUrl, directDownloadUrl = downloadUrl))
            }
            return Result.success(photos)
        }
    }

    private fun fetchViaPublicWeb(folderId: String): Result<List<FramePhoto>> {
        val webUrl = "https://drive.google.com/drive/folders/$folderId"
        val request = Request.Builder()
            .url(webUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code} al acceder a la carpeta pública"))
            }

            val html = response.body?.string().orEmpty()

            // Google Drive embeds file IDs in JavaScript arrays / data attributes:
            // e.g. ["1a2b3c4d5e...", "imagen.jpg", "image/jpeg"]
            val foundFileIds = linkedSetOf<String>()

            // Pattern for file IDs inside Drive viewer JSON
            val pattern = Pattern.compile("\\[\\\\?\"([a-zA-Z0-9_-]{28,45})\\\\?\",\\s*\\[?\\\\?\"([^\"]+\\.(?:jpg|jpeg|png|webp|gif))\\\\?\"")
            val matcher = pattern.matcher(html)
            var index = 1
            val photos = mutableListOf<FramePhoto>()

            while (matcher.find()) {
                val fileId = matcher.group(1)
                val fileName = matcher.group(2) ?: "Foto $index"
                if (fileId != null && foundFileIds.add(fileId)) {
                    val cdnUrl = "https://lh3.googleusercontent.com/d/$fileId=w2560-h1440"
                    val downloadUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                    photos.add(FramePhoto(id = fileId, title = fileName, url = cdnUrl, directDownloadUrl = downloadUrl))
                    index++
                }
            }

            // If regex didn't catch, fallback to secondary ID finder
            if (photos.isEmpty()) {
                val genericIdPattern = Pattern.compile("\"([a-zA-Z0-9_-]{33})\"")
                val genMatcher = genericIdPattern.matcher(html)
                while (genMatcher.find()) {
                    val fileId = genMatcher.group(1)
                    if (fileId != null && fileId != folderId && foundFileIds.add(fileId)) {
                        val cdnUrl = "https://lh3.googleusercontent.com/d/$fileId=w2560-h1440"
                        val downloadUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                        photos.add(FramePhoto(id = fileId, title = "Foto $index", url = cdnUrl, directDownloadUrl = downloadUrl))
                        index++
                    }
                }
            }

            return Result.success(photos)
        }
    }

    companion object {
        private const val TAG = "DriveRepository"
    }
}

