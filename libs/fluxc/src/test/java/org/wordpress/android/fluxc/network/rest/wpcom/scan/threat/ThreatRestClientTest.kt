package org.wordpress.android.fluxc.network.rest.wpcom.scan.threat

import com.android.volley.RequestQueue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.UnitTestUtils
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.scan.threat.ThreatMapper
import org.wordpress.android.fluxc.model.scan.threat.ThreatModel
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequest.WPComGsonNetworkError
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequestBuilder
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequestBuilder.Response
import org.wordpress.android.fluxc.network.rest.wpcom.WPComGsonRequestBuilder.Response.Success
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AccessToken
import org.wordpress.android.fluxc.store.ThreatStore.ThreatErrorType
import org.wordpress.android.fluxc.test

@RunWith(MockitoJUnitRunner::class)
class ThreatRestClientTest {
    @Mock private lateinit var wpComGsonRequestBuilder: WPComGsonRequestBuilder
    @Mock private lateinit var dispatcher: Dispatcher
    @Mock private lateinit var requestQueue: RequestQueue
    @Mock private lateinit var accessToken: AccessToken
    @Mock private lateinit var userAgent: UserAgent
    @Mock private lateinit var site: SiteModel
    @Mock private lateinit var threat: ThreatModel
    @Mock private lateinit var threatMapper: ThreatMapper

    private lateinit var urlCaptor: KArgumentCaptor<String>
    private lateinit var threatRestClient: ThreatRestClient
    private val siteId: Long = 12
    private val threatId: Long = 1

    @Before
    fun setUp() {
        urlCaptor = argumentCaptor()
        threatRestClient = ThreatRestClient(
            wpComGsonRequestBuilder,
            threatMapper,
            dispatcher,
            null,
            requestQueue,
            accessToken,
            userAgent
        )
    }

    @Test
    fun `fetch threat builds correct request url`() = test {
        val fetchThreatResponseJson = UnitTestUtils.getStringFromResourceFile(javaClass, THREAT_SUCCESS_RESPONSE_JSON)
        val fetchThreatResponseResponse = getThreatResponseFromJsonString(fetchThreatResponseJson)
        initFetchThreat(fetchThreatResponseResponse)

        threatRestClient.fetchThreat(site, threat)

        assertEquals(urlCaptor.firstValue, "$API_BASE_PATH/sites/${site.siteId}/scan/threat/${threat.id}/")
    }

    @Test
    fun `fetch threat dispatches response on success`() = test {
        val successResponseJson =
            UnitTestUtils.getStringFromResourceFile(javaClass, THREAT_SUCCESS_RESPONSE_JSON)
        val threatResponse = getThreatResponseFromJsonString(successResponseJson)
        initFetchThreat(threatResponse)

        val payload = threatRestClient.fetchThreat(site, threat)

        with(payload) {
            assertEquals(site, this@ThreatRestClientTest.site)
            assertNull(error)
            assertNotNull(threatModel)
        }
    }

    @Test
    fun `fetch threat dispatches api error on failure from api`() = test {
        val threatResponse = ThreatResponse(success = false)
        initFetchThreat(threatResponse)

        val payload = threatRestClient.fetchThreat(site, threat)

        with(payload) {
            assertEquals(site, this@ThreatRestClientTest.site)
            assertTrue(isError)
            assertEquals(ThreatErrorType.API_ERROR, error.type)
        }
    }

    private fun getThreatResponseFromJsonString(json: String): ThreatResponse {
        val responseType = object : TypeToken<ThreatResponse>() {}.type
        return Gson().fromJson(json, responseType) as ThreatResponse
    }

    private suspend fun initFetchThreat(
        data: ThreatResponse? = null,
        error: WPComGsonNetworkError? = null
    ): Response<ThreatResponse> {
        val nonNullData = data ?: mock()
        val response = if (error != null) Response.Error(error) else Success(nonNullData)
        whenever(
            wpComGsonRequestBuilder.syncGetRequest(
                eq(threatRestClient),
                urlCaptor.capture(),
                eq(mapOf()),
                eq(ThreatResponse::class.java),
                eq(false),
                any(),
                eq(false)
            )
        ).thenReturn(response)
        whenever(site.siteId).thenReturn(siteId)
        whenever(threat.id).thenReturn(data?.threat?.id ?: threatId)

        val threatModel = mock<ThreatModel>()
        whenever(threatMapper.map(any())).thenReturn(threatModel)

        return response
    }

    companion object {
        private const val API_BASE_PATH = "https://public-api.wordpress.com/wpcom/v2"
        private const val THREAT_SUCCESS_RESPONSE_JSON = "wp/jetpack/scan/threat/threat-success-response.json"
    }
}
