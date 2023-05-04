package com.baidu.duer.files.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.baidu.duer.files.compat.getDrawableCompat
import com.baidu.duer.files.compat.setTintCompat
import com.baidu.duer.files.navigation.StandardDirectoriesLiveData
import com.baidu.duer.files.navigation.StandardDirectory
import com.baidu.duer.files.navigation.getExternalStorageDirectory
import com.baidu.duer.files.ui.PreferenceFragmentCompat
import com.baidu.duer.files.util.getColorByAttr
import com.baidu.duer.files.util.valueCompat

class StandardDirectoryListPreferenceFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener {
    override fun onCreatePreferencesFix(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        StandardDirectoriesLiveData.observe(viewLifecycleOwner) { onStandardDirectoriesChanged(it) }
    }

    private fun onStandardDirectoriesChanged(
        standardDirectories: List<StandardDirectory>
    ) {
        val preferenceManager = preferenceManager
        val context = preferenceManager.context
        var preferenceScreen = preferenceScreen
        val oldPreferences = mutableMapOf<String, Preference>()
        if (preferenceScreen == null) {
            preferenceScreen = preferenceManager.createPreferenceScreen(context)
            setPreferenceScreen(preferenceScreen)
        } else {
            for (index in preferenceScreen.preferenceCount - 1 downTo 0) {
                val preference = preferenceScreen.getPreference(index)
                preferenceScreen.removePreference(preference)
                oldPreferences[preference.key] = preference
            }
        }
        val secondaryTextColor = context.getColorByAttr(android.R.attr.textColorSecondary)
        for (standardDirectory in standardDirectories) {
            val key = standardDirectory.key
            var preference = oldPreferences[key] as SwitchPreferenceCompat?
            if (preference == null) {
                preference = SwitchPreferenceCompat(context).apply {
                    this.key = key
                    isPersistent = false
                    onPreferenceClickListener = this@StandardDirectoryListPreferenceFragment
                }
            }
            preference.apply {
                icon = context.getDrawableCompat(standardDirectory.iconRes).apply {
                    mutate()
                    setTintCompat(secondaryTextColor)
                }
                title = standardDirectory.getTitle(context)
                summary = getExternalStorageDirectory(standardDirectory.relativePath)
                isChecked = standardDirectory.isEnabled
            }
            preferenceScreen.addPreference(preference)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        preference as SwitchPreferenceCompat
        val id = preference.key
        val isEnabled = preference.isChecked
        val settingsList = Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat.toMutableList()
        val index = settingsList.indexOfFirst { it.id == id }
        if (index != -1) {
            settingsList[index] = settingsList[index].copy(isEnabled = isEnabled)
        } else {
            val standardDirectory = StandardDirectoriesLiveData.valueCompat.find { it.key == id }!!
            settingsList += standardDirectory.toSettings().copy(isEnabled = isEnabled)
        }
        Settings.STANDARD_DIRECTORY_SETTINGS.putValue(settingsList)
        return true
    }
}
