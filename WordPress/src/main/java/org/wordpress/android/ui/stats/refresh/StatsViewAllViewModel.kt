package org.wordpress.android.ui.stats.refresh

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.wordpress.android.R
import org.wordpress.android.ui.pages.SnackbarMessageHolder
import org.wordpress.android.ui.stats.refresh.StatsViewModel.DateSelectorUiModel
import org.wordpress.android.ui.stats.refresh.lists.StatsBlock
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.EMPTY
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.ERROR
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.LOADING
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.SUCCESS
import org.wordpress.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider.SelectedDate
import org.wordpress.android.ui.stats.refresh.utils.StatsDateSelector
import org.wordpress.android.ui.stats.refresh.utils.StatsSiteProvider
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.util.mapNullable
import org.wordpress.android.util.mapSafe
import org.wordpress.android.util.throttle
import org.wordpress.android.viewmodel.Event
import org.wordpress.android.viewmodel.ScopedViewModel

class StatsViewAllViewModel(
    val mainDispatcher: CoroutineDispatcher,
    val bgDispatcher: CoroutineDispatcher,
    val useCase: BaseStatsUseCase<*, *>,
    private val statsSiteProvider: StatsSiteProvider,
    private val dateSelector: StatsDateSelector?,
    @StringRes val title: Int
) : ScopedViewModel(mainDispatcher) {
    val selectedDate = dateSelector?.selectedDate

    val dateSelectorData: LiveData<DateSelectorUiModel> = dateSelector?.dateSelectorData?.mapNullable {
        it ?: DateSelectorUiModel(false)
    } ?:  MutableLiveData(DateSelectorUiModel(false))

    val navigationTarget: LiveData<Event<NavigationTarget>> = useCase.navigationTarget

    val data: LiveData<StatsBlock> = useCase.liveData.mapSafe { useCaseModel ->
        when (useCaseModel.state) {
            SUCCESS -> StatsBlock.Success(useCaseModel.type, useCaseModel.data ?: listOf())
            ERROR -> StatsBlock.Error(useCaseModel.type, useCaseModel.stateData ?: useCaseModel.data ?: listOf())
            LOADING -> StatsBlock.Loading(useCaseModel.type, useCaseModel.data ?: useCaseModel.stateData ?: listOf())
            EMPTY -> StatsBlock.EmptyBlock(useCaseModel.type, useCaseModel.stateData ?: useCaseModel.data ?: listOf())
        }
    }.throttle(viewModelScope, true)

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _showSnackbarMessage = MutableLiveData<Event<SnackbarMessageHolder>>()
    val showSnackbarMessage: LiveData<Event<SnackbarMessageHolder>> = _showSnackbarMessage

    val toolbarHasShadow = dateSelectorData.mapSafe { !it.isVisible }

    fun start(startDate: SelectedDate?) {
        launch {
            startDate?.let {
                dateSelector?.start(startDate)
            }
            loadData(refresh = false, forced = false)
            dateSelector?.updateDateSelector()
        }
        dateSelector?.updateDateSelector()
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun onPullToRefresh() {
        _showSnackbarMessage.value = null
        refreshData()
    }

    private fun loadData(executeLoading: suspend () -> Unit) = launch {
        _isRefreshing.value = true

        executeLoading()

        _isRefreshing.value = false
    }

    private suspend fun loadData(refresh: Boolean, forced: Boolean) {
        withContext(bgDispatcher) {
            if (statsSiteProvider.hasLoadedSite()) {
                useCase.fetch(refresh, forced)
            } else {
                _showSnackbarMessage.postValue(
                    Event(SnackbarMessageHolder(UiStringRes(R.string.stats_site_not_loaded_yet)))
                )
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    override fun onCleared() {
        _showSnackbarMessage.value = null
        useCase.clear()
        statsSiteProvider.reset()
    }

    fun onRetryClick() {
        refreshData()
    }

    fun onNextDateSelected() {
        launch(mainDispatcher) {
            dateSelector?.onNextDateSelected()
        }
    }

    fun onPreviousDateSelected() {
        launch(mainDispatcher) {
            dateSelector?.onPreviousDateSelected()
        }
    }

    fun onDateChanged() {
        loadData {
            loadData(refresh = true, forced = false)
        }
    }

    private fun refreshData() {
        loadData {
            loadData(refresh = true, forced = true)
        }
    }

    fun getSelectedDate(): SelectedDate? {
        return dateSelector?.getSelectedDate()
    }
}
