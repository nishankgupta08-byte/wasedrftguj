package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FocusList
import com.example.ui.theme.toColorSafely
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    lists: List<FocusList>,
    initialListId: Int?,
    onDismiss: () -> Unit,
    onSave: (
        listId: Int,
        title: String,
        notes: String?,
        priority: Int,
        dueDate: Long?,
        dueTime: String?,
        tags: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Preselect current active category if available, else first standard list
    var selectedListId by remember {
        mutableStateOf(initialListId ?: lists.firstOrNull()?.id ?: 0)
    }

    var priority by remember { mutableStateOf(0) } // 0=None, 1=Low, 2=Medium, 3=High
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var dueTime by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var expandedListsDropdown by remember { mutableStateOf(false) }

    val formattedDueDate = remember(dueDate) {
        dueDate?.let {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(it))
        } ?: "No Due Date"
    }

    val selectedList = lists.find { it.id == selectedListId } ?: lists.firstOrNull()
    val listColor = remember(selectedList) {
        selectedList?.colorHex.toColorSafely(Color(0xFF3B82F6))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Add Focused Task",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task title text input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Clean workspace deck") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = listColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task body notes description input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Details") },
                    placeholder = { Text("e.g. Wipe dust off monitors and keyboard.") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = listColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category List picker dropdown
                Text(
                    text = "Assign to Category List",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedListsDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_category_dropdown_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedList?.let { "${it.emoji} ${it.name}" } ?: "Select List Category",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Open lists drawer"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedListsDropdown,
                        onDismissRequest = { expandedListsDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        lists.forEach { focusList ->
                            DropdownMenuItem(
                                text = { Text("${focusList.emoji} ${focusList.name}") },
                                onClick = {
                                    selectedListId = focusList.id
                                    expandedListsDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority segmented level picker
                Text(
                    text = "Select Priority Level",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorityLevels = listOf("None", "Low !", "Medium !!", "Urgent !!!")
                    priorityLevels.forEachIndexed { index, name ->
                        val isSelected = priority == index
                        val activeColor = when (index) {
                            3 -> Color(0xFFEF4444)
                            2 -> Color(0xFFF59E0B)
                            1 -> Color(0xFF3B82F6)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { priority = index }
                                .padding(vertical = 10.dp)
                                .testTag("priority_segment_$index")
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date setup
                Text(
                    text = "Deadline & Due Date",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                    }
                                    dueDate = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("due_date_picker_trigger")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar Picker"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = formattedDueDate, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (dueDate != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { dueDate = null },
                            modifier = Modifier.testTag("clear_date_btn")
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tags text input
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. homework, simple") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_tags_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = listColor
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom actions row triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    selectedListId,
                                    title.trim(),
                                    if (notes.isBlank()) null else notes.trim(),
                                    priority,
                                    dueDate,
                                    if (dueTime.isBlank()) null else dueTime.trim(),
                                    if (tags.isBlank()) null else tags.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = listColor
                        ),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_task_btn")
                    ) {
                        Text("Add Task", color = Color.White)
                    }
                }
            }
        }
    }
}
