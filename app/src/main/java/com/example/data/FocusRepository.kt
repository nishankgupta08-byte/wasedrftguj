package com.example.data

import kotlinx.coroutines.flow.Flow

class FocusRepository(
    private val listDao: FocusListDao,
    private val taskDao: FocusTaskDao
) {
    val allLists: Flow<List<FocusList>> = listDao.getAllLists()
    val allTasks: Flow<List<FocusTask>> = taskDao.getAllTasks()

    fun getTasksForList(listId: Int): Flow<List<FocusTask>> {
        return taskDao.getTasksForList(listId)
    }

    suspend fun getListById(id: Int): FocusList? {
        return listDao.getListById(id)
    }

    suspend fun insertList(list: FocusList): Long {
        return listDao.insertList(list)
    }

    suspend fun updateList(list: FocusList) {
        listDao.updateList(list)
    }

    suspend fun deleteList(list: FocusList) {
        // Cascade delete tasks first to prevent foreign key issues
        taskDao.deleteTasksByListId(list.id)
        listDao.deleteList(list)
    }

    suspend fun getTaskById(id: Int): FocusTask? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: FocusTask): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: FocusTask) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: FocusTask) {
        taskDao.deleteTask(task)
    }
}
