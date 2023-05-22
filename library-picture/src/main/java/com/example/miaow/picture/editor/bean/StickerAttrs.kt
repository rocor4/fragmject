package com.example.miaow.picture.editor.bean

import android.graphics.*
import androidx.core.graphics.alpha

class StickerAttrs(
    var bitmap: Bitmap,
    var description: String = "",
    var rotation: Float = 0f,
    var scale: Float = 1f,
    var translateX: Float = 0f,
    var translateY: Float = 0f,
){
    fun dealBackground(r:Int, g:Int, b:Int) {
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                val color = bitmap.getPixel(i, j)
//                获取像素点的透明度
                val a = Color.alpha(color) ?: 0
                if (a != 0){
                    val newColor = Color.argb(a, r, g, b)
                    bitmap.setPixel(i, j, newColor)
                }
            }
        }
    }
}