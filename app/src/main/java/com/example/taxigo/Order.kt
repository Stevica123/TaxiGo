package com.example.taxigo

data class Order(
    val id: String = "",
    val vehicleNumber: String = "",
    val address: String = "",
    val arrivalTime: Int = 0,
)
