package com.example.compoundingjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_presets")
data class FilterPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val presetName: String,
    val dateFilter: String,
    val symbolFilter: String,
    val timeframeFilter: String,
    val strategyFilter: String,
    val statusFilter: String,
    val qualityGradeFilter: String,
    val ruleFollowedFilter: String,
    val createdAt: Long,
    val updatedAt: Long
)
