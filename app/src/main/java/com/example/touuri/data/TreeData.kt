package com.example.touuri.data

data class TreeData(
    val species: String,
    val height: Double?,
    val diameter: Double?,
    val healthStatus: HealthStatus,
    val age: Int?,
    val notes: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val osmId: String? = null
) {
    enum class HealthStatus {
        EXCELLENT,
        GOOD,
        FAIR,
        POOR,
        DEAD
    }
} 