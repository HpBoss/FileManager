package com.baidu.duer.files.compat

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.TintTypedArray
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColorStateList
import com.baidu.duer.files.R
import com.baidu.duer.files.hiddenapi.RestrictedHiddenApi
import com.baidu.duer.files.util.lazyReflectedMethod
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import java.util.concurrent.Executor
import kotlin.Float
import kotlin.Int
import kotlin.IntArray
import kotlin.OptIn
import kotlin.String
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.getValue

fun Context.checkSelfPermissionCompat(permission: String): Int =
    ContextCompat.checkSelfPermission(this, permission)

@ColorInt
fun Context.getColorCompat(@ColorRes id: Int): Int = getColorStateListCompat(id).defaultColor

fun Context.getColorStateListCompat(@ColorRes id: Int): ColorStateList =
    AppCompatResources.getColorStateList(this, id)!!

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable =
    AppCompatResources.getDrawable(this, id)!!

@SuppressLint("RestrictedApi")
fun Context.obtainStyledAttributesCompat(
    set: AttributeSet? = null,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): TintTypedArray =
    TintTypedArray.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes)

@OptIn(ExperimentalContracts::class)
@SuppressLint("RestrictedApi")
inline fun <R> TintTypedArray.use(block: (TintTypedArray) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        recycle()
    }
}

val Context.mainExecutorCompat: Executor
    get() = ContextCompat.getMainExecutor(this)

fun <T> Context.getSystemServiceCompat(serviceClass: Class<T>): T =
    ContextCompat.getSystemService(this, serviceClass)!!

@RestrictedHiddenApi
private val getThemeResIdMethod by lazyReflectedMethod(Context::class.java, "getThemeResId")

val Context.themeResIdCompat: Int
    @StyleRes
    get() = getThemeResIdMethod.invoke(this) as Int

/**
 * 避免每次手动创建带圆角的Drawable
 */
fun Context.createCornerDrawable(radius: Float): Drawable {
    val model = ShapeAppearanceModel()
        .toBuilder()
        .setAllCorners(CornerFamily.ROUNDED, radius)
        .build()
    val shape = MaterialShapeDrawable(model)
    shape.fillColor = getColorStateList(this, R.color.white)
    return shape
}

/**
 * 定义四个角任意圆角值的Drawable
 */
fun Context.createCornerDrawable(
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float
): Drawable {
    val model = ShapeAppearanceModel()
        .toBuilder()
        .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
        .setTopRightCorner(CornerFamily.ROUNDED, topRight)
        .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
        .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
        .build()
    val shape = MaterialShapeDrawable(model)
    shape.fillColor = getColorStateList(this, R.color.white)
    return shape
}
