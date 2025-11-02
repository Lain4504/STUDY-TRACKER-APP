package com.example.studeytrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studeytrackerapp.data.api.SubjectResponse
import com.example.studeytrackerapp.data.database.StudySession
import com.example.studeytrackerapp.data.database.SubjectDurationPair
import com.example.studeytrackerapp.data.repository.AIRepository
import com.example.studeytrackerapp.data.repository.StudyRepository
import com.example.studeytrackerapp.util.DateFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.Date

data class StudySummary(
    val totalTimeMinutes: Int = 0,
    val mostStudiedSubject: String? = null,
    val averageFocus: Double = 0.0
)

data class FilterState(
    val selectedSubject: String? = null,
    val minFocus: Int? = null,
    val maxFocus: Int? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val searchQuery: String? = null
)

class HomeViewModel(
    private val repository: StudyRepository,
    private val aiRepository: AIRepository?
) : ViewModel() {
    
    private val _sessions = MutableStateFlow<List<StudySession>>(emptyList())
    val sessions: StateFlow<List<StudySession>> = _sessions.asStateFlow()
    
    private val _summary = MutableStateFlow<StudySummary>(StudySummary())
    val summary: StateFlow<StudySummary> = _summary.asStateFlow()
    
    private val _filterState = MutableStateFlow<FilterState>(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _weeklyChartData = MutableStateFlow<List<SubjectDurationPair>>(emptyList())
    val weeklyChartData: StateFlow<List<SubjectDurationPair>> = _weeklyChartData.asStateFlow()
    
    private val _subjects = MutableStateFlow<List<SubjectResponse>>(emptyList())
    val subjects: StateFlow<List<SubjectResponse>> = _subjects.asStateFlow()
    
    private val _aiTips = MutableStateFlow<List<String>>(emptyList())
    val aiTips: StateFlow<List<String>> = _aiTips.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var reloadJob: Job? = null
    
    init {
        loadSubjects()
        loadSessions()
        loadSummary()
        loadWeeklyChartData()
    }
    
    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                // Sync API sessions to database first
                repository.syncApiSessionsToDatabase()
                
                // Then load subjects for dropdown
                repository.refreshSubjects()
                val subjectsList = repository.getSubjects()
                android.util.Log.d("HomeViewModel", "Loaded ${subjectsList.size} subjects from API")
                _subjects.value = subjectsList
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading subjects", e)
                // Handle error silently or show error message
            }
        }
    }
    
    private fun loadSessions() {
        viewModelScope.launch {
            repository.getAllSessions().collect { sessionsList ->
                _sessions.value = sessionsList
                
                // Reload summary and chart data when sessions change
                reloadSummaryAndChart()
                
                // Generate AI tips when sessions are loaded
                if (sessionsList.size >= 3) {
                    loadAITips(sessionsList)
                }
            }
        }
    }
    
    private fun reloadSummaryAndChart() {
        // Cancel previous reload job to avoid multiple concurrent reloads
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            try {
                // Reload summary
                val startDate = DateFormatter.getWeekStartDate()
                val endDate = DateFormatter.getWeekEndDate()
                
                val totalTime = repository.getTotalDurationForWeek(startDate, endDate)
                val mostStudied = repository.getMostStudiedSubject(startDate, endDate)
                val avgFocus = repository.getAverageFocusLevel(startDate, endDate)
                
                _summary.value = StudySummary(
                    totalTimeMinutes = totalTime,
                    mostStudiedSubject = mostStudied,
                    averageFocus = avgFocus ?: 0.0
                )
                
                // Reload chart data
                val chartStartDate = DateFormatter.getLast7DaysStart()
                val chartEndDate = DateFormatter.getLast7DaysEnd()
                
                val chartData = repository.getWeeklySubjectDurations(chartStartDate, chartEndDate)
                _weeklyChartData.value = chartData
                
                android.util.Log.d("HomeViewModel", "Reloaded summary and chart: ${chartData.size} subjects, ${totalTime} minutes")
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error reloading summary and chart", e)
            }
        }
    }
    
    private fun loadAITips(sessions: List<StudySession>) {
        viewModelScope.launch {
            try {
                val tips = aiRepository?.generateStudyTips(sessions)
                if (tips != null) {
                    if (tips.isSuccess) {
                        val tipsList = tips.getOrNull() ?: emptyList()
                        _aiTips.value = tipsList
                        if (tipsList.isNotEmpty()) {
                            android.util.Log.d("HomeViewModel", "Loaded ${tipsList.size} AI tips from Gemini")
                        }
                    } else {
                        android.util.Log.e("HomeViewModel", "Error generating AI tips: ${tips.exceptionOrNull()}")
                        _aiTips.value = emptyList()
                    }
                } else {
                    _aiTips.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error generating AI tips", e)
                // Fallback to empty list, local analysis will be used
                _aiTips.value = emptyList()
            }
        }
    }
    
    private fun loadSummary() {
        viewModelScope.launch {
            val startDate = DateFormatter.getWeekStartDate()
            val endDate = DateFormatter.getWeekEndDate()
            
            val totalTime = repository.getTotalDurationForWeek(startDate, endDate)
            val mostStudied = repository.getMostStudiedSubject(startDate, endDate)
            val avgFocus = repository.getAverageFocusLevel(startDate, endDate)
            
            _summary.value = StudySummary(
                totalTimeMinutes = totalTime,
                mostStudiedSubject = mostStudied,
                averageFocus = avgFocus ?: 0.0
            )
        }
    }
    
    private fun loadWeeklyChartData() {
        viewModelScope.launch {
            val startDate = DateFormatter.getLast7DaysStart()
            val endDate = DateFormatter.getLast7DaysEnd()
            
            val chartData = repository.getWeeklySubjectDurations(startDate, endDate)
            _weeklyChartData.value = chartData
        }
    }
    
    fun applyFilters(filterState: FilterState) {
        viewModelScope.launch {
            _filterState.value = filterState
            
            repository.filterSessions(
                subject = filterState.selectedSubject,
                minFocus = filterState.minFocus,
                maxFocus = filterState.maxFocus,
                startDate = filterState.startDate,
                endDate = filterState.endDate,
                searchQuery = filterState.searchQuery
            ).collect { filteredSessions ->
                _sessions.value = filteredSessions
            }
        }
    }
    
    fun clearFilters() {
        viewModelScope.launch {
            _filterState.value = FilterState()
            repository.getAllSessions().collect { sessionsList ->
                _sessions.value = sessionsList
            }
        }
    }
    
    fun refresh() {
        loadSummary()
        loadWeeklyChartData()
        loadSubjects()
    }
}

