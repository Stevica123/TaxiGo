package com.example.taxigo.models

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.firebase.auth.FirebaseUser

data class UserModel(
    val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isGoogle: Boolean
) {
    companion object {
        fun fromFirebaseUser(user: FirebaseUser?): UserModel? {
            return user?.let {
                val isGoogleUser = it.providerData.any { info -> info.providerId == "google.com" }
                UserModel(
                    uid = it.uid,
                    name = it.displayName,
                    email = it.email,
                    photoUrl = it.photoUrl?.toString(),
                    isAnonymous = it.isAnonymous,
                    isGoogle = isGoogleUser
                )
            }
        }

        fun generateInitialCircle(context: Context, letter: Char): Drawable {
            val size = 200
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val backgroundPaint = Paint().apply {
                color = Color.parseColor("#DD4A3A")
                isAntiAlias = true
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint)

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 80f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            val yOffset = (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(letter.toString(), size / 2f, size / 2f - yOffset, textPaint)

            return BitmapDrawable(context.resources, bitmap)
        }
    }
}