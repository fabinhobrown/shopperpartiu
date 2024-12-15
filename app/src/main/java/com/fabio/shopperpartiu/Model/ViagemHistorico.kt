package com.fabio.shopperpartiu.Model

data class ViagemHistorico(
    val id: Int,
    val date: String,
    val origin: String,
    val destination: String,
    val distance: Double,
    val duration: String,
    val driver: String,
    val value: Int
)