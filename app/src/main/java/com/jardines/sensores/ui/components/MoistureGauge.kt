package com.jardines.sensores.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jardines.sensores.model.MoistureStatus
import com.jardines.sensores.ui.theme.StatusExcesoColor
import com.jardines.sensores.ui.theme.StatusOptimoColor
import com.jardines.sensores.ui.theme.StatusSecoColor

@Composable
fun MoistureGauge(
    moisturePercentage: Int,
    status: MoistureStatus,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 16.dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = moisturePercentage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "GaugeAnimation"
    )

    val gaugeColor = when (status) {
        MoistureStatus.SECO -> StatusSecoColor
        MoistureStatus.OPTIMO -> StatusOptimoColor
        MoistureStatus.EXCESO -> StatusExcesoColor
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val startAngle = 135f
            val sweepAngle = 270f

            // Background arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            val progressSweep = (animatedPercentage / 100f) * sweepAngle
            drawArc(
                color = gaugeColor,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${moisturePercentage}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Humedad Suelo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when(status) {
                    MoistureStatus.SECO -> "🌵 SECO"
                    MoistureStatus.OPTIMO -> "🌱 ÓPTIMO"
                    MoistureStatus.EXCESO -> "💧 EXCESO"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = gaugeColor
            )
        }
    }
}
