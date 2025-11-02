package com.example.studeytrackerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studeytrackerapp.ui.components.*
import com.example.studeytrackerapp.ui.viewmodel.HomeViewModel
import com.example.studeytrackerapp.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val chartData by viewModel.weeklyChartData.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val aiTips by viewModel.aiTips.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Summary") },
                actions = {
                    IconButton(onClick = onAddSessionClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Session"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                // Summary Header
                SummaryHeader(summary = summary)
            }
            
            item {
                // Weekly Chart
                WeeklyChart(chartData = chartData)
            }
            
            item {
                // AI Study Tips
                StudyTipsFeature(sessions = sessions, aiTips = aiTips)
            }
            
            item {
                // Filter Bar
                FilterBar(
                    subjects = subjects,
                    filterState = filterState,
                    onFilterChange = { viewModel.applyFilters(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )
            }
            
            item {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sessions yet. Add your first session!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(sessions) { session ->
                    StudySessionItem(session = session)
                }
            }
        }
    }
}

@Composable
fun SummaryHeader(
    summary: com.example.studeytrackerapp.ui.viewmodel.StudySummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "This Week",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Time Card
            SummaryCard(
                title = "Total Time",
                value = formatDuration(summary.totalTimeMinutes),
                modifier = Modifier.weight(1f)
            )
            
            // Most Studied Subject Card
            SummaryCard(
                title = "Top Subject",
                value = summary.mostStudiedSubject ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            
            // Average Focus Card
            SummaryCard(
                title = "Avg Focus",
                value = String.format("%.1f", summary.averageFocus),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}h ${mins}m"
    } else {
        "${mins}m"
    }
}

