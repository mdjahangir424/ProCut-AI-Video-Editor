package com.example.data.repository

import android.content.Context
import com.example.core.database.AppDatabase
import com.example.core.database.ProjectEntity
import com.example.core.database.ProjectJsonAdapter
import com.example.core.model.AspectRatio
import com.example.core.model.EditingProject
import com.example.core.model.FrameRate
import com.example.core.model.VideoResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val projectDao = database.projectDao()

    val projectsFlow: Flow<List<EditingProject>> = projectDao.getAllProjects().map { entities ->
        entities.mapNotNull { entity ->
            runCatching { ProjectJsonAdapter.fromJson(entity.projectJson) }.getOrNull()
        }
    }

    suspend fun getProjectById(id: String): EditingProject? = withContext(Dispatchers.IO) {
        val entity = projectDao.getProjectById(id) ?: return@withContext null
        runCatching { ProjectJsonAdapter.fromJson(entity.projectJson) }.getOrNull()
    }

    suspend fun saveProject(project: EditingProject) = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        val json = ProjectJsonAdapter.toJson(updated)
        val entity = ProjectEntity(
            id = updated.id,
            title = updated.title,
            aspectRatioName = updated.aspectRatio.name,
            resolutionName = updated.resolution.name,
            frameRateName = updated.frameRate.name,
            createdAt = updated.createdAt,
            updatedAt = updated.updatedAt,
            totalDurationMs = updated.totalDurationMs,
            thumbnailUri = updated.thumbnailUri ?: updated.clips.firstOrNull()?.uriString,
            projectJson = json
        )
        projectDao.insertOrUpdateProject(entity)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }

    suspend fun createNewProject(
        title: String,
        aspectRatio: AspectRatio,
        resolution: VideoResolution = VideoResolution.FHD_1080P,
        frameRate: FrameRate = FrameRate.FPS_30
    ): EditingProject = withContext(Dispatchers.IO) {
        val project = EditingProject(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Project ${System.currentTimeMillis() % 10000}" },
            aspectRatio = aspectRatio,
            resolution = resolution,
            frameRate = frameRate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(project)
        project
    }

    suspend fun duplicateProject(project: EditingProject): EditingProject = withContext(Dispatchers.IO) {
        val duplicated = project.copy(
            id = UUID.randomUUID().toString(),
            title = "${project.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(duplicated)
        duplicated
    }
}
