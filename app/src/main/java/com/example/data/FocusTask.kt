package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Represents a simple interactive Subtask in a checklist
data class SubTaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = FocusList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"])
    ]
)
data class FocusTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int, // Refers to parent list category
    val title: String,
    val notes: String? = null,
    val priority: Int = 0, // 0 = None, 1 = Low, 2 = Medium, 3 = High
    val dueDate: Long? = null, // Epoch millisecond timestamp
    val dueTime: String? = null, // e.g., "14:30"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val tags: String? = null, // Comma-separated tags
    val subTasksJson: String = "[]", // Serialized list of SubTaskItem
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Utility functions to parse helper information
    fun getParsedSubTasks(): List<SubTaskItem> {
        return FocusTypeConverters.subTasksFromJson(subTasksJson)
    }

    val isOverdue: Boolean
        get() = !isCompleted && dueDate != null && dueDate < System.currentTimeMillis()

    val isDueToday: Boolean
        get() {
            if (dueDate == null) return false
            val current = System.currentTimeMillis()
            val todayStart = current - (current % (24 * 60 * 60 * 1000))
            val todayEnd = todayStart + (24 * 60 * 60 * 1000)
            return dueDate in todayStart..<todayEnd
        }
}

object FocusTypeConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val subTaskListType = Types.newParameterizedType(List::class.java, SubTaskItem::class.java)
    private val adapter = moshi.adapter<List<SubTaskItem>>(subTaskListType)

    @TypeConverter
    @JvmStatic
    fun subTasksToJson(subTasks: List<SubTaskItem>): String {
        return try {
            adapter.toJson(subTasks) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    @JvmStatic
    fun subTasksFromJson(json: String): List<SubTaskItem> {
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
