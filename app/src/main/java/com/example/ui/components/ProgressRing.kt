package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBorderLight
import com.example.ui.theme.SleekPrimary

@Composable
fun ProgressRing(
    progress: Float, // value between 0.0f and 1.0f
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    gradientColors: List<Color> = listOf(
        SleekPrimary,
        SleekPrimary.copy(alpha = 0.8f)
    ),
    trackColor: Color = SleekBorderLight,
    centerLabel: String? = null
) {
    val cleanProgress = if (progress.isNaN() || progress.isInfinite()) 0f else progress.coerceIn(0.0f, 1.0f)
    
    // Smooth timing transition for changes
    val animatedProgress by animateFloatAsState(
        targetValue = cleanProgress,
        animationSpec = tween(durationMillis = 800),
        label = "ProgressRingSweep"
    )

    val labelText = centerLabel ?: "${(cleanProgress * 100).toInt()}%"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("progress_ring_box")
            .semantics {
                contentDescription = "Task completion progress: $labelText"
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val canvasSize = this.size
            val arcSize = Size(
                width = canvasSize.width - strokeWidthPx,
                height = canvasSize.height - strokeWidthPx
            )
            val arcOffset = Offset(
                x = strokeWidthPx / 2,
                y = strokeWidthPx / 2
            )

            // Draw track background circle
            drawCircle(
                color = trackColor,
                radius = (canvasSize.width - strokeWidthPx) / 2,
                center = Offset(canvasSize.width / 2, canvasSize.height / 2),
                style = Stroke(width = strokeWidthPx)
            )

            // Draw progress arc with gradient brush
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = arcOffset
                )
            }
        }

        // Center typography label showing completion %
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.22f).sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.testTag("progress_percent_label")
        )
    }
}
