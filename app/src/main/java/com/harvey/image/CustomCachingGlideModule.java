package com.harvey.image;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;


/**
 * Created by harvey on 2016/9/1 0001 11:44
 */
public class CustomCachingGlideModule implements GlideModule {
	public static final String GLIDE_CACHE_PATH = "Avatar";
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		builder.setDecodeFormat(DecodeFormat.DEFAULT);
		// 自定义内存缓存20%
		MemorySizeCalculator calculator = new MemorySizeCalculator(context);
		int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
		int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
		int customMemoryCacheSize = (int) (1.2 * defaultMemoryCacheSize);
		int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
		builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
		builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
		int cacheSize100MegaBytes = 104857600;
		builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, GLIDE_CACHE_PATH, cacheSize100MegaBytes));
	}

	@Override
	public void registerComponents(Context context, Glide glide) {

	}
}
