package com.example.studeytrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studeytrackerapp.data.api.SubjectResponse
import com.example.studeytrackerapp.data.database.StudySession
import com.example.studeytrackerapp.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class AddSessionState(
    val selectedSubject: SubjectResponse? = null,
    val date: Date = Date(),
    val duration: String = "",
    val focusLevel: Int = 3,
    val notes: String = "",
    val subjects: List<SubjectResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AddSessionViewModel(
    private val repository: StudyRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AddSessionState>(AddSessionState())
    val state: StateFlow<AddSessionState> = _state.asStateFlow()
    
    init {
        loadSubjects()
    }
    
    private fun loadSubjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val subjects = repository.getSubjects()
                android.util.Log.d("AddSessionViewModel", "Loaded ${subjects.size} subjects from API")
                _state.value = _state.value.copy(
                    subjects = subjects,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                android.util.Log.e("AddSessionViewModel", "Error loading subjects", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load subjects: ${e.message}"
                )
            }
        }
    }
    
    fun selectSubject(subject: SubjectResponse) {
        _state.value = _state.value.copy(selectedSubject = subject)
    }
    
    fun setDate(date: Date) {
        _state.value = _state.value.copy(date = date)
    }
    
    fun setDuration(duration: String) {
        _state.value = _state.value.copy(duration = duration)
    }
    
    fun setFocusLevel(level: Int) {
        if (level in 1..5) {
            _state.value = _state.value.copy(focusLevel = level)
        }
    }
    
    fun setNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }
    
    suspend fun saveSession(): Boolean {
        val currentState = _state.value
        
        // Validation
        if (currentState.selectedSubject == null) {
            _state.value = currentState.copy(errorMessage = "Please select a subject")
            return false
        }
        
        val durationInt = currentState.duration.toIntOrNull()
        if (durationInt == null || durationInt <= 0) {
            _state.value = currentState.copy(errorMessage = "Duration must be greater than 0")
            return false
        }
        
        val session = StudySession(
            subjectName = currentState.selectedSubject!!.name,
            subjectIconUrl = currentState.selectedSubject!!.iconUrl,
            date = currentState.date,
            duration = durationInt,
            focusLevel = currentState.focusLevel,
            notes = currentState.notes.ifBlank { null }
        )
        
        return try {
            repository.insertSession(session)
            true
        } catch (e: Exception) {
            _state.value = currentState.copy(errorMessage = "Failed to save session: ${e.message}")
            false
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

