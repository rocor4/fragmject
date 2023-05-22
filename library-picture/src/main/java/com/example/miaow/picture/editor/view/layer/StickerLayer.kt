package com.example.miaow.picture.editor.view.layer

import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.values
import com.example.miaow.picture.editor.bean.StickerAttrs
import com.example.miaow.picture.editor.utils.Vector2D
import kotlin.math.min

class StickerLayer(
    private val parent: View,
    private val attrs: StickerAttrs,
    private val listener: OnStickerClickListener? = null
) : ILayer {

    companion object {
        private const val INVALID_POINTER_ID = -1
        private const val MINIMUM_SCALE = 0.5f
        private const val RECT_ROUND = 30.0f
    }

    private var viewWidth = 0
    private var viewHeight = 0
    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private val parentMatrix = Matrix()
    private val stickerRectF = RectF()
    private var stickerWidth = attrs.bitmap.width
    private var stickerHeight = attrs.bitmap.height
    private var currRotation = attrs.rotation
    private var currScale = attrs.scale
    private var currTranslateX = attrs.translateX
    private var currTranslateY = attrs.translateY
    private var initRotation = 0f
    private val prevSpanVector = Vector2D(0f, 0f)
    private val currSpanVector = Vector2D(0f, 0f)
    private var pointerIndexId0 = INVALID_POINTER_ID
    private var pointerIndexId1 = INVALID_POINTER_ID
    private var downX = 0f
    private var downY = 0f
    private val borderPaint = Paint()
    private val borderRectF = RectF()
    private var inBorder = false
    private var isInProgress = false
    private var touchTime = 0L

    var isEnabled = true

    private val scaleGestureDetector = ScaleGestureDetector(parent.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                adjustScale(detector.scaleFactor)
                measureBitmap()
                parent.postInvalidate()
                return true
            }

        })

    init {
        borderPaint.isAntiAlias = true
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1f
    }

    fun setParentMatrix(matrix: Matrix) {
        parentMatrix.set(matrix)
    }

    fun inStickerBounds(x: Float, y: Float): Boolean {
        return borderRectF.contains(x, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            scaleGestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isInProgress = false
                    touchTime = System.currentTimeMillis()
                    val pointerId0 = event.getPointerId(0)
                    pointerIndexId0 = event.findPointerIndex(pointerId0)
                    downX = event.x
                    downY = event.y
                    if (!inBorder) {
                        inBorder = true
                        parent.postInvalidate()
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    isInProgress = true
                    val pointerId1 = event.getPointerId(event.actionIndex)
                    pointerIndexId1 = event.findPointerIndex(pointerId1)
                    computeSpanVector(event)
                    prevSpanVector.set(currSpanVector)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (pointerIndexId0 != INVALID_POINTER_ID) {
                        if (pointerIndexId1 != INVALID_POINTER_ID) {
                            computeSpanVector(event)
                            val deltaAngle = Vector2D.getAngle(prevSpanVector, currSpanVector)
                            adjustAngle(deltaAngle)
                        } else {
                            if (!isInProgress) {
                                val currX = event.getX(pointerIndexId0)
                                val currY = event.getY(pointerIndexId0)
                                adjustTranslation(currX - downX, currY - downY)
                                downX = currX
                                downY = currY
                            }
                        }
                        parent.postInvalidate()
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    pointerIndexId1 = if (event.pointerCount > 2) {
                        val pointerId1 = event.getPointerId(event.actionIndex)
                        event.findPointerIndex(pointerId1)
                    } else {
                        INVALID_POINTER_ID
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val isClick = System.currentTimeMillis() - touchTime < 250
                    if (isClick && attrs.description.isNotBlank()) {
                        val bitmap = attrs.bitmap
                        val desc = attrs.description
                        val rotation = currRotation
                        val scale = currScale
                        val x = currTranslateX
                        val y = currTranslateY
                        listener?.onClick(StickerAttrs(bitmap, desc, rotation, scale, x, y))
                    }
                    if (currTranslateX == 0f) {
                        currTranslateX = (viewWidth * 0.5f - parentTranslateX()) / parentScaleX()
                    }
                    if (currTranslateY == 0f) {
                        currTranslateY = if (parentTranslateY() < 0) {
                            (viewHeight * 0.5f - parentTranslateY()) / parentScaleY()
                        } else {
                            min(viewHeight, bitmapHeight) * 0.5f / parentScaleY()
                        }
                    }
                    val sw = stickerWidth * 0.5f / parentScaleX()
                    if (currTranslateX - sw < 0 || currTranslateX + sw > bitmapWidth) {
                        //贴图超过边缘回正
//                        currTranslateX = (viewWidth * 0.5f - parentTranslateX()) / parentScaleX()
                    }
                    val sh = stickerHeight * 0.5f / parentScaleY()
                    if (currTranslateY - sh < 0 || currTranslateY + sh > bitmapHeight) {
//                        currTranslateY = if (parentTranslateY() < 0) {
//                            (viewHeight * 0.5f - parentTranslateY()) / parentScaleY()
//                        } else {
//                            min(viewHeight, bitmapHeight) * 0.5f / parentScaleY()
//                        }
                    }
                    measureBitmap()
                    pointerIndexId0 = INVALID_POINTER_ID
                    pointerIndexId1 = INVALID_POINTER_ID
                    initRotation = currRotation
                    if (inBorder) {
                        inBorder = false
                        parent.postInvalidate()
                    }
                    return false
                }
            }
            measureBitmap()
        }
        return isEnabled
    }

    override fun onSizeChanged(
        viewWidth: Int,
        viewHeight: Int,
        bitmapWidth: Int,
        bitmapHeight: Int
    ) {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight
        this.bitmapWidth = bitmapWidth
        this.bitmapHeight = bitmapHeight
        if (currTranslateX == 0f) {
            currTranslateX = (viewWidth * 0.5f - parentTranslateX()) / parentScaleX()
        }
        if (currTranslateY == 0f) {
            currTranslateY = if (parentTranslateY() < 0) {
                (viewHeight * 0.5f - parentTranslateY()) / parentScaleY()
            } else {
                min(viewHeight, bitmapHeight) * 0.5f / parentScaleY()
            }
        }
        measureBitmap()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(currRotation, borderRectF.centerX(), borderRectF.centerY())
        if (inBorder) {
            canvas.drawRect(borderRectF, borderPaint)
        }
        canvas.drawBitmap(attrs.bitmap, null, stickerRectF, null)
        canvas.restore()
    }

    private fun measureBitmap() {
        val stickerLeft = currTranslateX - (stickerWidth * currScale / parentScaleX() *    0.3f)
        val stickerTop = currTranslateY - (stickerHeight * currScale / parentScaleY() *    0.3f)
        val stickerRight = currTranslateX + (stickerWidth * currScale / parentScaleX() *   0.3f)
        val stickerBottom = currTranslateY + (stickerHeight * currScale / parentScaleY() * 0.3f)
        stickerRectF.set(stickerLeft, stickerTop, stickerRight, stickerBottom)
        val borderLeft = stickerRectF.left - RECT_ROUND
        val borderTop = stickerRectF.top - RECT_ROUND
        val borderRight = stickerRectF.right + RECT_ROUND
        val borderBottom = stickerRectF.bottom + RECT_ROUND
        borderRectF.set(borderLeft, borderTop, borderRight, borderBottom)
        val tempMatrix = Matrix()
        tempMatrix.mapRect(stickerRectF)
        tempMatrix.mapRect(borderRectF)
        tempMatrix.postRotate(currRotation, borderRectF.centerX(), borderRectF.centerY())
    }

    private fun computeSpanVector(event: MotionEvent) {
        if (pointerIndexId0 != INVALID_POINTER_ID && pointerIndexId1 != INVALID_POINTER_ID) {
            try {
                val cx0 = event.getX(pointerIndexId0)
                val cy0 = event.getY(pointerIndexId0)
                val cx1 = event.getX(pointerIndexId1)
                val cy1 = event.getY(pointerIndexId1)
                val cvx = cx1 - cx0
                val cvy = cy1 - cy0
                if (!cvx.isNaN() && !cvy.isNaN()) {
                    currSpanVector.set(cvx, cvy)
                }
            }catch (ex: IllegalArgumentException){
                Log.e("StickerLayer", ex.toString())
            }
        }
    }

    private fun adjustAngle(degrees: Float) {
        var rotation = degrees
        if (rotation > 180.0f) {
            rotation -= 360.0f
        } else if (rotation < -180.0f) {
            rotation += 360.0f
        }
        if (!degrees.isNaN()) {
            currRotation = initRotation + degrees
        }
    }

    private fun adjustTranslation(deltaX: Float, deltaY: Float) {
        currTranslateX += deltaX
        currTranslateY += deltaY
    }

    private fun adjustScale(deltaScale: Float) {
        currScale *= deltaScale
        currScale = MINIMUM_SCALE.coerceAtLeast(currScale)
    }

    private fun parentScaleX(): Float {
        return parentMatrix.values()[0]
    }

    private fun parentScaleY(): Float {
        return parentMatrix.values()[4]
    }

    private fun parentTranslateX(): Float {
        return parentMatrix.values()[2]
    }

    private fun parentTranslateY(): Float {
        return parentMatrix.values()[5]
    }

}

interface OnStickerClickListener {
    fun onClick(attrs: StickerAttrs)
}