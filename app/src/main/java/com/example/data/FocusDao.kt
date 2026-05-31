package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusListDao {
    @Query("SELECT * FROM lists ORDER BY sortOrder ASC")
    fun getAllLists(): Flow<List<FocusList>>

    @Query("SELECT * FROM lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: Int): FocusList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: FocusList): Long

    @Update
    suspend fun updateList(list: FocusList)

    @Delete
    suspend fun deleteList(list: FocusList)
}

@Dao
interface FocusTaskDao {
    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<FocusTask>>

    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY sortOrder ASC, createdAt DESC")
    fun getTasksForList(listId: Int): Flow<List<FocusTask>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): FocusTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FocusTask): Long

    @Update
    suspend fun updateTask(task: FocusTask)

    @Delete
    suspend fun deleteTask(task: FocusTask)

    @Query("DELETE FROM tasks WHERE listId = :listId")
    suspend fun deleteTasksByListId(listId: Int)
}
