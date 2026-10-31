package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ProxyNode
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyNodeDao {
    @Query("SELECT * FROM proxy_nodes ORDER BY id DESC")
    fun getAllNodes(): Flow<List<ProxyNode>>

    @Query("SELECT * FROM proxy_nodes WHERE id = :id LIMIT 1")
    suspend fun getNodeById(id: Long): ProxyNode?

    @Query("SELECT * FROM proxy_nodes WHERE isSelected = 1 LIMIT 1")
    fun getSelectedNodeFlow(): Flow<ProxyNode?>

    @Query("SELECT * FROM proxy_nodes WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedNode(): ProxyNode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: ProxyNode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<ProxyNode>)

    @Update
    suspend fun update(node: ProxyNode)

    @Delete
    suspend fun delete(node: ProxyNode)

    @Query("DELETE FROM proxy_nodes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE proxy_nodes SET isSelected = 0")
    suspend fun clearSelection()

    @Query("UPDATE proxy_nodes SET isSelected = 1 WHERE id = :id")
    suspend fun setNodeSelected(id: Long)

    @Query("UPDATE proxy_nodes SET latencyMs = :latency, lastTestedTime = :timestamp WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM proxy_nodes")
    suspend fun getCount(): Int
}
