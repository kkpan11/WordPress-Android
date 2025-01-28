package org.wordpress.android.viewmodel.pages

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.BACKGROUND
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.R
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.CauseOfOnPostChanged
import org.wordpress.android.fluxc.model.LocalOrRemoteId.LocalId
import org.wordpress.android.fluxc.model.LocalOrRemoteId.RemoteId
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.MediaStore.OnMediaChanged
import org.wordpress.android.fluxc.store.MediaStore.OnMediaUploaded
import org.wordpress.android.fluxc.store.PostStore
import org.wordpress.android.fluxc.store.PostStore.OnPostChanged
import org.wordpress.android.fluxc.store.PostStore.OnPostUploaded
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnSiteChanged
import org.wordpress.android.ui.posts.PostUtils
import org.wordpress.android.ui.uploads.PostEvents
import org.wordpress.android.ui.uploads.ProgressEvent
import org.wordpress.android.ui.uploads.UploadService
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T
import org.wordpress.android.util.EventBusWrapper
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import org.wordpress.android.ui.utils.UiString.UiStringRes

/**
 * This is a temporary class to make the PagesViewModel more manageable. It was inspired by the PostListEventListener
 */
@Suppress("LongParameterList")
class PageListEventListener(
    private val dispatcher: Dispatcher,
    private val bgDispatcher: CoroutineDispatcher,
    private val postStore: PostStore,
    private val eventBusWrapper: EventBusWrapper,
    private val siteStore: SiteStore,
    private val site: SiteModel,
    private val handleRemoteAutoSave: (LocalId, Boolean) -> Unit,
    private val handlePostUploadFinished: (RemoteId, PageUploadErrorWrapper, Boolean) -> Unit,
    private val invalidateUploadStatus: (List<LocalId>) -> Unit,
    private val handleHomepageSettingsChange: (SiteModel) -> Unit,
    private val handlePageUpdatedWithoutError: () -> Unit
) : CoroutineScope {
    init {
        dispatcher.register(this)
        eventBusWrapper.register(this)
    }

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job

    /**
     * Handles the onDestroy event to cleanup the registration for dispatcher and cancelling any pending jobs.
     */
    fun onDestroy() {
        job.cancel()
        dispatcher.unregister(this)
        eventBusWrapper.unregister(this)
    }

    /**
     * Has lower priority than the PostUploadHandler and UploadService, which ensures that they already processed this
     * OnPostChanged event. This means we can safely rely on their internal state being up to date.
     */
    @Suppress("unused")
    @Subscribe(threadMode = MAIN, priority = 5)
    fun onPostChanged(event: OnPostChanged) {
        // We need to subscribe on the MAIN thread, in order to ensure the priority parameter is taken into account.
        // However, we want to perform the body of the method on a background thread.
        launch {
            when (event.causeOfChange) {
                // Fetched post list event will be handled by OnListChanged
                is CauseOfOnPostChanged.RemoteAutoSavePost -> {
                    if (event.isError) {
                        AppLog.d(
                            T.POSTS, "REMOTE_AUTO_SAVE_POST failed: " +
                                    event.error.type + " - " + event.error.message
                        )
                    }

                    uploadStatusChanged(
                        LocalId((event.causeOfChange as CauseOfOnPostChanged.RemoteAutoSavePost).localPostId)
                    )
                    handleRemoteAutoSave.invoke(
                        LocalId((event.causeOfChange as CauseOfOnPostChanged.RemoteAutoSavePost).localPostId),
                        event.isError
                    )
                }

                is CauseOfOnPostChanged.UpdatePost -> {
                    if (event.isError) {
                        AppLog.e(
                            T.POSTS,
                            "Error updating the post with type: ${event.error.type} and" +
                                    " message: ${event.error.message}"
                        )
                    } else {
                        handlePageUpdatedWithoutError.invoke()
                    }
                    uploadStatusChanged(LocalId((event.causeOfChange as CauseOfOnPostChanged.UpdatePost).localPostId))
                }
                is CauseOfOnPostChanged.DeletePost -> Unit // Do nothing
                is CauseOfOnPostChanged.RestorePost -> Unit // Do nothing
                is CauseOfOnPostChanged.FetchPages -> Unit // Do nothing
                is CauseOfOnPostChanged.FetchPosts -> Unit // Do nothing
                is CauseOfOnPostChanged.RemoveAllPosts -> Unit // Do nothing
                is CauseOfOnPostChanged.RemovePost -> Unit // Do nothing
                is CauseOfOnPostChanged.FetchPostLikes -> Unit // Do nothing
            }
        }
    }

    @Suppress("unused", "SpreadOperator")
    @Subscribe(threadMode = BACKGROUND)
    fun onMediaChanged(event: OnMediaChanged) {
        uploadStatusChanged(*event.mediaList.map { LocalId(it.localPostId) }.toTypedArray())
    }

    @Suppress("unused")
    @Subscribe(threadMode = BACKGROUND)
    fun onPostUploaded(event: OnPostUploaded) {
        if (event.post != null && event.post.isPage && event.post.localSiteId == site.id) {
            uploadStatusChanged(LocalId(event.post.id))
            val errorMessage = getPageUploadErrorMessage(event)
            handlePostUploadFinished(
                RemoteId(event.post.remotePostId),
                PageUploadErrorWrapper(event.isError, errorMessage, shouldRetryAfterPageUploadError(event)),
                event.isFirstTimePublish
            )
        }
    }

    private fun getPageUploadErrorMessage(event: OnPostUploaded): UiString? {
        return when {
            event.error?.type == PostStore.PostErrorType.OLD_REVISION ->
                UiStringRes(R.string.page_upload_conflict_error)
            event.error?.message != null ->
                UiString.UiStringText(event.error.message)
            else -> null
        }
    }

    private fun shouldRetryAfterPageUploadError(event: OnPostUploaded): Boolean {
        return event.error?.type != PostStore.PostErrorType.OLD_REVISION
    }

    @Suppress("unused")
    @Subscribe(threadMode = BACKGROUND)
    fun onMediaUploaded(event: OnMediaUploaded) {
        val media = event.media
        if (media != null && media.localPostId != 0 && site.id == media.localSiteId) {
            uploadStatusChanged(LocalId(media.localPostId))
        }
    }

    /**
     * Upload started, reload so correct status on uploading post appears
     */
    @Suppress("unused")
    @Subscribe(threadMode = BACKGROUND)
    fun onEventBackgroundThread(event: PostEvents.PostUploadStarted) {
        if (event.post != null && event.post.isPage && event.post.localSiteId == site.id) {
            uploadStatusChanged(LocalId(event.post.id))
        }
    }

    /**
     * Upload cancelled (probably due to failed media), reload so correct status on uploading post appears
     */
    @Suppress("unused")
    @Subscribe(threadMode = BACKGROUND)
    fun onEventBackgroundThread(event: PostEvents.PostUploadCanceled) {
        if (event.post != null && event.post.isPage && event.post.localSiteId == site.id) {
            uploadStatusChanged(LocalId(event.post.id))
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = BACKGROUND)
    fun onEventBackgroundThread(event: ProgressEvent) {
        if (site.id == event.media.localSiteId) {
            uploadStatusChanged(LocalId(event.media.localPostId))
        }
    }

    @Suppress("unused", "SpreadOperator")
    @Subscribe(threadMode = BACKGROUND)
    fun onEventBackgroundThread(event: UploadService.UploadMediaRetryEvent) {
        if (event.mediaModelList != null && event.mediaModelList.isNotEmpty()) {
            // if there' a Post to which the retried media belongs, clear their status
            val postsToRefresh = PostUtils.getPostsThatIncludeAnyOfTheseMedia(postStore, event.mediaModelList)
            uploadStatusChanged(*postsToRefresh.map { LocalId(it.id) }.toTypedArray())
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = MAIN)
    fun onSiteChanged(event: OnSiteChanged) {
        if (!event.isError && siteStore.hasSiteWithLocalId(site.id)) {
            siteStore.getSiteByLocalId(site.id)?.let { updatedSite ->
                if (updatedSite.showOnFront != site.showOnFront ||
                    updatedSite.pageForPosts != site.pageForPosts ||
                    updatedSite.pageOnFront != site.pageForPosts
                ) {
                    handleHomepageSettingsChange(updatedSite)
                }
            }
        }
    }

    private fun uploadStatusChanged(vararg localPostIds: LocalId) {
        invalidateUploadStatus.invoke(localPostIds.toList())
    }

    class Factory @Inject constructor() {
        @Suppress("LongParameterList")
        fun createAndStartListening(
            dispatcher: Dispatcher,
            bgDispatcher: CoroutineDispatcher,
            postStore: PostStore,
            eventBusWrapper: EventBusWrapper,
            siteStore: SiteStore,
            site: SiteModel,
            invalidateUploadStatus: (List<LocalId>) -> Unit,
            handleRemoteAutoSave: (LocalId, Boolean) -> Unit,
            handlePostUploadFinished: (RemoteId, PageUploadErrorWrapper, Boolean) -> Unit,
            handleHomepageSettingsChange: (SiteModel) -> Unit,
            handlePageUpdatedWithoutError: () -> Unit,
        ): PageListEventListener {
            return PageListEventListener(
                dispatcher = dispatcher,
                bgDispatcher = bgDispatcher,
                postStore = postStore,
                eventBusWrapper = eventBusWrapper,
                siteStore = siteStore,
                site = site,
                invalidateUploadStatus = invalidateUploadStatus,
                handleRemoteAutoSave = handleRemoteAutoSave,
                handlePostUploadFinished = handlePostUploadFinished,
                handleHomepageSettingsChange = handleHomepageSettingsChange,
                handlePageUpdatedWithoutError = handlePageUpdatedWithoutError
            )
        }
    }
}
