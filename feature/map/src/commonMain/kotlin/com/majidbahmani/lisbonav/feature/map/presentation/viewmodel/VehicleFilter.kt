package com.majidbahmani.lisbonav.feature.map.presentation.viewmodel

import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle

/**
 * The buses whose line starts with [query] ("35" matches 3510 and 3507). A blank query keeps all.
 * Prefix, not "contains": typing a line number narrows the map step by step, as expected.
 */
internal fun List<Vehicle>.filterByLine(query: String): List<Vehicle> {
    val line = query.trim()
    if (line.isEmpty()) return this
    return filter { it.lineId.startsWith(line, ignoreCase = true) }
}
