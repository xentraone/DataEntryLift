package com.xentraone.dataentrylift

data class Entry(
    val id: Long = 0,
    val date: String,   // yyyy-MM-dd
    val job: String,
    val unit: String,
    val dx: String,
    val nor: String,    // a number like "2", or text like "call back", or blank
    val ot1: Double = 0.0,
    val ot2: Double = 0.0,
    val ot3: Double = 0.0
)
