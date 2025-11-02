package com.example.studeytrackerapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studeytrackerapp.data.database.SubjectDurationPair
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeeklyChart(
    chartData: List<SubjectDurationPair>,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available for the last 7 days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    val totalMinutes = chartData.sumOf { it.totalDuration }
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFFFFD740),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA)
    )
    
    val pieData = chartData.mapIndexed { index, pair ->
        val percentage = (pair.totalDuration.toFloat() / totalMinutes * 100)
        PieSliceData(
            label = pair.subjectName,
            percentage = percentage.toInt(),
            value = pair.totalDuration.toFloat(),
            color = colors[index % colors.size]
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weekly Study Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawPieChart(pieData, this.size)
                    }
                }
                
                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pieData.forEach { slice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = slice.color,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Text(
                                text = "${slice.label}: ${slice.percentage}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PieSliceData(
    val label: String,
    val percentage: Int,
    val value: Float,
    val color: Color
)

private fun DrawScope.drawPieChart(
    data: List<PieSliceData>,
    size: Size
) {
    val totalValue = data.sumOf { it.value.toDouble() }.toFloat()
    val center = Offset(size.width / 2, size.height / 2)
    val radius = minOf(size.width, size.height) / 2 * 0.8f
    val startAngle = -90f
    
    var currentAngle = startAngle
    
    data.forEach { slice ->
        val sweepAngle = (slice.value / totalValue) * 360f
        
        drawArc(
            color = slice.color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        
        currentAngle += sweepAngle
    }
}

