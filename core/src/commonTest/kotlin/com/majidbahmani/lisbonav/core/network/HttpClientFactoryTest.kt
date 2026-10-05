package com.majidbahmani.lisbonav.core.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HttpClientFactoryTest {

    @Serializable
    private data class Bus(val id: String)

    private val requests = mutableListOf<HttpRequestData>()

    private fun client(body: String, status: HttpStatusCode = HttpStatusCode.OK) = createHttpClient(
        MockEngine { request ->
            requests += request
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        },
    )

    @Test
    fun decodesJson_ignoringUnknownFields() = runTest {
        val bus: Bus = client("""{"id":"a","speed":50,"new_field":true}""").get("https://example.org/bus").body()

        assertEquals(Bus("a"), bus)
    }

    @Test
    fun sendsAcceptJson() = runTest {
        client("""{"id":"a"}""").get("https://example.org/bus")

        assertTrue(requests.single().headers.getAll(HttpHeaders.Accept).orEmpty().any { "application/json" in it })
    }

    @Test
    fun usesTheUrlAsGiven_noBaseUrl() = runTest {
        client("""{"id":"a"}""").get("https://example.org/v2/vehicles")

        assertEquals("https://example.org/v2/vehicles", requests.single().url.toString())
    }

    @Test
    fun serverError_throws() = runTest {
        assertFailsWith<ServerResponseException> {
            client("""{"error":"down"}""", HttpStatusCode.InternalServerError).get("https://example.org/bus")
        }
    }
}
