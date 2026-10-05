package com.majidbahmani.lisbonav

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
