package com.baidu.duer.files.app

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

abstract class AppActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus //这时能获取到焦点的View就是EditText
            if (isShouldHideKeyboard(v, ev)) {
                val imm = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                imm.hideSoftInputFromWindow(v!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                v.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            //只要点击了EditText周围的空白处，就返回true
            //return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
            return !(event.y > top && event.y < bottom)
        }
        return false
    }
}
