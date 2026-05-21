package com.example.compoundingjournal.data.dao

import androidx.room.*
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPresetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: FilterPresetEntity): Long

    @Update
    suspend fun updatePreset(preset: FilterPresetEntity)

    @Delete
    suspend fun deletePreset(preset: FilterPresetEntity)

    @Query("SELECT * FROM filter_presets ORDER BY presetName ASC")
    fun getAllPresets(): Flow<List<FilterPresetEntity>>

    @Query("SELECT * FROM filter_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): FilterPresetEntity?
}
