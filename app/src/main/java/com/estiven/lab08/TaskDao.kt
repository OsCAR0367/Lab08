package com.estiven.lab08

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE description LIKE '%' || :searchQuery || '%'")
    suspend fun searchTasks(searchQuery: String): List<Task>

    @Query("SELECT * FROM tasks ORDER BY " +
            "CASE :sortBy " +
            "WHEN 'name' THEN description " +
            "WHEN 'date' THEN createdAt " +
            "WHEN 'status' THEN isCompleted " +
            "ELSE createdAt END " +
            "|| CASE WHEN :isAscending = 1 THEN ' ASC' ELSE ' DESC' END")
    suspend fun getSortedTasks(sortBy: String, isAscending: Boolean): List<Task>
}

