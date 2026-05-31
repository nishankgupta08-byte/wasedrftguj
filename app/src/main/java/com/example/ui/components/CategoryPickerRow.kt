package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusList
import com.example.ui.theme.toColorSafely

@Composable
fun CategoryPickerRow(
    lists: List<FocusList>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_picker_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // "All Lists" selector pill
        item {
            val isAllSelected = selectedId == null
            val backgroundColor by animateColorAsState(
                targetValue = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                label = "AllPillBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "AllPillText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onSelected(null) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("all_lists_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌐",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "All Lists",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }
            }
        }

        // Custom Lists pills
        items(lists, key = { it.id }) { item ->
            val isSelected = selectedId == item.id
            val parsedColor = remember(item.colorHex) {
                item.colorHex.toColorSafely(Color(0xFF3B82F6))
            }

            val pillBg = if (isSelected) parsedColor else parsedColor.copy(alpha = 0.12f)
            val pillText = if (isSelected) Color.White else parsedColor
            val borderModifier = if (isSelected) Modifier else Modifier.border(
                1.dp,
                parsedColor.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillBg)
                    .then(borderModifier)
                    .clickable { onSelected(item.id) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("list_pill_${item.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.emoji,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = pillText
                        )
                    )
                }
            }
        }

        // Add Category capsule button
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    .clickable { onAddClicked() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("add_list_pill_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Category list",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New List",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}
