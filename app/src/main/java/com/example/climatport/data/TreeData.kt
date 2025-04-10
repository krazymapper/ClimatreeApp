package com.example.climatport.data

data class TreeData(
    val species: String,
    val height: Double?,
    val diameter: Double?,
    val notes: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
) 