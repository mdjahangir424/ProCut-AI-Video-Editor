package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val aspectRatioName: String,
    val resolutionName: String,
    val frameRateName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val totalDurationMs: Long,
    val thumbnailUri: String?,
    val projectJson: String
)
