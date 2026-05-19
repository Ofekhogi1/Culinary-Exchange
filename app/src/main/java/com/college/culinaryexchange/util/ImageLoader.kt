package com.college.culinaryexchange.util

import android.content.Context
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

object ImageLoader {

    fun load(context: Context, url: String?, imageView: ImageView) {
        if (url.isNullOrEmpty()) return
        Glide.with(context).load(toGlideModel(url)).centerCrop().into(imageView)
    }

    fun loadCircle(context: Context, url: String?, imageView: ImageView) {
        if (url.isNullOrEmpty()) return
        Glide.with(context).load(toGlideModel(url)).transform(CircleCrop()).into(imageView)
    }

    private fun toGlideModel(url: String): Any =
        if (url.startsWith("data:image")) Base64.decode(url.substringAfter("base64,"), Base64.DEFAULT)
        else url
}
