package com.example.studeytrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studeytrackerapp.data.database.StudySession
import java.util.Calendar

data class StudyTip(
    val title: String,
    val description: String,
    val type: TipType
)

enum class TipType {
    TIME_PATTERN,
    FOCUS_TREND,
    SUBJECT_BALANCE,
    DURATION_OPTIMIZATION
}

@Composable
fun StudyTipsFeature(
    sessions: List<StudySession>,
    aiTips: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Use AI tips if available, otherwise use local analysis
    val tips = if (aiTips.isNotEmpty()) {
        // Convert AI tips string format to StudyTip objects
        aiTips.mapIndexed { index, tipString ->
            val parts = tipString.split(":", limit = 2)
            val title = parts.getOrNull(0)?.trim()?.removePrefix("${index + 1}.")?.trim() ?: "Study Tip"
            val description = parts.getOrNull(1)?.trim() ?: tipString
            StudyTip(
                title = title,
                description = description,
                type = TipType.TIME_PATTERN // Default type for AI tips
            )
        }
    } else {
        analyzeStudyPatterns(sessions)
    }
    
    if (tips.isEmpty()) {
        return
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Study Tips",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "AI Study Tips",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            tips.forEach { tip ->
                StudyTipItem(tip = tip)
            }
        }
    }
}

@Composable
fun StudyTipItem(
    tip: StudyTip,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun analyzeStudyPatterns(sessions: List<StudySession>): List<StudyTip> {
    if (sessions.isEmpty() || sessions.size < 3) {
        return emptyList()
    }
    
    val tips = mutableListOf<StudyTip>()
    
    // Analyze time of day patterns
    val timeOfDayGroups = sessions.groupBy { session ->
        val calendar = Calendar.getInstance()
        calendar.time = session.date
        calendar.get(Calendar.HOUR_OF_DAY)
    }
    
    val mostProductiveHour = timeOfDayGroups.maxByOrNull { it.value.sumOf { s -> s.focusLevel } }
    mostProductiveHour?.let {
        val hour = it.key
        val avgFocus = it.value.map { s -> s.focusLevel }.average()
        if (avgFocus >= 4.0 && it.value.size >= 3) {
            val timeLabel = when {
                hour < 12 -> "morning ($hour:00)"
                hour < 18 -> "afternoon ($hour:00)"
                else -> "evening ($hour:00)"
            }
            tips.add(
                StudyTip(
                    title = "Peak Focus Time",
                    description = "You show highest focus levels during $timeLabel. Consider scheduling important study sessions during this time.",
                    type = TipType.TIME_PATTERN
                )
            )
        }
    }
    
    // Analyze focus trends
    val recentSessions = sessions.sortedByDescending { it.date }.take(5)
    val olderSessions = sessions.sortedByDescending { it.date }.drop(5).take(5)
    
    if (recentSessions.isNotEmpty() && olderSessions.isNotEmpty()) {
        val recentAvgFocus = recentSessions.map { it.focusLevel }.average()
        val olderAvgFocus = olderSessions.map { it.focusLevel }.average()
        
        if (recentAvgFocus < olderAvgFocus - 0.5) {
            tips.add(
                StudyTip(
                    title = "Focus Level Trend",
                    description = "Your focus levels have decreased recently. Consider taking breaks between sessions or adjusting your study environment.",
                    type = TipType.FOCUS_TREND
                )
            )
        }
    }
    
    // Analyze subject balance
    val subjectDuration = sessions.groupBy { it.subjectName }
        .mapValues { it.value.sumOf { s -> s.duration.toLong() } }
    
    if (subjectDuration.size >= 2) {
        val maxSubject = subjectDuration.maxByOrNull { it.value }
        val totalDuration = subjectDuration.values.sum()
        maxSubject?.let {
            val percentage = (it.value.toDouble() / totalDuration * 100).toInt()
            if (percentage > 60) {
                tips.add(
                    StudyTip(
                        title = "Subject Balance",
                        description = "${it.key} takes up $percentage% of your study time. Consider diversifying your subjects for better overall progress.",
                        type = TipType.SUBJECT_BALANCE
                    )
                )
            }
        }
    }
    
    // Analyze session duration patterns
    val longSessions = sessions.filter { it.duration > 120 }
    val shortSessions = sessions.filter { it.duration < 30 }
    
    if (longSessions.isNotEmpty()) {
        val avgFocusLong = longSessions.map { it.focusLevel }.average()
        val avgFocusShort = shortSessions.takeIf { it.isNotEmpty() }?.map { it.focusLevel }?.average() ?: 0.0
        
        if (avgFocusLong < avgFocusShort && shortSessions.isNotEmpty()) {
            tips.add(
                StudyTip(
                    title = "Optimal Session Duration",
                    description = "Your focus tends to decrease in sessions longer than 2 hours. Consider breaking long study sessions into shorter, focused blocks with breaks.",
                    type = TipType.DURATION_OPTIMIZATION
                )
            )
        }
    }
    
    return tips.take(3) // Return top 3 tips
}

