package com.fabio.shopperpartiu.Model

import java.io.Serializable

data class Motorista(
    val idMotorista: Int,
    val name: String,
    val description: String,
    val vehicle: String,
    val value: String,
    val review: List<Pair<Int, String>>
) : Serializable