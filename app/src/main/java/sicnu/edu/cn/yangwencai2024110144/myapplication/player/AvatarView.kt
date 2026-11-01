package sicnu.edu.cn.yangwencai2024110144.myapplication.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.google.android.material.imageview.ShapeableImageView
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 头像显示工具。
 *
 * 用户选择的相册图片可能有几千万像素，直接 setImageURI 很容易 OOM，
 * 所以先按目标尺寸降采样，再显示到圆形头像里。
 * 没有选择头像时显示统一的占位图。
 */
fun ShapeableImageView.showAvatar(avatarUri: String?) {
    if (avatarUri.isNullOrBlank()) {
        showPlaceholder()
        return
    }

    val bitmap = runCatching {
        decodeSampledBitmap(Uri.parse(avatarUri), AVATAR_SIZE_PX)
    }.getOrNull()

    if (bitmap == null) {
        showPlaceholder()
    } else {
        setPadding(0, 0, 0, 0)
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageBitmap(bitmap)
    }
}

private fun ShapeableImageView.showPlaceholder() {
    val padding = (24 * resources.displayMetrics.density).toInt()
    setPadding(padding, padding, padding, padding)
    scaleType = ImageView.ScaleType.CENTER_INSIDE
    setImageResource(R.drawable.ic_avatar_placeholder)
}

private fun ShapeableImageView.decodeSampledBitmap(uri: Uri, requiredSize: Int): Bitmap? {
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= requiredSize &&
        bounds.outHeight / (sampleSize * 2) >= requiredSize
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private const val AVATAR_SIZE_PX = 256
