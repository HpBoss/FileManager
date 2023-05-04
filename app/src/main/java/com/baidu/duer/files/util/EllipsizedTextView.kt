package com.baidu.duer.files.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.baidu.duer.files.R

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/25
 * @Description :
 */
class EllipsizedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    var ellipsis = getDefaultEllipsis().toString()
    var ellipsisColor = getDefaultEllipsisColor()

    private val ellipsisSpannable: SpannableString
    private val spannableStringBuilder = SpannableStringBuilder()

    init {
        if (attrs != null) {
            val typedArray =
                context.theme.obtainStyledAttributes(attrs, R.styleable.EllipsizedTextView, 0, 0)
            typedArray.let {
                ellipsis = typedArray.getString(R.styleable.EllipsizedTextView_ellipsis)
                    ?: getDefaultEllipsis().toString()
                ellipsisColor = typedArray.getColor(
                    R.styleable.EllipsizedTextView_ellipsisColor,
                    getDefaultEllipsisColor()
                )
                typedArray.recycle()
            }
        }

        ellipsisSpannable = SpannableString(ellipsis)
        ellipsisSpannable.setSpan(
            ForegroundColorSpan(ellipsisColor),
            0,
            ellipsis.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val availableScreenWidth =
            measuredWidth - compoundPaddingLeft.toFloat() - compoundPaddingRight.toFloat()
        var availableTextWidth = availableScreenWidth * maxLines
        var ellipsizedText = TextUtils.ellipsize(text, paint, availableTextWidth, ellipsize)

        if (ellipsizedText.toString() != text.toString()) {
            availableTextWidth = (availableScreenWidth - paint.measureText(ellipsis)) * maxLines
            ellipsizedText = TextUtils.ellipsize(text, paint, availableTextWidth, ellipsize)
            text = if (ellipsizedText.isEmpty()) {
                TextUtils.ellipsize(text, paint, availableTextWidth, TextUtils.TruncateAt.END)
            } else {
                val defaultEllipsisStart = ellipsizedText.indexOf(getDefaultEllipsis())
                val defaultEllipsisEnd = defaultEllipsisStart + 1

                spannableStringBuilder.clear()
                spannableStringBuilder.append(ellipsizedText)
                    .replace(defaultEllipsisStart, defaultEllipsisEnd, ellipsisSpannable)
            }
        }
    }

    private fun getDefaultEllipsis(): Char {
        return Typography.ellipsis
    }

    private fun getDefaultEllipsisColor(): Int {
        return textColors.defaultColor
    }
}