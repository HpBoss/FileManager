package com.baidu.duer.files.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.baidu.duer.files.R

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/15
 * @Description : 可对带边框的TextView，随意组合设定四个角的radius
 */
class RoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val mPaint by lazy { Paint() }
    private val mRectF by lazy { RectF() }
    private val mOriginRectF by lazy { RectF() }
    private val mPath by lazy { Path() }
    private val mTempPath by lazy { Path() }
    private var mXfermode: Xfermode? = null
    private val mRadii by lazy { FloatArray(8) }
    private var mStrokeColor: Int = Color.WHITE
    private val mStrokeWidth: Float
    private val mRadiusTopLeft: Float
    private val mRadiusTopRight: Float
    private val mRadiusBottomLeft: Float
    private val mRadiusBottomRight: Float

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setRadius()
        mRectF.set(mStrokeWidth, mStrokeWidth, w - mStrokeWidth, h - mStrokeWidth)
        mOriginRectF.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun draw(canvas: Canvas) {
        canvas.saveLayer(mRectF, null)
        super.draw(canvas)
        mPaint.apply {
            reset()
            isAntiAlias = true
            style = Paint.Style.FILL
            xfermode = mXfermode
        }

        mPath.apply {
            reset()
            addRoundRect(mRectF, mRadii, Path.Direction.CCW)
        }

        mTempPath.apply {
            reset()
            addRect(mOriginRectF, Path.Direction.CCW)
            op(mPath, Path.Op.DIFFERENCE)
        }
        canvas.drawPath(mTempPath, mPaint)
        canvas.restore()
        mPaint.xfermode = null
    }

    private fun setRadius() {
        mRadii[0] = mRadiusTopLeft - mStrokeWidth
        mRadii[2] = mRadiusTopRight - mStrokeWidth
        mRadii[4] = mRadiusBottomRight - mStrokeWidth
        mRadii[6] = mRadiusBottomLeft - mStrokeWidth
        mRadii[1] = mRadii[0]
        mRadii[3] = mRadii[2]
        mRadii[5] = mRadii[4]
        mRadii[7] = mRadii[6]
    }

    init {
        // 背景不能为null
        if (background == null) {
            setBackgroundColor(Color.parseColor("#00000000"))
        }
        setLayerType(LAYER_TYPE_NONE, null)
        mXfermode = PorterDuffXfermode(
            PorterDuff.Mode.DST_OUT
        )

        val array = context.obtainStyledAttributes(
            attrs, R.styleable.RoundTextView, defStyleAttr, 0
        )

        mRadiusTopLeft = array.getDimension(R.styleable.RoundTextView_rTopLeftRadius, 0f)
        mRadiusTopRight = array.getDimension(R.styleable.RoundTextView_rTopRightRadius, 0f)
        mRadiusBottomLeft = array.getDimension(R.styleable.RoundTextView_rBottomLeftRadius, 0f)
        mRadiusBottomRight = array.getDimension(R.styleable.RoundTextView_rBottomRightRadius, 0f)
        mStrokeWidth = array.getDimension(R.styleable.RoundTextView_rStrokeWidth, 0f)
        mStrokeColor = array.getColor(R.styleable.RoundTextView_rStrokeColor, mStrokeColor)

        array.recycle()
    }
}