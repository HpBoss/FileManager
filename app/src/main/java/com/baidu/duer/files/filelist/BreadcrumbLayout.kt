package com.baidu.duer.files.filelist

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.BreadcrumbItemBinding
import com.baidu.duer.files.util.*
import java8.nio.file.Path

class BreadcrumbLayout : HorizontalScrollView {
    private val tabLayoutHeight = context.getDimensionPixelSize(R.dimen.tab_layout_height)

    // Using a color state list resource somehow results in red color in dark mode on API 21.
    // Run `git revert 5bb2fd1` once we no longer support API 21.
    private val itemColor =
        ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(
                ContextCompat.getColor(context, R.color.bread_crumb_path_text_activated_color),
                ContextCompat.getColor(context, R.color.bread_crumb_path_text_normal_color)
            )
        )
    private val popupContext =
        context.withTheme(context.getResourceIdByAttr(R.attr.actionBarPopupTheme))

    private val itemsLayout: LinearLayout

    private lateinit var listener: Listener
    private lateinit var data: BreadcrumbData

    private var isLayoutDirty = true
    private var isScrollToSelectedItemPending = false
    private var isFirstScroll = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isHorizontalScrollBarEnabled = false
        itemsLayout = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        itemsLayout.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        setPaddingRelative(0, 0, 0, 0)
        addView(itemsLayout, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    override fun jumpDrawablesToCurrentState() {
        // HACK: AppBarLayout.updateAppBarLayoutDrawableState() calls
        // CoordinatorLayout.jumpDrawablesToCurrentState() to fix a pre-N visual bug according to a
        // comment in AppBarLayout.BaseBehavior.onLayoutChild(), however that results in our ripple
        // disappearing. One way to ignore that call path is to skip when we are in layout, so that
        // we at least preserve the other call path upon being attached to window.
        if (isInLayout) {
            return
        }
        super.jumpDrawablesToCurrentState()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightMeasureSpec = heightMeasureSpec
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            var height = tabLayoutHeight
            if (heightMode == MeasureSpec.AT_MOST) {
                height = height.coerceAtMost(MeasureSpec.getSize(heightMeasureSpec))
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun requestLayout() {
        isLayoutDirty = true

        super.requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        isLayoutDirty = false
        if (isScrollToSelectedItemPending) {
            scrollToSelectedItem()
            isScrollToSelectedItemPending = false
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setData(data: BreadcrumbData) {
        if (this::data.isInitialized && this.data == data) {
            return
        }
        this.data = data
        if (data.paths.isEmpty()) this.visibility = GONE
        inflateItemViews()
        bindItemViews()
        scrollToSelectedItem()
    }

    private fun scrollToSelectedItem() {
        if (isLayoutDirty || data.paths.isEmpty()) {
            isScrollToSelectedItemPending = true
            return
        }
        val selectedItemView = itemsLayout.getChildAt(data.selectedIndex)
        val scrollX = if (layoutDirection == View.LAYOUT_DIRECTION_LTR) {
            selectedItemView.left - itemsLayout.paddingStart
        } else {
            selectedItemView.right - width + itemsLayout.paddingStart
        }
        if (!isFirstScroll && isShown) {
            smoothScrollTo(scrollX, 0)
        } else {
            scrollTo(scrollX, 0)
        }
        isFirstScroll = false
    }

    private fun inflateItemViews() {
        // HACK: Remove/add views at the front so that ripple remains correct, as we are potentially
        // collapsing/expanding breadcrumbs at the front.
        for (index in data.paths.size until itemsLayout.childCount) {
            itemsLayout.removeViewAt(0)
        }
        for (index in itemsLayout.childCount until data.paths.size) {
            val binding = BreadcrumbItemBinding.inflate(context.layoutInflater, itemsLayout, false)
            val menu = PopupMenu(popupContext, binding.root)
                .apply { inflate(R.menu.file_list_breadcrumb) }
            binding.text.setTextColor(itemColor)
            binding.root.tag = binding to menu
            itemsLayout.addView(binding.root, 0)
        }
    }

    private fun bindItemViews() {
        for (index in data.paths.indices) {
            @Suppress("UNCHECKED_CAST")
            val tag = itemsLayout.getChildAt(index).tag as Pair<BreadcrumbItemBinding, PopupMenu>
            val (binding, menu) = tag
            binding.text.text = if (data.paths.size <= 1) {
                this.visibility = View.GONE
                binding.text.background = null
                ""
            } else {
                binding.text.background =
                    ContextCompat.getDrawable(context, R.drawable.bread_crumb_path_background)
                this.visibility = View.VISIBLE
                data.nameProducers[index](binding.text.context)
            }
            binding.arrowImage.isVisible = index != data.paths.size - 1
            binding.root.isActivated = index == data.selectedIndex
            if (index == data.selectedIndex) {
                binding.text.setPadding(14.px, 0, 14.px, 0)
                binding.text.typeface = Typeface.DEFAULT
            } else {
                binding.text.setPadding(0)
                binding.text.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
            val path = data.paths[index]
            binding.root.setOnClickListener {
                if (data.selectedIndex == index) {
                    scrollToSelectedItem()
                } else {
                    listener.navigateTo(path)
                }
            }
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_copy_path -> {
                        listener.copyPath(path)
                        true
                    }
                    R.id.action_open_in_new_task -> {
                        listener.openInNewTask(path)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    interface Listener {
        fun navigateTo(path: Path)
        fun copyPath(path: Path)
        fun openInNewTask(path: Path)
    }
}
