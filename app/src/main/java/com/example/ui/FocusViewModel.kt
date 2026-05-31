package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterMode { ALL, TODAY, OVERDUE }
enum class SortOrder { DUE_DATE, PRIORITY, CREATED_AT }

data class TodayStats(
    val completed: Int,
    val total: Int,
    val percentage: Float,
    val overdueCount: Int
)

data class DayProgress(val dayOfMonth: Int, val completedCount: Int)
data class MonthlyStats(
    val monthName: String,
    val totalCompletedThisMonth: Int,
    val completionsByDay: List<DayProgress>,
    val completedByCategory: Map<String, Int>
)

class FocusViewModel(
    private val context: Context,
    private val repository: FocusRepository
) : ViewModel() {

    // --- Preferences: Theme & Gmail login ---
    private val prefs = context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
    
    val isBlackTheme = MutableStateFlow(prefs.getBoolean("is_black_theme", true))
    
    fun toggleTheme() {
        val next = !isBlackTheme.value
        isBlackTheme.value = next
        prefs.edit().putBoolean("is_black_theme", next).apply()
    }

    // --- Gmail Authentication (Simulated & Real configuration state) ---
    val userEmail = MutableStateFlow(prefs.getString("user_email", null))
    val userName = MutableStateFlow(prefs.getString("user_name", null))
    val userAvatarUrl = MutableStateFlow(prefs.getString("user_avatar", null))

    fun loginWithGmail(email: String, name: String) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        val avatar = "https://api.dicebear.com/7.x/bottts/svg?seed=" + cleanEmail.hashCode()
        userEmail.value = cleanEmail
        userName.value = cleanName
        userAvatarUrl.value = avatar
        
        prefs.edit().apply {
            putString("user_email", cleanEmail)
            putString("user_name", cleanName)
            putString("user_avatar", avatar)
            apply()
        }
    }

    fun logoutGmail() {
        userEmail.value = null
        userName.value = null
        userAvatarUrl.value = null
        prefs.edit().apply {
            remove("user_email")
            remove("user_name")
            remove("user_avatar")
            apply()
        }
    }

    // --- Pomodoro Timer State ---
    private var timerJob: kotlinx.coroutines.Job? = null
    val pomodoroTotal = MutableStateFlow(25 * 60) // 25 mins by default
    val pomodoroRemaining = MutableStateFlow(25 * 60)
    val isTimerRunning = MutableStateFlow(false)
    val isWorkPeriod = MutableStateFlow(true) // true = Work, false = Break
    val pomodoroCompletedCount = MutableStateFlow(0) // total sessions completed in this session

    fun startTimer() {
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (pomodoroRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                pomodoroRemaining.value -= 1
            }
            // Timer finished!
            if (isWorkPeriod.value) {
                // Was work, now break!
                pomodoroCompletedCount.value += 1
                isWorkPeriod.value = false
                pomodoroTotal.value = 5 * 60 // 5 min break
                pomodoroRemaining.value = 5 * 60
            } else {
                // Was break, now work!
                isWorkPeriod.value = true
                pomodoroTotal.value = 25 * 60
                pomodoroRemaining.value = 25 * 60
            }
            isTimerRunning.value = false
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
    }

    fun resetTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
        if (isWorkPeriod.value) {
            pomodoroTotal.value = 25 * 60
            pomodoroRemaining.value = 25 * 60
        } else {
            pomodoroTotal.value = 5 * 60
            pomodoroRemaining.value = 5 * 60
        }
    }

    fun toggleSessionMode() {
        timerJob?.cancel()
        isTimerRunning.value = false
        isWorkPeriod.value = !isWorkPeriod.value
        if (isWorkPeriod.value) {
            pomodoroTotal.value = 25 * 60
            pomodoroRemaining.value = 25 * 60
        } else {
            pomodoroTotal.value = 5 * 60
            pomodoroRemaining.value = 5 * 60
        }
    }

    fun customDuration(minutes: Int) {
        timerJob?.cancel()
        isTimerRunning.value = false
        pomodoroTotal.value = minutes * 60
        pomodoroRemaining.value = minutes * 60
    }

    // Active state flows
    val lists: StateFlow<List<FocusList>> = repository.allLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedListId = MutableStateFlow<Int?>(null) // null = Show All
    val filterMode = MutableStateFlow(FilterMode.ALL)
    val sortOrder = MutableStateFlow(SortOrder.DUE_DATE)
    val searchQuery = MutableStateFlow("")

    // Map combined fields into a highly reactive, optimized task query
    val tasks: StateFlow<List<FocusTask>> = combine(
        repository.allTasks,
        selectedListId,
        filterMode,
        sortOrder,
        searchQuery
    ) { allTasks, listId, mode, sort, query ->
        var filtered = allTasks

        // 1. List selection filter
        if (listId != null) {
            filtered = filtered.filter { it.listId == listId }
        }

        // 2. Tab Filter modes (ALL, TODAY, OVERDUE)
        val now = System.currentTimeMillis()
        filtered = when (mode) {
            FilterMode.ALL -> filtered
            FilterMode.TODAY -> filtered.filter {
                it.dueDate != null && isSameDay(it.dueDate, now)
            }
            FilterMode.OVERDUE -> filtered.filter {
                !it.isCompleted && it.dueDate != null && it.dueDate < now
            }
        }

        // 3. Search text index
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                (it.notes?.contains(query, ignoreCase = true) ?: false) ||
                (it.tags?.contains(query, ignoreCase = true) ?: false)
            }
        }

        // 4. Custom sorting (Collapses completed items to the bottom of the stack)
        when (sort) {
            SortOrder.DUE_DATE -> {
                filtered.sortedWith(
                    compareBy<FocusTask> { it.isCompleted }
                        .thenBy { it.dueDate ?: Long.MAX_VALUE }
                        .thenByDescending { it.priority }
                        .thenByDescending { it.createdAt }
                )
            }
            SortOrder.PRIORITY -> {
                filtered.sortedWith(
                    compareBy<FocusTask> { it.isCompleted }
                        .thenByDescending { it.priority }
                        .thenBy { it.dueDate ?: Long.MAX_VALUE }
                        .thenByDescending { it.createdAt }
                )
            }
            SortOrder.CREATED_AT -> {
                filtered.sortedWith(
                    compareBy<FocusTask> { it.isCompleted }
                        .thenByDescending { it.createdAt }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map combined list states into direct focused calculations for the progress ring
    val todayStats: StateFlow<TodayStats> = repository.allTasks.map { allTasks ->
        val now = System.currentTimeMillis()
        val todayTasks = allTasks.filter {
            it.dueDate != null && isSameDay(it.dueDate, now)
        }
        val total = todayTasks.size
        val completed = todayTasks.filter { it.isCompleted }.size
        val percentage = if (total > 0) completed.toFloat() / total else 0f
        
        val overdueTotal = allTasks.filter {
            !it.isCompleted && it.dueDate != null && it.dueDate < now
        }.size

        TodayStats(
            completed = completed,
            total = total,
            percentage = percentage,
            overdueCount = overdueTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayStats(0, 0, 0f, 0))

    // Monthly Statistics Flow
    val monthlyStats: StateFlow<MonthlyStats> = combine(
        repository.allTasks,
        lists
    ) { allTasks, listCategories ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val monthDisplayName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: ""
        
        val currentMonthTasks = allTasks.filter { task ->
            if (task.isCompleted && task.completedAt != null) {
                val compCal = Calendar.getInstance().apply { timeInMillis = task.completedAt }
                compCal.get(Calendar.MONTH) == currentMonth && compCal.get(Calendar.YEAR) == currentYear
            } else false
        }
        
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayProgressList = (1..maxDays).map { day ->
            val count = currentMonthTasks.count { task ->
                val compCal = Calendar.getInstance().apply { timeInMillis = task.completedAt!! }
                compCal.get(Calendar.DAY_OF_MONTH) == day
            }
            DayProgress(day, count)
        }
        
        val listsMap = listCategories.associate { it.id to it.name }
        val groupCompletedByCategory = mutableMapOf<String, Int>()
        currentMonthTasks.forEach { task ->
            val catName = listsMap[task.listId] ?: "Other"
            groupCompletedByCategory[catName] = (groupCompletedByCategory[catName] ?: 0) + 1
        }
        
        MonthlyStats(
            monthName = "$monthDisplayName $currentYear",
            totalCompletedThisMonth = currentMonthTasks.size,
            completionsByDay = dayProgressList,
            completedByCategory = groupCompletedByCategory
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyStats("", 0, emptyList(), emptyMap())
    )

    // List Category manipulations
    fun createList(name: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            val order = lists.value.size
            repository.insertList(
                FocusList(
                    name = name,
                    emoji = emoji,
                    colorHex = colorHex,
                    sortOrder = order
                )
            )
        }
    }

    fun deleteList(list: FocusList) {
        viewModelScope.launch {
            if (selectedListId.value == list.id) {
                selectedListId.value = null // reset selection
            }
            repository.deleteList(list)
        }
    }

    // Task manipulations
    fun createTask(
        listId: Int,
        title: String,
        notes: String? = null,
        priority: Int = 0,
        dueDate: Long? = null,
        dueTime: String? = null,
        tags: String? = null,
        subTasks: List<SubTaskItem> = emptyList()
    ) {
        viewModelScope.launch {
            val task = FocusTask(
                listId = listId,
                title = title.trim(),
                notes = if (notes?.isNotBlank() == true) notes.trim() else null,
                priority = priority,
                dueDate = dueDate,
                dueTime = if (dueTime?.isNotBlank() == true) dueTime.trim() else null,
                tags = if (tags?.isNotBlank() == true) tags.trim() else null,
                subTasksJson = FocusTypeConverters.subTasksToJson(subTasks),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
        }
    }

    fun updateTask(task: FocusTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: FocusTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Toggle main task completed state
    fun toggleTaskComplete(task: FocusTask) {
        viewModelScope.launch {
            val completed = !task.isCompleted
            repository.updateTask(
                task.copy(
                    isCompleted = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Add list item under sub-task checklist
    fun addSubTaskToTask(task: FocusTask, title: String) {
        viewModelScope.launch {
            val current = task.getParsedSubTasks().toMutableList()
            val newItem = SubTaskItem(
                id = Math.random().toString().substring(2, 8),
                title = title.trim(),
                isCompleted = false,
                sortOrder = current.size
            )
            current.add(newItem)
            repository.updateTask(
                task.copy(
                    subTasksJson = FocusTypeConverters.subTasksToJson(current),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Toggle sub task list checkbox
    fun toggleSubTaskComplete(task: FocusTask, subTaskId: String) {
        viewModelScope.launch {
            val current = task.getParsedSubTasks().map {
                if (it.id == subTaskId) {
                    it.copy(isCompleted = !it.isCompleted)
                } else it
            }
            repository.updateTask(
                task.copy(
                    subTasksJson = FocusTypeConverters.subTasksToJson(current),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Remove item from sub checklist
    fun deleteSubTask(task: FocusTask, subTaskId: String) {
        viewModelScope.launch {
            val current = task.getParsedSubTasks().filter { it.id != subTaskId }
            repository.updateTask(
                task.copy(
                    subTasksJson = FocusTypeConverters.subTasksToJson(current),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

class FocusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getInstance(context)
        val repository = FocusRepository(database.listDao(), database.taskDao())
        @Suppress("UNCHECKED_CAST")
        return FocusViewModel(context.applicationContext, repository) as T
    }
}
