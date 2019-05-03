package org.wordpress.android.fluxc.store

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.persistence.InsightTypeSqlUtils
import org.wordpress.android.fluxc.store.StatsStore.InsightType.COMMENTS
import org.wordpress.android.fluxc.store.StatsStore.InsightType.FOLLOWERS
import org.wordpress.android.fluxc.store.StatsStore.InsightType.LATEST_POST_SUMMARY
import org.wordpress.android.fluxc.store.StatsStore.InsightType.POSTING_ACTIVITY
import org.wordpress.android.fluxc.test

@RunWith(MockitoJUnitRunner::class)
class StatsStoreTest {
    @Mock lateinit var site: SiteModel
    @Mock lateinit var insightTypesSqlUtils: InsightTypeSqlUtils
    private lateinit var store: StatsStore

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        store = StatsStore(
                Unconfined,
                insightTypesSqlUtils
        )
    }

    @Test
    fun `returns default stats types when DB is empty`() = test {
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(listOf())

        val result = store.getAddedInsights(site)

        assertThat(result).containsExactly(*DEFAULT_INSIGHTS.toTypedArray())
    }

    @Test
    fun `returns updated stats types from DB`() = test {
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(listOf(COMMENTS))

        val result = store.getAddedInsights(site)

        assertThat(result).containsExactly(COMMENTS)
    }

    @Test
    fun `updates types with added and removed`() = test {
        val addedTypes = listOf(
                COMMENTS
        )
        val removedTypes = store.getRemovedInsights(addedTypes)
        store.updateTypes(site, addedTypes)

        verify(insightTypesSqlUtils).insertOrReplaceAddedItems(site, addedTypes)
        verify(insightTypesSqlUtils).insertOrReplaceRemovedItems(site, removedTypes)
    }

    @Test
    fun `moves type up in the list when it is last`() = test {
        val insightType = listOf(
                LATEST_POST_SUMMARY,
                FOLLOWERS,
                COMMENTS
        )
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(insightType)

        store.moveTypeUp(site, COMMENTS)

        verify(insightTypesSqlUtils).insertOrReplaceAddedItems(site, listOf(LATEST_POST_SUMMARY, COMMENTS, FOLLOWERS))
    }

    @Test
    fun `does not move type up in the list when it is first`() = test {
        val insightType = listOf(
                COMMENTS,
                LATEST_POST_SUMMARY,
                FOLLOWERS
        )
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(insightType)

        store.moveTypeUp(site, COMMENTS)

        verify(insightTypesSqlUtils, never()).insertOrReplaceAddedItems(eq(site), any())
    }

    @Test
    fun `moves type down in the list when it is first`() = test {
        val insightType = listOf(
                LATEST_POST_SUMMARY,
                FOLLOWERS,
                COMMENTS
        )
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(insightType)

        store.moveTypeDown(site, LATEST_POST_SUMMARY)

        verify(insightTypesSqlUtils).insertOrReplaceAddedItems(site, listOf(FOLLOWERS, LATEST_POST_SUMMARY, COMMENTS))
    }

    @Test
    fun `does not move type down in the list when it is last`() = test {
        val insightType = listOf(
                COMMENTS,
                FOLLOWERS,
                LATEST_POST_SUMMARY
        )
        whenever(insightTypesSqlUtils.selectAddedItemsOrderedByStatus(site)).thenReturn(insightType)

        store.moveTypeDown(site, LATEST_POST_SUMMARY)

        verify(insightTypesSqlUtils, never()).insertOrReplaceAddedItems(eq(site), any())
    }

    @Test
    fun `removes type from list`() = test {
        store.removeType(site, LATEST_POST_SUMMARY)

        val addedTypes = DEFAULT_INSIGHTS - LATEST_POST_SUMMARY

        // executed twice, because the first time the default list is inserted first
        verify(insightTypesSqlUtils).insertOrReplaceAddedItems(
                site,
                addedTypes
        )

        verify(insightTypesSqlUtils).insertOrReplaceRemovedItems(
                site,
                store.getRemovedInsights(addedTypes)
        )

        store.removeType(site, POSTING_ACTIVITY)

        verify(insightTypesSqlUtils).insertOrReplaceAddedItems(
                site,
                addedTypes - POSTING_ACTIVITY
        )
        verify(insightTypesSqlUtils).insertOrReplaceRemovedItems(
                site,
                store.getRemovedInsights(addedTypes - POSTING_ACTIVITY)
        )
    }
}
