package com.example.studeytrackerapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studeytrackerapp.data.api.SubjectResponse
import com.example.studeytrackerapp.ui.viewmodel.FilterState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    subjects: List<SubjectResponse>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    onDateRangeSelected: (Date?, Date?) -> Unit = { _, _ -> }
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasActiveFilters(filterState)) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Filters"
                            )
                        }
                    }
                    
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Hide" else "Show")
                    }
                }
            }
            
            if (expanded) {
                // Subject Filter
                var subjectExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = filterState.selectedSubject ?: "All",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                onFilterChange(filterState.copy(selectedSubject = null))
                                subjectExpanded = false
                            }
                        )
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    onFilterChange(filterState.copy(selectedSubject = subject.name))
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Focus Level Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filterState.minFocus?.toString() ?: "",
                        onValueChange = {
                            onFilterChange(filterState.copy(minFocus = it.toIntOrNull()))
                        },
                        label = { Text("Min Focus") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("1") }
                    )
                    
                    OutlinedTextField(
                        value = filterState.maxFocus?.toString() ?: "",
                        onValueChange = {
                            onFilterChange(filterState.copy(maxFocus = it.toIntOrNull()))
                        },
                        label = { Text("Max Focus") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("5") }
                    )
                }
                
                // Date Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val startDateDialogState = rememberMaterialDialogState()
                    var startDateTemp by remember { mutableStateOf<Date?>(null) }
                    
                    OutlinedButton(
                        onClick = { startDateDialogState.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = filterState.startDate?.let { 
                                java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(it)
                            } ?: "From Date"
                        )
                    }
                    
                    MaterialDialog(
                        dialogState = startDateDialogState,
                        buttons = {
                            positiveButton("OK") {
                                startDateTemp?.let {
                                    onFilterChange(filterState.copy(startDate = it))
                                }
                            }
                            negativeButton("Cancel")
                        }
                    ) {
                        datepicker(
                            initialDate = filterState.startDate?.let {
                                LocalDate.ofInstant(
                                    java.time.Instant.ofEpochMilli(it.time),
                                    ZoneId.systemDefault()
                                )
                            } ?: LocalDate.now(),
                            onDateChange = {
                                startDateTemp = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            }
                        )
                    }
                    
                    val endDateDialogState = rememberMaterialDialogState()
                    var endDateTemp by remember { mutableStateOf<Date?>(null) }
                    
                    OutlinedButton(
                        onClick = { endDateDialogState.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = filterState.endDate?.let { 
                                java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(it)
                            } ?: "To Date"
                        )
                    }
                    
                    MaterialDialog(
                        dialogState = endDateDialogState,
                        buttons = {
                            positiveButton("OK") {
                                endDateTemp?.let {
                                    onFilterChange(filterState.copy(endDate = it))
                                }
                            }
                            negativeButton("Cancel")
                        }
                    ) {
                        datepicker(
                            initialDate = filterState.endDate?.let {
                                LocalDate.ofInstant(
                                    java.time.Instant.ofEpochMilli(it.time),
                                    ZoneId.systemDefault()
                                )
                            } ?: LocalDate.now(),
                            onDateChange = {
                                endDateTemp = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            }
                        )
                    }
                }
                
                // Search in Notes
                OutlinedTextField(
                    value = filterState.searchQuery ?: "",
                    onValueChange = {
                        onFilterChange(filterState.copy(searchQuery = it.ifBlank { null }))
                    },
                    label = { Text("Search in notes") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter search query...") }
                )
            }
        }
    }
}

private fun hasActiveFilters(filterState: FilterState): Boolean {
    return filterState.selectedSubject != null ||
            filterState.minFocus != null ||
            filterState.maxFocus != null ||
            filterState.startDate != null ||
            filterState.endDate != null ||
            !filterState.searchQuery.isNullOrBlank()
}

