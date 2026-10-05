package com.majidbahmani.lisbonav.analytics

/**
 * One analytics event: a name and a few parameters, following Firebase's rules so no event is
 * silently dropped. Invalid events fail at creation: events are constants, so tests catch them.
 *
 * Never put personal data in an event (card number, date of birth, trips, balances, positions).
 *
 * @property params values are [String], [Long] or [Double] (the types both platforms' SDKs accept).
 */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
) {
    init {
        requireValidName(name, "Event name")
        require(params.size <= MAX_PARAMS) { "At most $MAX_PARAMS parameters, got ${params.size}" }
        params.forEach { (key, value) ->
            requireValidName(key, "Parameter name")
            when (value) {
                is String -> require(value.length <= MAX_STRING_VALUE_LENGTH) {
                    "Parameter '$key' is longer than $MAX_STRING_VALUE_LENGTH characters"
                }
                is Long, is Double -> Unit
                else -> throw IllegalArgumentException(
                    "Parameter '$key' must be a String, Long or Double, not ${value::class.simpleName}",
                )
            }
        }
    }

    companion object {
        /** A screen was shown. [screenName] is short and stable ("map", "card"), not a class name. */
        fun screenView(screenName: String) = AnalyticsEvent(
            name = "screen_view",
            params = mapOf("screen_name" to screenName, "screen_class" to screenName),
        )
    }
}

private const val MAX_NAME_LENGTH = 40
private const val MAX_PARAMS = 25
private const val MAX_STRING_VALUE_LENGTH = 100

/** Letters, digits and underscores, starting with a letter. */
private val NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*")

/** Reserved by Firebase and Google Analytics. */
private val RESERVED_PREFIXES = listOf("firebase_", "google_", "ga_")

private fun requireValidName(name: String, what: String) {
    require(name.length <= MAX_NAME_LENGTH) { "$what '$name' is longer than $MAX_NAME_LENGTH characters" }
    require(NAME_PATTERN.matches(name)) { "$what '$name' must be letters, digits and _, starting with a letter" }
    require(RESERVED_PREFIXES.none { name.startsWith(it) }) { "$what '$name' uses a reserved prefix" }
}
