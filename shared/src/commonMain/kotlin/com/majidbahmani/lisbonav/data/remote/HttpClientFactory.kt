package com.majidbahmani.lisbonav.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The shared HTTP client for the Carris Metropolitana API.
 *
 * The [engine] is passed in: OkHttp on Android, Darwin on iOS (wired in the DI step),
 * and a `MockEngine` in tests.
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    // Fail on 4xx/5xx instead of trying to parse an error body as data.
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                // New API fields must not break parsing.
                ignoreUnknownKeys = true
            },
        )
    }

    // ContentNegotiation already sends `Accept: application/json`.
    defaultRequest {
        url(CarrisMetropolitanaApi.BASE_URL)
    }
}
