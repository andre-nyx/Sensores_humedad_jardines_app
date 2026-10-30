package com.jardines.sensores.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jardines.sensores.model.HistoricalPoint
import com.jardines.sensores.ui.theme.GreenPrimary
import com.jardines.sensores.ui.theme.StatusSecoColor

@Composable
fun MoistureChart(
    points: List<HistoricalPoint>,
    minThreshold: Int,
    maxThreshold: Int,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier = modifier.height(160.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos de historial disponibles.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val chartLineColor = GreenPrimary
    val minThresholdColor = StatusSecoColor.copy(alpha = 0.8f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Historial de Humedad (24 Horas)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "-- Mín. recomendado ($minThreshold%)",
                fontSize = 11.sp,
                color = minThresholdColor,
                fontWeight = FontWeight.Medium
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height

            val paddingX = 20f
            val paddingY = 20f

            val chartWidth = width - (paddingX * 2)
            val chartHeight = height - (paddingY * 2)

            // Draw horizontal grid lines (0%, 50%, 100%)
            val gridLevels = listOf(0.2f, 0.5f, 0.8f)
            gridLevels.forEach { ratio ->
                val y = paddingY + chartHeight * (1f - ratio)
                drawLine(
                    color = gridColor,
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Draw Min Threshold line
            val minRatio = (minThreshold / 100f).coerceIn(0f, 1f)
            val minY = paddingY + chartHeight * (1f - minRatio)
            drawLine(
                color = minThresholdColor,
                start = Offset(paddingX, minY),
                end = Offset(width - paddingX, minY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            // Draw moisture line
            val path = Path()
            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

            points.forEachIndexed { index, point ->
                val ratioY = (point.moistureValue / 100f).coerceIn(0f, 1f)
                val x = paddingX + (index * stepX)
                val y = paddingY + chartHeight * (1f - ratioY)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw point circle
                drawCircle(
                    color = chartLineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = chartLineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Time labels below chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            points.forEach { point ->
                Text(
                    text = point.timeLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
