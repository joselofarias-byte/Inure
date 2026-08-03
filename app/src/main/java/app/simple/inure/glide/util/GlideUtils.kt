package app.simple.inure.glide.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.simple.inure.R
import app.simple.inure.util.BitmapHelper.toBitmap
import app.simple.inure.util.BitmapHelper.toInputStream
import java.io.InputStream

object GlideUtils {

    fun Context.getGeneratedAppIconBitmap(): Bitmap? {
        R.drawable.ic_app_icon_placeholder.toBitmap(this).toInputStream().use {
            return BitmapFactory.decodeStream(it)
        }
    }

    fun Context.getGeneratedAppIconStream(): InputStream {
        R.drawable.ic_app_icon_placeholder.toBitmap(this).toInputStream().use {
            return it
        }
    }
}
