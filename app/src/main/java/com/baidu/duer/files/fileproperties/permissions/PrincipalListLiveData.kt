package com.baidu.duer.files.fileproperties.permissions

import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.baidu.duer.files.app.packageManager
import com.baidu.duer.files.util.*

abstract class PrincipalListLiveData : MutableLiveData<Stateful<List<PrincipalItem>>>() {
    init {
        loadValue()
    }

    private fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val principals = androidPrincipals
                val androidIds = principals.mapTo(mutableSetOf()) { it.id }
                val installedApplicationInfos = packageManager.getInstalledApplications(0)
                val uidApplicationInfoMap = mutableMapOf<Int, MutableList<ApplicationInfo>>()
                for (applicationInfo in installedApplicationInfos) {
                    val uid = applicationInfo.uid
                    if (uid in androidIds) {
                        continue
                    }
                    uidApplicationInfoMap.getOrPut(uid) { mutableListOf() }.add(applicationInfo)
                }
                for ((uid, applicationInfos) in uidApplicationInfoMap) {
                    val principal = PrincipalItem(
                        uid, getAppPrincipalName(uid), applicationInfos,
                        applicationInfos.map { it.loadLabel(packageManager).toString() }
                    )
                    principals.add(principal)
                }
                principals.sortBy { it.id }
                Success(principals as List<PrincipalItem>)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    @get:Throws(Exception::class)
    protected abstract val androidPrincipals: MutableList<PrincipalItem>

    protected abstract fun getAppPrincipalName(uid: Int): String

    companion object {
        @JvmStatic
        protected val AID_USER_OFFSET = 100000

        @JvmStatic
        protected val AID_APP_START = 10000

        @JvmStatic
        protected val AID_CACHE_GID_START = 20000

        @JvmStatic
        protected val AID_CACHE_GID_END = 29999

        @JvmStatic
        protected val AID_SHARED_GID_START = 50000

        @JvmStatic
        protected val AID_SHARED_GID_END = 59999

        @JvmStatic
        protected val AID_ISOLATED_START = 99000
    }
}
