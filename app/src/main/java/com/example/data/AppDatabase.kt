package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusRoomConverters {
    @TypeConverter
    fun subTasksToJson(subTasks: List<SubTaskItem>): String {
        return FocusTypeConverters.subTasksToJson(subTasks)
    }

    @TypeConverter
    fun subTasksFromJson(json: String): List<SubTaskItem> {
        return FocusTypeConverters.subTasksFromJson(json)
    }
}

@Database(entities = [FocusList::class, FocusTask::class], version = 1, exportSchema = false)
@TypeConverters(FocusRoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listDao(): FocusListDao
    abstract fun taskDao(): FocusTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focus_tasks_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate database with beautiful default list categories synchronously during transaction creation
                        db.execSQL("INSERT INTO lists (name, emoji, colorHex, sortOrder, isDefault) VALUES ('Personal', '🏡', '#0284C7', 0, 1)")
                        db.execSQL("INSERT INTO lists (name, emoji, colorHex, sortOrder, isDefault) VALUES ('Work', '💼', '#EA580C', 1, 0)")
                        db.execSQL("INSERT INTO lists (name, emoji, colorHex, sortOrder, isDefault) VALUES ('Wellness', '🧘', '#8B5CF6', 2, 0)")
                        db.execSQL("INSERT INTO lists (name, emoji, colorHex, sortOrder, isDefault) VALUES ('Finance', '💰', '#16A34A', 3, 0)")
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
