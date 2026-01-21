package com.example.mediacraft.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object PathUtils {
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor?.moveToFirst()
            return columnIndex?.let { cursor?.getString(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            cursor?.close()
        }
    }
}