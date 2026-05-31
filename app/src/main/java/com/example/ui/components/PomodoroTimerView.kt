package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FocusViewModel
import java.util.Locale

@Composable
fun PomodoroTimerView(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val totalSeconds by viewModel.pomodoroTotal.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.pomodoroRemaining.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isWork by viewModel.isWorkPeriod.collectAsStateWithLifecycle()
    val completedCycles by viewModel.pomodoroCompletedCount.collectAsStateWithLifecycle()

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 1f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val currentThemeIsBlack by viewModel.isBlackTheme.collectAsStateWithLifecycle()
    val accentColor = if (isWork) {
        if (currentThemeIsBlack) Color(0xFFA855F7) else Color(0xFF6750A4) // purple work
    } else {
        Color(0xFF10B981) // emerald break
    }

    val timerStateLabel = if (isWork) "Work & Focus" else "Rest & Recharge"
    val timerSubLabel = if (isWork) "Feed your mind with deep work" else "Enjoy 5 minutes of mindful peace"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top status card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = timerStateLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timerSubLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🏆 $completedCycles Done",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    )
                }
            }
        }

        // Timer Dial
        Box(
            modifier = Modifier
                .size(260.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    style = Stroke(width = 10.dp.toPx())
                )
                // Countdown Arc
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeLabel,
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isWork) "🎯 FOCUSING" else "☕ BREAKTIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Preset chips for Quick Duration setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(15, 25, 45, 60).forEach { mins ->
                val isSelected = totalSeconds == mins * 60
                val buttonBg = if (isSelected) accentColor else MaterialTheme.colorScheme.surface
                val buttonText = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(buttonBg)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.customDuration(mins) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = buttonText
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Main Control Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            // Switch Mode (Work <-> Break manually)
            FilledTonalButton(
                onClick = { viewModel.toggleSessionMode() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (isWork) "☕ Switch Break" else "🎯 Switch Work",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Start / Pause Floating Action style
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .testTag("pomodoro_play_btn"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (currentThemeIsBlack) Color.White else MaterialTheme.colorScheme.background
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isRunning) {
                    // Custom Pause visual representation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp, 18.dp).background(Color.Unspecified).clip(RoundedCornerShape(1.dp)).background(if (currentThemeIsBlack) Color.White else Color.Black))
                        Box(modifier = Modifier.size(6.dp, 18.dp).background(Color.Unspecified).clip(RoundedCornerShape(1.dp)).background(if (currentThemeIsBlack) Color.White else Color.Black))
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Countdown"
                    )
                }
            }

            // Reset Duration
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .testTag("pomodoro_reset_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer Clock",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
