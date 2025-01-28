package org.wordpress.android.ui.deeplinks.handlers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.wordpress.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.wordpress.android.util.UriWrapper
import org.wordpress.android.viewmodel.Event
import javax.inject.Inject

class DeepLinkHandlers
@Inject constructor(
    editorLinkHandler: EditorLinkHandler,
    statsLinkHandler: StatsLinkHandler,
    startLinkHandler: StartLinkHandler,
    readerLinkHandler: ReaderLinkHandler,
    pagesLinkHandler: PagesLinkHandler,
    notificationsLinkHandler: NotificationsLinkHandler,
    qrCodeAuthLinkHandler: QRCodeAuthLinkHandler,
    homeLinkHandler: HomeLinkHandler,
    mediaLinkHandler: MediaLinkHandler,
    domainManagementLinkHandler: DomainManagementLinkHandler,
    qrCodeMediaLinkHandler: QRCodeMediaLinkHandler,
    siteMonitorLinkHandler: SiteMonitorLinkHandler
) {
    private val handlers = listOf(
        editorLinkHandler,
        statsLinkHandler,
        startLinkHandler,
        readerLinkHandler,
        pagesLinkHandler,
        notificationsLinkHandler,
        qrCodeAuthLinkHandler,
        homeLinkHandler,
        mediaLinkHandler,
        domainManagementLinkHandler,
        qrCodeMediaLinkHandler,
        siteMonitorLinkHandler
    )

    private val _toast by lazy {
        MediatorLiveData<Event<Int>>().also { mediator ->
            handlers.forEach {
                it.toast()?.let { toast ->
                    mediator.addSource(toast) { event ->
                        if (event != null) {
                            mediator.value = event
                        }
                    }
                }
            }
        }
    }
    val toast: LiveData<Event<Int>> = _toast

    fun buildNavigateAction(uri: UriWrapper): NavigateAction? {
        return handlers.firstOrNull { it.shouldHandleUrl(uri) }?.buildNavigateAction(uri)
    }

    fun isDeepLink(url: String): Boolean {
        return handlers.any { it.shouldHandleUrl(UriWrapper(url)) }
    }

    fun stripUrl(uri: UriWrapper): String? {
        return handlers.firstOrNull { it.shouldHandleUrl(uri) }?.stripUrl(uri)
    }
}
