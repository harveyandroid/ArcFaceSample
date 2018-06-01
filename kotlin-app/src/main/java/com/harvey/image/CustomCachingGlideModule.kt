package com.harvey.image

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.GlideModule

/**
 * Created by hanhui on 2018/6/1 0001 11:49
 */
class CustomCachingGlideModule : GlideModule {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDecodeFormat(DecodeFormat.DEFAULT)
        val calculator = MemorySizeCalculator(context)
        val defaultMemoryCacheSize = calculator.memoryCacheSize
        val defaultBitmapPoolSize = calculator.bitmapPoolSize
        val customMemoryCacheSize = (1.2 * defaultMemoryCacheSize).toInt()
        val customBitmapPoolSize = (1.2 * defaultBitmapPoolSize).toInt()
        builder.setMemoryCache(LruResourceCache(customMemoryCacheSize))
        builder.setBitmapPool(LruBitmapPool(customBitmapPoolSize))
        val cacheSize100MegaBytes = 104857600
        builder.setDiskCache(ExternalCacheDiskCacheFactory(context, GLIDE_CACHE_PATH, cacheSize100MegaBytes))
    }

    override fun registerComponents(context: Context, glide: Glide) {

    }

    companion object {
        val GLIDE_CACHE_PATH = "Avatar"
    }
}