package com.estiven.lab08

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var description: String,
    var isCompleted: Boolean = false,
    val createdAt: Date = Date()
)