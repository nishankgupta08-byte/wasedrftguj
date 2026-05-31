package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusList
import com.example.data.FocusTask
import com.example.ui.theme.SleekOverdueBg
import com.example.ui.theme.SleekOverdueBorder
import com.example.ui.theme.SleekOverdueText
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekPrimaryVariant
import com.example.ui.theme.SleekSupportingText
import com.example.ui.theme.toColorSafely
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemCard(
    task: FocusTask,
    listCategory: FocusList?,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onToggleSubTask: (String) -> Unit,
    onDeleteSubTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var newSubTaskTitle by remember { mutableStateOf("") }
    
    val dateString = remember(task.dueDate) {
        task.dueDate?.let {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(it))
        }
    }

    val isOverdue = task.isOverdue
    val isDueToday = task.isDueToday
    val subTasks = task.getParsedSubTasks()
    val completedSubtasks = subTasks.count { it.isCompleted }

    val categoryColor = remember(listCategory) {
        listCategory?.colorHex.toColorSafely()
    }

    val cardBgColor = when {
        task.isCompleted -> SleekPrimaryVariant.copy(alpha = 0.4f)
        isOverdue -> SleekOverdueBg
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = when {
        task.isCompleted -> Color.Transparent
        isOverdue -> SleekOverdueBorder
        else -> SleekCardBorder.copy(alpha = 0.7f)
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .animateContentSize(animationSpec = tween(durationMillis = 250))
            .alpha(if (task.isCompleted) 0.6f else 1.0f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Checkbox, Title details, Priority & Trash
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Interactive Checkbox
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("task_checkbox_toggle_${task.id}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (task.isCompleted) categoryColor else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (task.isCompleted) Color.Transparent else categoryColor,
                                shape = CircleShape
                            )
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title, Notes & Category Badges
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier.testTag("task_title_${task.id}")
                    )

                    if (!task.notes.isNullOrBlank()) {
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            ),
                            maxLines = 2,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Metadata details, tags and category names
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Label
                        if (listCategory != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(categoryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${listCategory.emoji} ${listCategory.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = categoryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Due Date Label
                        if (dateString != null) {
                            val textColor = when {
                                task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                isOverdue -> SleekOverdueText // Overdue RED
                                isDueToday -> Color(0xFF3263E0) // Today BLUE
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Due date",
                                    tint = textColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$dateString${if (task.dueTime != null) " @ ${task.dueTime}" else ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textColor,
                                        fontWeight = if (isOverdue || isDueToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action Priority indicators
                if (task.priority > 0 && !task.isCompleted) {
                    val badgeColor = when (task.priority) {
                        3 -> SleekOverdueText // Urgent red
                        2 -> Color(0xFFE29202) // Medium gold
                        else -> Color(0xFF3263E0) // Low blue
                    }
                    val badgeText = when (task.priority) {
                        3 -> "!!!"
                        2 -> "!!"
                        else -> "!"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Checklist indicator if subtasks exist but row is collapsed
            if (subTasks.isNotEmpty() && !isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { if (subTasks.isEmpty()) 0f else (completedSubtasks.toFloat() / subTasks.size).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = categoryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$completedSubtasks/${subTasks.size} subtasks",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Expandable details block: Checklist Subtask Editor
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Tags badges row if tags exist
                    if (!task.tags.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            task.tags.split(",").forEach { tag ->
                                if (tag.trim().isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "#${tag.trim()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Checklist Heading
                    Text(
                        text = "Subtask Checklist (${completedSubtasks}/${subTasks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checklist Items Column
                    subTasks.forEach { subItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtask Checkbox
                            IconButton(
                                onClick = { onToggleSubTask(subItem.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (subItem.isCompleted) Color.Transparent else categoryColor,
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                        .background(
                                            if (subItem.isCompleted) categoryColor else Color.Transparent,
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                ) {
                                    if (subItem.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Subtask Text
                            Text(
                                text = subItem.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (subItem.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subItem.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Delete Subtask
                            IconButton(
                                onClick = { onDeleteSubTask(subItem.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete list item",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inline Input row to append checklist items directly
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubTaskTitle,
                            onValueChange = { newSubTaskTitle = it },
                            placeholder = { Text("Add checklist item...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 48.dp)
                                .testTag("subtask_input_field_${task.id}"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = categoryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newSubTaskTitle.isNotBlank()) {
                                        onAddSubTask(newSubTaskTitle)
                                        newSubTaskTitle = ""
                                    }
                                }
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSubTaskTitle.isNotBlank()) {
                                    onAddSubTask(newSubTaskTitle)
                                    newSubTaskTitle = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(categoryColor.copy(alpha = 0.15f))
                                .testTag("subtask_add_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add subtask",
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
