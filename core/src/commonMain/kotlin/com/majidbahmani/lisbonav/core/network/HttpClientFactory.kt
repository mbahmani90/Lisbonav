package com.majidbahmani.lisbonav.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The app's HTTP client, shared by every API.
 *
 * Generic on purpose: no base URL here; each API builds its own URLs. The [engine] is passed in:
 * OkHttp on Android, Darwin on iOS (see httpEngineModule), and a `MockEngine` in tests.
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    // Fail on 4xx/5xx instead of trying to parse an error body as data.
    expectSuccess = true

    // Also sends `Accept: application/json`.
    install(ContentNegotiation) {
        json(
            Json {
                // New API fields must not break parsing.
                ignoreUnknownKeys = true
            },
        )
    }
}
