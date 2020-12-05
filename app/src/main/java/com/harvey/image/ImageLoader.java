package com.harvey.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;

public class ImageLoader {

    public static void displayImage(Context context, Object resource, ImageView imageView) {
        displayImage(context, resource, imageView, true);
    }

    public static void displayImage(Context context, Object resource, GlideDrawableImageViewTarget target) {
        displayImage(context, resource, target, true);
    }

    public static void displayImage(Context context, Object resource, GlideDrawableImageViewTarget target,
                                    boolean isCache) {
        if (context == null)
            return;
        if (resource == null)
            return;
        if (TextUtils.isEmpty(resource.toString()))
            return;
        if (target == null)
            return;
        Glide.with(context).load(resource).thumbnail(0.1f)// 缩略图支持
                .skipMemoryCache(isCache ? false : true)// 跳过内存缓存
                .diskCacheStrategy(isCache ? DiskCacheStrategy.SOURCE : DiskCacheStrategy.NONE)// 设置缓存策略
                .into(target);
    }

    public static void displayImage(Context context, Object resource, ImageView imageView, boolean isCache) {
        if (context == null)
            return;
        if (resource == null)
            return;
        if (TextUtils.isEmpty(resource.toString()))
            return;
        if (imageView == null)
            return;
        Glide.with(context).load(resource).thumbnail(0.1f)// 缩略图支持
                .skipMemoryCache(isCache ? false : true)// 跳过内存缓存
                .diskCacheStrategy(isCache ? DiskCacheStrategy.SOURCE : DiskCacheStrategy.NONE)// 设置缓存策略
                .into(imageView);
    }

    public static void displayImage(Context context, String url, ImageView imageView, int radius) {
        if (context == null)
            return;
        if (imageView == null)
            return;
        Glide.with(context).load(url).thumbnail(0.1f)// 缩略图支持
                .priority(Priority.NORMAL)// 设置下载优先级
                .diskCacheStrategy(DiskCacheStrategy.ALL)// 设置缓存策略
                .transform(new GlideRoundTransform(context, radius)).into(imageView);
    }

    public static void displayCircleImage(Context context, Object url, ImageView imageView, boolean isCache) {
        if (context == null)
            return;
        if (url == null)
            return;
        if (imageView == null)
            return;
        Glide.with(context).load(url).skipMemoryCache(isCache ? false : true)// 跳过内存缓存
                .diskCacheStrategy(isCache ? DiskCacheStrategy.SOURCE : DiskCacheStrategy.NONE)
                .transform(new GlideCircleTransform(context)).into(imageView);
    }

    public static void displayCircleImage(Context context, Object url, ViewTarget<View, GlideDrawable> target) {
        if (context == null)
            return;
        if (url == null)
            return;
        if (target == null)
            return;
        Glide.with(context).load(url).skipMemoryCache(false)// 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)// 设置缓存策略
                .transform(new GlideCircleTransform(context)).into(target);
    }

    public static void displayImage(Context context, Object url, SimpleTarget<Bitmap> target) {
        if (context == null)
            return;
        if (url == null)
            return;
        if (target == null)
            return;
        Glide.with(context).load(url).asBitmap().thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(target);
    }


    public static void displayCircleImage(Context context, String url, ImageView imageView) {
        displayCircleImage(context, url, imageView, true);
    }

    public static void displayCircleImage(Context context, int resourceId, ImageView imageView) {
        displayCircleImage(context, resourceId, imageView, true);
    }

    public static void displayGifImage(Context context, Object resource, ImageView imageView) {
        Glide.with(context).load(resource).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE)// 设置缓存策略
                .into(imageView);
    }

    public static void clearDiskCache(final Activity activity) {
        // 必须在后台线程中调用，建议同时clearMemory()
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(activity.getApplication()).clearDiskCache();
            }
        }).start();
    }

    public static void clearMemory(final Activity activity) {
        // 必须在UI线程中调用
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.get(activity).clearMemory();
            }
        });
    }

    public static void pauseLoadImage(final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).pauseRequests();
            }
        });
    }

    public static void resumeLoadImage(final Activity context) {
        if (Glide.with(context).isPaused() && !context.isFinishing()) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(context).resumeRequests();
                }
            });
        }
    }

}
