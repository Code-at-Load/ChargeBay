package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReminderItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleNightRed

@Composable
fun RemindersWidget(
    modifier: Modifier = Modifier,
    items: List<ReminderItem>,
    onToggleItem: (String) -> Unit,
    onAddItem: (String) -> Unit = {},
    onDeleteItem: (String) -> Unit = {},
    isNightMode: Boolean = false
) {
    var isAdding by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }

    val headerColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleBlue,
        animationSpec = tween(500),
        label = "remindersHeaderColor"
    )
    val checkColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleBlue,
        animationSpec = tween(500),
        label = "remindersCheckColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "remindersTextColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.7f))
            .padding(14.dp)
            .testTag("reminders_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with interactive Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Reminders",
                        tint = headerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bedside Checklist",
                        color = headerColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${items.count { it.isCompleted }}/${items.size}",
                        color = headerColor.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(headerColor.copy(alpha = 0.2f))
                            .clickable { isAdding = !isAdding },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdding) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = headerColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Quick Inline Task Creator
            AnimatedVisibility(visible = isAdding) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("New task...", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = headerColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(headerColor)
                            .clickable {
                                if (newTaskText.isNotBlank()) {
                                    onAddItem(newTaskText)
                                    newTaskText = ""
                                    isAdding = false
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Add", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Items list (showing up to 3 items in widget view)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.take(3).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleItem(item.id) }
                            .padding(vertical = 3.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item.isCompleted) checkColor else Color.Transparent
                                )
                                .then(
                                    if (!item.isCompleted) {
                                        Modifier.background(
                                            Color.White.copy(alpha = if (isNightMode) 0.2f else 0.25f),
                                            CircleShape
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = item.title,
                            color = if (item.isCompleted) textColor.copy(alpha = 0.45f) else textColor,
                            fontSize = 13.sp,
                            fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Subtle delete icon
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { onDeleteItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}
