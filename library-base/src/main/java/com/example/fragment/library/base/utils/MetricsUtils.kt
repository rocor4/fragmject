package com.example.fragment.library.base.utils

import android.content.Context
import com.example.fragment.library.base.provider.BaseContentProvider

/**
 * 获取屏幕宽度
 */
fun screenWidth(): Int {
    val width = BaseContentProvider.get().getScreenWidth()
    return if (width % 2 == 0) width else width + 1
}

fun Context.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

/**
 * 获取屏幕高度
 */
fun screenHeight() : Int {
    val height = BaseContentProvider.get().getScreenHeight()
    return if (height % 2 == 0) height else height + 1
}

fun Context.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun densityDpi() = BaseContentProvider.get().getDensityDpi()

fun Context.getDensityDpi(): Int {
    return resources.displayMetrics.densityDpi
}

/**
 * dp转px
 */
fun dp2px(dp: Float) = BaseContentProvider.get().dp2px(dp)

fun Context.dp2px(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f)
}

/**
 * px转dp
 */
fun px2dp(px: Float) = BaseContentProvider.get().px2dp(px)

fun Context.px2dp(px: Float): Float {
    return (px / resources.displayMetrics.density + 0.5f)
}

/**
 * px转sp
 */
fun px2sp(px: Float) = BaseContentProvider.get().px2sp(px)

fun Context.px2sp(px: Float): Float {
    return (px / resources.displayMetrics.scaledDensity + 0.5f)
}

/**
 * sp转px
 */
fun sp2px(sp: Float) = BaseContentProvider.get().sp2px(sp)

fun Context.sp2px(sp: Float): Float {
    return (sp * resources.displayMetrics.scaledDensity + 0.5f)
}