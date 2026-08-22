package com.example.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.squareup.moshi.Json

@Keep
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String,
    @Json(name = "colorHex") val colorHex: String,
    @Json(name = "isDefault") val isDefault: Boolean = false,
    @ColumnInfo(defaultValue = "0") @Json(name = "orderIndex") val orderIndex: Int = 0
)

@Keep
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["categoryId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) @Json(name = "id") val id: Int = 0,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "categoryId") val categoryId: Int,
    @Json(name = "isCompleted") val isCompleted: Boolean = false,
    @Json(name = "reminderTime") val reminderTime: Long? = null, // timestamp in millis
    @Json(name = "repeatType") val repeatType: String? = null, // "none", "daily", "every_other_day", "weekly"
    @Json(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)

@Keep
@Entity(
    tableName = "subtasks",
    indices = [Index(value = ["taskId"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true) @Json(name = "id") val id: Int = 0,
    @Json(name = "taskId") val taskId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "isCompleted") val isCompleted: Boolean = false
)
