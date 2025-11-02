package com.example.studeytrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studeytrackerapp.data.api.SubjectResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelector(
    subjects: List<SubjectResponse>,
    selectedSubject: SubjectResponse?,
    onSubjectSelected: (SubjectResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSubject?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            placeholder = { Text("Select or type subject") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    onClick = {
                        onSubjectSelected(subject)
                        expanded = false
                    }
                )
            }
        }
    }
}

