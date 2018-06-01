package com.harvey.image

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.ViewTarget

/**
 * Created by hanhui on 2018/6/1 0001 11:51
 */
object ImageLoader {

    fun displayImage(context: Context, resource: Any, imageView: ImageView) {
        displayImage(context, resource, imageView, true)
    }

    fun displayImage(context: Context, resource: Any, target: GlideDrawableImageViewTarget) {
        displayImage(context, resource, target, true)
    }

    fun displayImage(context: Context?, resource: Any?, target: GlideDrawableImageViewTarget?,
                     isCache: Boolean) {
        if (context == null)
            return
        if (resource == null)
            return
        if (TextUtils.isEmpty(resource.toString()))
            return
        if (target == null)
            return
        Glide.with(context).load(resource).thumbnail(0.1f)// 缩略图支持
                .skipMemoryCache(if (isCache) false else true)// 跳过内存缓存
                .diskCacheStrategy(if (isCache) DiskCacheStrategy.SOURCE else DiskCacheStrategy.NONE)// 设置缓存策略
                .into(target)
    }

    fun displayImage(context: Context?, resource: Any?, imageView: ImageView?, isCache: Boolean) {
        if (context == null)
            return
        if (resource == null)
            return
        if (TextUtils.isEmpty(resource.toString()))
            return
        if (imageView == null)
            return
        Glide.with(context).load(resource).thumbnail(0.1f)// 缩略图支持
                .skipMemoryCache(if (isCache) false else true)// 跳过内存缓存
                .diskCacheStrategy(if (isCache) DiskCacheStrategy.SOURCE else DiskCacheStrategy.NONE)// 设置缓存策略
                .into(imageView)
    }

    fun displayImage(context: Context?, url: String, imageView: ImageView?, radius: Int) {
        if (context == null)
            return
        if (imageView == null)
            return
        Glide.with(context).load(url).thumbnail(0.1f)// 缩略图支持
                .priority(Priority.NORMAL)// 设置下载优先级
                .diskCacheStrategy(DiskCacheStrategy.ALL)// 设置缓存策略
                .transform(GlideRoundTransform(context, radius)).into(imageView)
    }

    fun displayCircleImage(context: Context?, url: Any?, imageView: ImageView?, isCache: Boolean) {
        if (context == null)
            return
        if (url == null)
            return
        if (imageView == null)
            return
        Glide.with(context).load(url).skipMemoryCache(if (isCache) false else true)// 跳过内存缓存
                .diskCacheStrategy(if (isCache) DiskCacheStrategy.SOURCE else DiskCacheStrategy.NONE)
                .transform(GlideCircleTransform(context)).into(imageView)
    }

    fun displayCircleImage(context: Context?, url: Any?, target: ViewTarget<View, GlideDrawable>?) {
        if (context == null)
            return
        if (url == null)
            return
        if (target == null)
            return
        Glide.with(context).load(url).skipMemoryCache(false)// 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)// 设置缓存策略
                .transform(GlideCircleTransform(context)).into(target)
    }

    fun displayImage(context: Context?, url: Any?, target: SimpleTarget<Bitmap>?) {
        if (context == null)
            return
        if (url == null)
            return
        if (target == null)
            return
        Glide.with(context).load(url).asBitmap().thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(target)
    }


    fun displayCircleImage(context: Context, url: String, imageView: ImageView) {
        displayCircleImage(context, url, imageView, true)
    }

    fun displayCircleImage(context: Context, resourceId: Int, imageView: ImageView) {
        displayCircleImage(context, resourceId, imageView, true)
    }

    fun displayGifImage(context: Context, resource: Any, imageView: ImageView) {
        Glide.with(context).load(resource).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE)// 设置缓存策略
                .into(imageView)
    }

    fun clearDiskCache(activity: Activity) {
        // 必须在后台线程中调用，建议同时clearMemory()
        Thread(Runnable { Glide.get(activity.application).clearDiskCache() }).start()
    }

    fun clearMemory(activity: Activity) {
        // 必须在UI线程中调用
        activity.runOnUiThread { Glide.get(activity).clearMemory() }
    }

    fun pauseLoadImage(context: Activity) {
        context.runOnUiThread { Glide.with(context).pauseRequests() }
    }

    fun resumeLoadImage(context: Activity) {
        if (Glide.with(context).isPaused && !context.isFinishing) {
            context.runOnUiThread { Glide.with(context).resumeRequests() }
        }
    }
}