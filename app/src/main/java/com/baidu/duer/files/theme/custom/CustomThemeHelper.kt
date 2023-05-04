package com.baidu.duer.files.theme.custom

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.baidu.duer.files.R
import com.baidu.duer.files.compat.recreateCompat
import com.baidu.duer.files.compat.setThemeCompat
import com.baidu.duer.files.compat.themeResIdCompat
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.theme.night.NightModeHelper
import com.baidu.duer.files.util.SimpleActivityLifecycleCallbacks
import com.baidu.duer.files.util.valueCompat

object CustomThemeHelper {
    private val activityBaseThemes = mutableMapOf<Activity, Int>()

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                check(activityBaseThemes.containsKey(activity)) {
                    "Activity must extend AppActivity: $activity"
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                activityBaseThemes.remove(activity)
            }
        })
    }

    fun apply(activity: Activity) {
        val baseThemeRes = activity.themeResIdCompat
        activityBaseThemes[activity] = baseThemeRes
        val customThemeRes = getCustomThemeRes(baseThemeRes, activity)
        activity.setThemeCompat(customThemeRes)
    }

    fun sync() {
        for ((activity, baseThemeRes) in activityBaseThemes) {
            val currentThemeRes = activity.themeResIdCompat
            val customThemeRes = getCustomThemeRes(baseThemeRes, activity)
            if (currentThemeRes != customThemeRes) {
                // Ignore ".Black" theme changes when not in night mode.
                if (!NightModeHelper.isInNightMode(activity as AppCompatActivity)
                    && isBlackThemeChange(currentThemeRes, customThemeRes, activity)
                ) {
                    continue
                }
                if (activity is OnThemeChangedListener) {
                    (activity as OnThemeChangedListener).onThemeChanged(customThemeRes)
                } else {
                    activity.recreateCompat()
                }
            }
        }
    }

    private fun getCustomThemeRes(@StyleRes baseThemeRes: Int, context: Context): Int {
        val resources = context.resources
        val baseThemeName = resources.getResourceName(baseThemeRes)
        val customThemeName = if (Settings.MATERIAL_DESIGN_3.valueCompat) {
            val defaultThemeName = resources.getResourceEntryName(R.style.Theme_MaterialFiles)
            val material3ThemeName =
                resources.getResourceEntryName(R.style.Theme_MaterialFiles_Material3)
            baseThemeName.replace(defaultThemeName, material3ThemeName)
        } else {
            val themeColorName =
                resources.getResourceEntryName(Settings.THEME_COLOR.valueCompat.resourceId)
            "$baseThemeName.$themeColorName"
        } + if (Settings.BLACK_NIGHT_MODE.valueCompat) ".Black" else ""
        return resources.getIdentifier(customThemeName, null, null)
    }

    private fun isBlackThemeChange(
        @StyleRes themeRes1: Int,
        @StyleRes themeRes2: Int,
        context: Context
    ): Boolean {
        val resources = context.resources
        val themeName1 = resources.getResourceName(themeRes1)
        val themeName2 = resources.getResourceName(themeRes2)
        return themeName1 == "$themeName2.Black" || themeName2 == "$themeName1.Black"
    }

    interface OnThemeChangedListener {
        fun onThemeChanged(@StyleRes theme: Int)
    }
}
