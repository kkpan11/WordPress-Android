package org.wordpress.android.util.crashlogging

import android.content.SharedPreferences
import com.automattic.android.tracks.crashlogging.CrashLoggingDataProvider
import com.automattic.android.tracks.crashlogging.CrashLoggingUser
import com.automattic.android.tracks.crashlogging.EventLevel
import com.automattic.android.tracks.crashlogging.ExtraKnownKey
import com.automattic.android.tracks.crashlogging.ReleaseName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.BuildConfig
import org.wordpress.android.R
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.AccountModel
import org.wordpress.android.fluxc.store.AccountStore
import org.wordpress.android.fluxc.store.AccountStore.OnAccountChanged
import org.wordpress.android.modules.APPLICATION_SCOPE
import org.wordpress.android.util.BuildConfigWrapper
import org.wordpress.android.util.EncryptedLogging
import org.wordpress.android.util.LocaleManagerWrapper
import org.wordpress.android.util.LogFileProviderWrapper
import org.wordpress.android.viewmodel.ResourceProvider
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class WPCrashLoggingDataProvider @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val resourceProvider: ResourceProvider,
    private val accountStore: AccountStore,
    private val localeManager: LocaleManagerWrapper,
    private val encryptedLogging: EncryptedLogging,
    private val logFileProvider: LogFileProviderWrapper,
    private val webviewVersionProvider: WebviewVersionProvider,
    private val buildConfig: BuildConfigWrapper,
    @Named(APPLICATION_SCOPE) private val appScope: CoroutineScope,
    wpPerformanceMonitoringConfig: WPPerformanceMonitoringConfig,
    dispatcher: Dispatcher,
) : CrashLoggingDataProvider {
    init {
        dispatcher.register(this)
    }

    override val buildType: String = BuildConfig.BUILD_TYPE
    override val enableCrashLoggingLogs: Boolean = false
    override val locale: Locale
        get() = localeManager.getLocale()
    override val releaseName: ReleaseName = if (buildConfig.isDebug()) {
        ReleaseName.SetByApplication("debug")
    } else {
        ReleaseName.SetByTracksLibrary
    }
    override val sentryDSN: String = BuildConfig.SENTRY_DSN

    override val applicationContextProvider = flowOf(mapOf(WEBVIEW_VERSION to webviewVersionProvider.getVersion()))

    override fun crashLoggingEnabled(): Boolean {
        if (buildConfig.isDebug()) {
            return false
        }

        val hasUserAllowedReporting = sharedPreferences.getBoolean(
            resourceProvider.getString(R.string.pref_key_send_crash),
            true
        )
        return hasUserAllowedReporting
    }

    override fun extraKnownKeys(): List<ExtraKnownKey> {
        return listOf(EXTRA_UUID)
    }

    /**
     * If Sentry is unable to upload the event in its first attempt, it'll call the `setBeforeSend` callback
     * before trying to send it again. This can be easily reproduced by turning off network connectivity
     * and re-launching the app over and over again which will hit this callback each time.
     *
     * The problem with that is it'll keep queuing more and more logs to be uploaded to MC and more
     * importantly, it'll set the `uuid` of the Sentry event to the log file at the time of the successful
     * Sentry request. Since we are interested in the logs for when the crash happened, this would not be
     * correct for us.
     *
     * We can simply fix this issue by checking if the [EXTRA_UUID] field is already set.
     */
    override fun provideExtrasForEvent(
        currentExtras: Map<ExtraKnownKey, String>,
        eventLevel: EventLevel
    ): Map<ExtraKnownKey, String> {
        return currentExtras + if (currentExtras[EXTRA_UUID] == null) {
            appendEncryptedLogsUuid(eventLevel)
        } else {
            emptyMap()
        }
    }

    private fun appendEncryptedLogsUuid(eventLevel: EventLevel): Map<ExtraKnownKey, String> {
        val encryptedLogsUuid = mutableMapOf<ExtraKnownKey, String>()
        logFileProvider.getLogFiles().lastOrNull()?.let { logFile ->
            if (logFile.exists()) {
                encryptedLogging.encryptAndUploadLogFile(
                    logFile = logFile,
                    shouldStartUploadImmediately = eventLevel != EventLevel.FATAL
                )?.let { uuid ->
                    encryptedLogsUuid.put(EXTRA_UUID, uuid)
                }
            }
        }
        return encryptedLogsUuid
    }

    override fun shouldDropWrappingException(module: String, type: String, value: String): Boolean {
        return module == EVENT_BUS_MODULE &&
                type == EVENT_BUS_EXCEPTION &&
                value == EVENT_BUS_INVOKING_SUBSCRIBER_FAILED_ERROR
    }

    override val user = MutableStateFlow(accountStore.account?.toCrashLoggingUser())

    override val performanceMonitoringConfig = wpPerformanceMonitoringConfig()

    @Suppress("unused", "unused_parameter")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAccountChanged(event: OnAccountChanged) {
        appScope.launch {
            user.emit(accountStore.account.toCrashLoggingUser())
        }
    }

    private fun AccountModel.toCrashLoggingUser(): CrashLoggingUser? {
        if (userId == 0L) return null

        return CrashLoggingUser(
            userID = userId.toString(),
            email = email,
            username = userName
        )
    }

    companion object {
        const val EXTRA_UUID = "uuid"
        const val WEBVIEW_VERSION = "webview.version"
        const val EVENT_BUS_MODULE = "org.greenrobot.eventbus"
        const val EVENT_BUS_EXCEPTION = "EventBusException"
        const val EVENT_BUS_INVOKING_SUBSCRIBER_FAILED_ERROR = "Invoking subscriber failed"
    }
}
