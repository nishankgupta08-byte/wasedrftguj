package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FilterMode
import com.example.ui.FocusViewModel
import com.example.ui.SortOrder
import com.example.ui.theme.SleekAccentBlue
import com.example.ui.theme.SleekAccentBlueText
import com.example.ui.theme.SleekOverdueText
import com.example.ui.theme.toColorSafely
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DashboardTab { TASKS, POMODORO, STATS, ACCOUNT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusDashboard(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()

    val selectedListId by viewModel.selectedListId.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(DashboardTab.TASKS) }

    var showAddCategory by remember { mutableStateOf(false) }
    var showAddTask by remember { mutableStateOf(false) }

    // Dynamic greeting calculation depending on active local time hour
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning ☀️"
            in 12..16 -> "Good afternoon 🌤️"
            else -> "Good evening 🌙"
        }
    }

    val todayDateString = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        sdf.format(Date())
    }

    // Interactive mindfulness quote
    val mindfulnessQuote = remember(todayStats.percentage) {
        when {
            todayStats.total == 0 -> "Let's organize the focal points for today."
            todayStats.percentage >= 1.0f -> "Clarity achieved! Fantastic work on completing today's focus!"
            todayStats.percentage >= 0.6f -> "Over halfway complete. Keep maintaining that momentum."
            todayStats.percentage >= 0.2f -> "Each step completed brings focused clarity. Keep going!"
            else -> "A journey of a thousand miles begins with a single task."
        }
    }

    val selectedList = lists.find { it.id == selectedListId }

    Scaffold(
        modifier = modifier.testTag("focus_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == DashboardTab.TASKS,
                    onClick = { currentTab = DashboardTab.TASKS },
                    icon = { Text("📋", fontSize = 20.sp) },
                    label = { Text("Queue", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_tasks")
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.POMODORO,
                    onClick = { currentTab = DashboardTab.POMODORO },
                    icon = { Text("⏰", fontSize = 20.sp) },
                    label = { Text("Focus", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_pomodoro")
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.STATS,
                    onClick = { currentTab = DashboardTab.STATS },
                    icon = { Text("📈", fontSize = 20.sp) },
                    label = { Text("Progress", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
                NavigationBarItem(
                    selected = currentTab == DashboardTab.ACCOUNT,
                    onClick = { currentTab = DashboardTab.ACCOUNT },
                    icon = { Text("👤", fontSize = 20.sp) },
                    label = { Text("Sync", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_account")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == DashboardTab.TASKS && lists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddTask = true },
                    containerColor = SleekAccentBlue,
                    contentColor = SleekAccentBlueText,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("dashboard_fab_add_task")
                        .padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create fine task focus"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DashboardTab.TASKS -> {
                    // Header Core block: Greeting, Progress Dial & Stats
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = greeting,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Elegant inline theme switcher
                                    IconButton(
                                        onClick = { viewModel.toggleTheme() },
                                        modifier = Modifier.testTag("theme_toggle_btn")
                                    ) {
                                        val isThemeBlack by viewModel.isBlackTheme.collectAsStateWithLifecycle()
                                        Text(
                                            text = if (isThemeBlack) "☀️" else "🌙",
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayDateString,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mindfulnessQuote,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    ),
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Display Canvas progress ring with center completion ratio label
                            ProgressRing(
                                progress = todayStats.percentage,
                                size = 64.dp,
                                centerLabel = "${todayStats.completed}/${todayStats.total}",
                                gradientColors = selectedList?.colorHex?.let { hex ->
                                    val parsed = hex.toColorSafely()
                                    listOf(
                                        parsed,
                                        parsed.copy(alpha = 0.7f),
                                        parsed.copy(alpha = 0.4f)
                                    )
                                } ?: listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("header_progress_ring")
                            )
                        }
                    }

                    // Circular Quick statistics overlay row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Today completion badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "📋 Today: ${todayStats.completed}/${todayStats.total}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Overdue tasks counter alert pill
                        if (todayStats.overdueCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekOverdueText.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("overdue_indicator_pill")
                            ) {
                                Text(
                                    text = "🚨 Overdue: ${todayStats.overdueCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SleekOverdueText
                                    )
                                )
                            }
                        }
                    }

                    // Category Horizontal Picker
                    CategoryPickerRow(
                        lists = lists,
                        selectedId = selectedListId,
                        onSelected = { viewModel.selectedListId.value = it },
                        onAddClicked = { showAddCategory = true }
                    )

                    // Search Bar Textfield
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search focused tasks...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search bar icon"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("dashboard_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = selectedList?.colorHex.toColorSafely(MaterialTheme.colorScheme.primary)
                        )
                    )

                    // Dynamic filter options Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Filter Tab Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val filterLayouts = listOf(
                                FilterMode.ALL to "All Queue",
                                FilterMode.TODAY to "⏰ Today",
                                FilterMode.OVERDUE to "⚠️ Overdue"
                            )
                            filterLayouts.forEach { (mode, title) ->
                                val isSelected = filterMode == mode
                                val accentColor = selectedList?.colorHex.toColorSafely(MaterialTheme.colorScheme.primary)
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.filterMode.value = mode },
                                    label = { Text(title, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("filter_chip_${mode.name}")
                                )
                            }
                        }

                        // Dynamic Sorting Choice Selector
                        var showSortMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.testTag("sort_menu_trigger_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Filter and Sort criteria"
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sort: Deadline Date") },
                                    onClick = {
                                        viewModel.sortOrder.value = SortOrder.DUE_DATE
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortOrder == SortOrder.DUE_DATE) Text("✓")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort: Priority Scale") },
                                    onClick = {
                                        viewModel.sortOrder.value = SortOrder.PRIORITY
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortOrder == SortOrder.PRIORITY) Text("✓")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort: Created Timestamp") },
                                    onClick = {
                                        viewModel.sortOrder.value = SortOrder.CREATED_AT
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortOrder == SortOrder.CREATED_AT) Text("✓")
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Tasks List queue or beautiful empty states indicators
                    if (tasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.testTag("dashboard_empty_state")
                            ) {
                                Text(
                                    text = "🧘",
                                    fontSize = 62.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Perfect Clarity",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lists.isEmpty()) {
                                        "First create a Category List (e.g. Work, Personal) to get started on your agenda!"
                                    } else if (searchQuery.isNotBlank()) {
                                        "No tasks matched your query description."
                                    } else {
                                        "No active focal points left. Clear mind, clear space!"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("tasks_lazy_column"),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(tasks, key = { it.id }) { focusTask ->
                                val currentTaskCategory = lists.find { it.id == focusTask.listId }
                                TaskItemCard(
                                    task = focusTask,
                                    listCategory = currentTaskCategory,
                                    onToggleComplete = { viewModel.toggleTaskComplete(focusTask) },
                                    onDelete = { viewModel.deleteTask(focusTask) },
                                    onAddSubTask = { item -> viewModel.addSubTaskToTask(focusTask, item) },
                                    onToggleSubTask = { itemId -> viewModel.toggleSubTaskComplete(focusTask, itemId) },
                                    onDeleteSubTask = { itemId -> viewModel.deleteSubTask(focusTask, itemId) }
                                )
                            }
                        }
                    }
                }
                DashboardTab.POMODORO -> {
                    PomodoroTimerView(viewModel = viewModel)
                }
                DashboardTab.STATS -> {
                    MonthlyStatsView(viewModel = viewModel)
                }
                DashboardTab.ACCOUNT -> {
                    AccountView(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Add Category dialog popup
    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onSave = { name, emoji, colorHex ->
                viewModel.createList(name, emoji, colorHex)
                showAddCategory = false
            }
        )
    }

    // Modal Add Task dialog scheduler
    if (showAddTask && lists.isNotEmpty()) {
        AddTaskDialog(
            lists = lists,
            initialListId = selectedListId,
            onDismiss = { showAddTask = false },
            onSave = { listId, title, notes, priority, dueDate, dueTime, tags ->
                viewModel.createTask(listId, title, notes, priority, dueDate, dueTime, tags)
                showAddTask = false
            }
        )
    }
}
