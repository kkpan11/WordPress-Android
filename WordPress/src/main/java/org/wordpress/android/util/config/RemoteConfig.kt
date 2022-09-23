package org.wordpress.android.util.config

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.wordpress.android.BuildConfig
import org.wordpress.android.analytics.AnalyticsTracker
import org.wordpress.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.fluxc.persistence.RemoteConfigDao
import org.wordpress.android.fluxc.store.NotificationStore.Companion.WPCOM_PUSH_DEVICE_UUID
import org.wordpress.android.fluxc.store.mobile.FeatureFlagsStore
import org.wordpress.android.fluxc.utils.PreferenceUtils
import org.wordpress.android.modules.APPLICATION_SCOPE
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T.UTILS
import org.wordpress.android.util.config.AppConfig.FeatureState
import javax.inject.Inject
import javax.inject.Named

const val REMOTE_REFRESH_INTERVAL_IN_HOURS = 12
const val REMOTE_FLAG_PLATFORM_PARAMETER ="android"

/**
 * Do not use this class outside of this package. Use [AppConfig] instead
 */
class RemoteConfig
@Inject constructor(
    private val featureFlagStore: FeatureFlagsStore,
    private val context: Context,
    @Named(APPLICATION_SCOPE) private val appScope: CoroutineScope
) {
    private val preferences by lazy { PreferenceUtils.getFluxCPreferences(context) }

    lateinit var flags: List<RemoteConfigDao.RemoteConfig>

    fun init(appScope: CoroutineScope) {
        Log.e("Fetching remote flags", "initiated")
        appScope.launch {
            flags = featureFlagStore.getFeatureFlags()
        }
    }

    private suspend fun fetchRemoteFlags() {
        Log.e("Refreshing remote flags", " ")
        val response = featureFlagStore.fetchFeatureFlags(
                buildNumber = BuildConfig.VERSION_CODE.toString(),
                deviceId = preferences.getString(WPCOM_PUSH_DEVICE_UUID, "")?:"",
                identifier = BuildConfig.APPLICATION_ID,
                marketingVersion = BuildConfig.VERSION_NAME,
                platform = REMOTE_FLAG_PLATFORM_PARAMETER
        )
        Log.e("response", response.toString())
        response.featureFlags?.let { configValues ->
            Log.e("Remote config values", configValues.toString())
            AnalyticsTracker.track(
                    Stat.FEATURE_FLAGS_SYNCED_STATE,
                    configValues
            )
        }
        if (response.isError) {
            AppLog.e(
                    UTILS,
                    "Remote config sync failed"
            )
        }
    }

    fun refresh(appScope: CoroutineScope, forced: Boolean) {
        Log.e("refresh remote flags", "function called")
        appScope.launch {
            if (isRefreshNeeded() || forced) {
                fetchRemoteFlags()
                flags = featureFlagStore.getFeatureFlags()
            }
        }
    }

    fun isEnabled(field: String): Boolean = FirebaseRemoteConfig.getInstance().getBoolean(field)
    fun getString(field: String): String = FirebaseRemoteConfig.getInstance().getString(field)
    fun getFeatureState(remoteField: String, buildConfigValue: Boolean): FeatureState {
        val remoteConfig = flags.find { it.key == remoteField }
        return if (remoteConfig == null) {
            appScope.launch { featureFlagStore.insertRemoteConfigValue(remoteField, buildConfigValue) }
            FeatureState.BuildConfigValue(buildConfigValue)
        } else {
            FeatureState.RemoteValue(remoteConfig.value)
        }
    }

    private fun isRefreshNeeded(): Boolean {
        val lastModifiedFlag = featureFlagStore.getTheLastSyncedRemoteConfig()
        val timeDifferenceInMilliSeconds = System.currentTimeMillis() - lastModifiedFlag
        val differenceInHours = (timeDifferenceInMilliSeconds / (60 * 60 * 1000) % 24)
        if (differenceInHours >= REMOTE_REFRESH_INTERVAL_IN_HOURS) return true
        return false
    }

    fun clear() {
        Log.e("clearing", "the remote config values")
        flags = emptyList()
        featureFlagStore.clearAllValues()
    }
}

