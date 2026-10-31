package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config_profiles")
data class XrayConfigProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val rawJson: String,
    val isPreset: Boolean = false,
    val isActive: Boolean = false,
    val latencyMs: Long = -1L, // -1: untested, -2: timeout, >0: latency in ms
    val lastTestedTime: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
