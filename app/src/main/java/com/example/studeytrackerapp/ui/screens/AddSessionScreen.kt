package com.example.studeytrackerapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studeytrackerapp.ui.components.*
import com.example.studeytrackerapp.ui.viewmodel.AddSessionViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionScreen(
    viewModel: AddSessionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val dateDialogState = rememberMaterialDialogState()
    
    var selectedDate by remember { mutableStateOf(Date()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Study Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Selection
            SubjectSelector(
                subjects = state.subjects,
                selectedSubject = state.selectedSubject,
                onSubjectSelected = { viewModel.selectSubject(it) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Date Picker
            OutlinedButton(
                onClick = { dateDialogState.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(state.date)}"
                )
            }
            
            // Duration Input
            OutlinedTextField(
                value = state.duration,
                onValueChange = { viewModel.setDuration(it) },
                label = { Text("Duration (minutes)") },
                placeholder = { Text("Enter duration") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Focus Level
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Focus Level: ${state.focusLevel}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Star Selector
                StarRating(
                    rating = state.focusLevel,
                    onRatingChange = { viewModel.setFocusLevel(it) },
                    enabled = true,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Slider Alternative
                Slider(
                    value = state.focusLevel.toFloat(),
                    onValueChange = { viewModel.setFocusLevel(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
            
            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add your reflections here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 5,
                singleLine = false
            )
            
            // Error Message
            state.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.saveSession()) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save Session")
                }
            }
        }
    }
    
    // Date Picker Dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("OK") {
                viewModel.setDate(selectedDate)
            }
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = LocalDate.now(),
            onDateChange = {
                selectedDate = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
        )
    }
}

