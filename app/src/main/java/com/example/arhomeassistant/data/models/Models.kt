package com.example.arhomeassistant.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "appliances")
data class Appliance(
    @PrimaryKey val id: String,
    val name: String,
    val type: ApplianceType,
    val manufacturer: String,
    val model: String,
    val purchaseDate: Date?,
    val lastMaintenanceDate: Date?,
    val maintenanceInterval: Int, // in days
    val notes: String?
)

enum class ApplianceType {
    HVAC,
    WATER_HEATER,
    DISHWASHER,
    WASHING_MACHINE,
    DRYER,
    REFRIGERATOR,
    OVEN,
    MICROWAVE,
    OTHER
}

@Entity(tableName = "maintenance_records")
data class MaintenanceRecord(
    @PrimaryKey val id: String,
    val applianceId: String,
    val date: Date,
    val type: MaintenanceType,
    val description: String,
    val cost: Double?,
    val technician: String?,
    val notes: String?
)

enum class MaintenanceType {
    ROUTINE_CHECK,
    REPAIR,
    REPLACEMENT,
    CLEANING,
    PART_REPLACEMENT
}

@Entity(tableName = "ar_markers")
data class ARMarker(
    @PrimaryKey val id: String,
    val applianceId: String,
    val position: Position,
    val rotation: Rotation,
    val scale: Float
)

data class Position(
    val x: Float,
    val y: Float,
    val z: Float
)

data class Rotation(
    val x: Float,
    val y: Float,
    val z: Float
)

data class MaintenanceGuide(
    val id: String,
    val applianceType: ApplianceType,
    val title: String,
    val steps: List<MaintenanceStep>,
    val difficulty: MaintenanceDifficulty,
    val estimatedTime: Int, // in minutes
    val tools: List<String>,
    val safetyNotes: List<String>
)

data class MaintenanceStep(
    val order: Int,
    val description: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val arModelUrl: String?,
    val tips: List<String>
)

enum class MaintenanceDifficulty {
    EASY,
    MODERATE,
    DIFFICULT,
    PROFESSIONAL
}
