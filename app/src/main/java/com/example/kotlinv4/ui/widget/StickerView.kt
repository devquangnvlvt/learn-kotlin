package com.example.kotlinv4.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.kotlinv4.R
import kotlin.math.*

/**
 * StickerView — Custom View hiển thị 1 bitmap có thể:
 *   - Drag   : kéo để di chuyển
 *   - Scale  : handle góc dưới phải để phóng to/thu nhỏ
 *   - Rotate : handle góc trên phải để xoay
 *   - Flip   : handle góc dưới trái — chạm vào lật bitmap, handle giữ nguyên vị trí
 *   - Delete : handle góc trên trái — chạm vào tự remove khỏi parent
 *
 * Dùng độc lập hoặc stack nhiều cái trong 1 FrameLayout —
 * mỗi StickerView quản lý đúng 1 bitmap, tái sử dụng bằng
 * setFeatures() + setBitmap().
 */
class StickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ── Features ──────────────────────────────────────────────────────────────
    private var features: Set<String> = StickerFeature.ALL

    fun setFeatures(featureSet: Set<String>) {
        features = featureSet
        invalidate()
    }

    // ── Bitmap ────────────────────────────────────────────────────────────────
    private var bitmap: Bitmap? = null

    // ── Transform state ───────────────────────────────────────────────────────
    private var cx = 0f
    private var cy = 0f
    private var scale = 1f
    private var rotation = 0f
    private var flipX = false
    private var flipY = false

    // ── Handle ────────────────────────────────────────────────────────────────
    private val handleRadius = 28f
    private val iconSize = 36

    private val iconScale: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_handle_scale)
    }
    private val iconRotate: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_handle_rotate)
    }
    private val iconFlip: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_handle_flip)
    }
    private val iconDelete: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_handle_delete)
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    private val borderShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66000000")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = DashPathEffect(floatArrayOf(16f, 8f), 0f)
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(16f, 8f), 0f)
    }
    private val handleScalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL
    }
    private val handleRotatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5252"); style = Paint.Style.FILL
    }
    private val handleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 2f
    }
    private val handleFlipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3"); style = Paint.Style.FILL
    }
    private val handleDeletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336"); style = Paint.Style.FILL
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }
    private val matrix = Matrix()

    // ── Touch ─────────────────────────────────────────────────────────────────
    private enum class TouchMode { NONE, DRAG, SCALE, ROTATE, PINCH }
    private var touchMode = TouchMode.NONE
    private var isSelected = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initScale = 1f
    private var initRotation = 0f
    private var initDist = 0f
    private var initAngle = 0f
    // Pinch state
    private var pinchInitDist = 0f
    private var pinchInitScale = 1f

    // ── Callback khi user chạm ra ngoài (để parent deselect các view khác) ───
    var onTouchedOutside: (() -> Unit)? = null

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Set bitmap cần hiển thị.
     * Tự động căn giữa và scale vừa view (80% kích thước view).
     */
    fun setBitmap(bmp: Bitmap) {
        bitmap = bmp
        post {
            cx = width / 2f
            cy = height / 2f
            val maxW = width * 0.8f
            val maxH = height * 0.8f
            scale = min(maxW / bmp.width, maxH / bmp.height)
            invalidate()
        }
    }

    /**
     * Set bitmap sticker nhỏ hơn (30% width view).
     * Dùng khi add sticker overlay lên frame.
     */
    fun setStickerBitmap(bmp: Bitmap) {
        bitmap = bmp
        post {
            cx = width / 2f
            cy = height / 2f
            scale = (width * 0.3f) / bmp.width.coerceAtLeast(1)
            isSelected = true   // sticker mới tạo → tự động selected
            invalidate()
        }
    }

    /** Deselect (ẩn border + handle) — gọi từ bên ngoài khi sticker khác được chọn */
    fun deselect() {
        isSelected = false
        invalidate()
    }

    fun flipHorizontal() { flipX = !flipX; invalidate() }
    fun flipVertical()   { flipY = !flipY; invalidate() }

    // ── Draw ──────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        val bmp = bitmap ?: return
        val hw = bmp.width * scale / 2f
        val hh = bmp.height * scale / 2f

        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(rotation)

        // Vẽ bitmap với flip (scale âm chỉ ảnh hưởng bitmap, restore trước khi vẽ handle)
        canvas.save()
        canvas.scale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
        matrix.reset()
        matrix.setScale(scale, scale)
        matrix.postTranslate(-hw, -hh)
        canvas.drawBitmap(bmp, matrix, bitmapPaint)
        canvas.restore()  // ← restore flip, handle vẽ tiếp không bị lật

        // Border + handle vẽ sau restore → luôn đúng vị trí dù flipX/flipY
        if (isSelected) {
            canvas.drawRect(-hw, -hh, hw, hh, borderShadowPaint)
            canvas.drawRect(-hw, -hh, hw, hh, borderPaint)
            if (StickerFeature.SCALE in features)
                drawHandle(canvas, hw, hh, handleScalePaint, iconScale)
            if (StickerFeature.ROTATE in features)
                drawHandle(canvas, hw, -hh, handleRotatePaint, iconRotate)
            if (StickerFeature.FLIP in features)
                drawHandle(canvas, -hw, hh, handleFlipPaint, iconFlip)
            if (StickerFeature.DELETE in features)
                drawHandle(canvas, -hw, -hh, handleDeletePaint, iconDelete)
        }

        canvas.restore()
    }

    private fun drawHandle(canvas: Canvas, hx: Float, hy: Float, bgPaint: Paint, icon: Drawable?) {
        canvas.drawCircle(hx, hy, handleRadius, bgPaint)
        canvas.drawCircle(hx, hy, handleRadius, handleStrokePaint)
        icon?.let {
            val half = iconSize / 2
            it.setBounds(
                (hx - half).toInt(), (hy - half).toInt(),
                (hx + half).toInt(), (hy + half).toInt()
            )
            it.draw(canvas)
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val bmp = bitmap ?: return false
        val hw = bmp.width * scale / 2f
        val hh = bmp.height * scale / 2f

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val tx = event.x
                val ty = event.y
                val local = toLocal(tx, ty)
                val lx = local[0]; val ly = local[1]

                val hitImage  = lx in -hw..hw && ly in -hh..hh
                val hitScale  = StickerFeature.SCALE in features && dist(lx, ly, hw, hh) < handleRadius * 1.5f
                val hitRotate = StickerFeature.ROTATE in features && dist(lx, ly, hw, -hh) < handleRadius * 1.5f
                val hitFlip   = StickerFeature.FLIP in features && dist(lx, ly, -hw, hh) < handleRadius * 1.5f
                val hitDelete = StickerFeature.DELETE in features && dist(lx, ly, -hw, -hh) < handleRadius * 1.5f

                if (hitImage || hitScale || hitRotate || hitFlip || hitDelete) {
                    isSelected = true
                    invalidate()
                } else {
                    isSelected = false
                    invalidate()
                    onTouchedOutside?.invoke()
                    return false
                }

                touchMode = when {
                    hitDelete -> {
                        (parent as? android.view.ViewGroup)?.removeView(this)
                        TouchMode.NONE
                    }
                    hitFlip -> {
                        // Chỉ lật bitmap, handle giữ nguyên vì vẽ ngoài scope flip
                        flipX = !flipX
                        invalidate()
                        TouchMode.NONE
                    }
                    hitScale -> {
                        initScale = scale
                        initDist = dist(tx, ty, cx, cy)
                        TouchMode.SCALE
                    }
                    hitRotate -> {
                        initRotation = rotation
                        initAngle = angle(cx, cy, tx, ty)
                        TouchMode.ROTATE
                    }
                    StickerFeature.DRAG in features && hitImage -> {
                        lastTouchX = tx; lastTouchY = ty
                        TouchMode.DRAG
                    }
                    else -> TouchMode.NONE
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    TouchMode.DRAG -> {
                        val bmp2 = bitmap ?: return true
                        val hw2 = bmp2.width * scale / 2f
                        val hh2 = bmp2.height * scale / 2f
                        val newCx = cx + event.x - lastTouchX
                        val newCy = cy + event.y - lastTouchY
                        cx = if (hw2 <= width - hw2) newCx.coerceIn(hw2, width - hw2) else newCx
                        cy = if (hh2 <= height - hh2) newCy.coerceIn(hh2, height - hh2) else newCy
                        lastTouchX = event.x; lastTouchY = event.y
                        invalidate()
                    }
                    TouchMode.SCALE -> {
                        val newDist = dist(event.x, event.y, cx, cy)
                        if (initDist > 0) {
                            scale = (initScale * newDist / initDist).coerceIn(0.05f, 10f)
                            invalidate()
                        }
                    }
                    TouchMode.ROTATE -> {
                        val newAngle = angle(cx, cy, event.x, event.y)
                        rotation = initRotation + (newAngle - initAngle)
                        invalidate()
                    }
                    TouchMode.PINCH -> {
                        if (event.pointerCount >= 2) {
                            val newDist = dist(
                                event.getX(0), event.getY(0),
                                event.getX(1), event.getY(1)
                            )
                            if (pinchInitDist > 0) {
                                scale = (pinchInitScale * newDist / pinchInitDist).coerceIn(0.05f, 10f)
                                invalidate()
                            }
                        }
                    }
                    else -> {}
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // Ngón thứ 2 chạm xuống → chuyển sang PINCH
                if (event.pointerCount == 2) {
                    isSelected = true
                    touchMode = TouchMode.PINCH
                    pinchInitDist = dist(
                        event.getX(0), event.getY(0),
                        event.getX(1), event.getY(1)
                    )
                    pinchInitScale = scale
                    invalidate()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Ngón thứ 2 nhấc lên → quay về NONE, reset nếu cần
                if (touchMode == TouchMode.PINCH) {
                    touchMode = TouchMode.NONE
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchMode = TouchMode.NONE
            }
        }
        return true
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun toLocal(x: Float, y: Float): FloatArray {
        val dx = x - cx; val dy = y - cy
        val rad = Math.toRadians(-rotation.toDouble())
        return floatArrayOf(
            (dx * cos(rad) - dy * sin(rad)).toFloat(),
            (dx * sin(rad) + dy * cos(rad)).toFloat()
        )
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))

    private fun angle(cx: Float, cy: Float, x: Float, y: Float): Float =
        Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()
}
