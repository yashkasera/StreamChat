package com.yashkasera.streamchat.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

fun getColorStateList(context: Context, @ColorRes id: Int) =
    ColorStateList.valueOf(ContextCompat.getColor(context, id))

fun ImageView.loadImage(url: String) {
    val shimmer = Shimmer.AlphaHighlightBuilder()
        .setBaseAlpha(0.8f)
        .setHighlightAlpha(0.7f)
        .setDropoff(0.9f)
        .setIntensity(0.5f)
        .setAutoStart(true)
        .build()

    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }
    Glide.with(this)
        .load(url)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .override(200, 200)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(shimmerDrawable)
        .into(this)
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.getDrawable(@DrawableRes id: Int): Drawable? =
    ContextCompat.getDrawable(requireContext(), id)