package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.XrayConfigProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigProfileDao {
    @Query("SELECT * FROM config_profiles ORDER BY isPreset DESC, id DESC")
    fun getAllProfiles(): Flow<List<XrayConfigProfile>>

    @Query("SELECT * FROM config_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): XrayConfigProfile?

    @Query("SELECT * FROM config_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<XrayConfigProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: XrayConfigProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<XrayConfigProfile>)

    @Update
    suspend fun update(profile: XrayConfigProfile)

    @Delete
    suspend fun delete(profile: XrayConfigProfile)

    @Query("DELETE FROM config_profiles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE config_profiles SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE config_profiles SET isActive = 1 WHERE id = :id")
    suspend fun setActiveProfile(id: Long)

    @Query("UPDATE config_profiles SET latencyMs = :latency, lastTestedTime = :timestamp WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM config_profiles")
    suspend fun getCount(): Int
}
