package com.example.studeytrackerapp.data.repository

import com.example.studeytrackerapp.data.api.SessionApiResponse
import com.example.studeytrackerapp.data.api.SubjectApi
import com.example.studeytrackerapp.data.api.SubjectResponse
import com.example.studeytrackerapp.data.database.StudySession
import com.example.studeytrackerapp.data.database.StudySessionDao
import com.example.studeytrackerapp.data.database.SubjectDurationPair
import com.example.studeytrackerapp.util.SubjectIconMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class StudyRepository(
    private val studySessionDao: StudySessionDao,
    private val subjectApi: SubjectApi
) {
    // Subjects from API
    private var subjectsCache: List<SubjectResponse>? = null
    
    suspend fun getSubjects(): List<SubjectResponse> {
        if (subjectsCache == null) {
            try {
                // API returns sessions, extract unique subjects
                val apiSessions = subjectApi.getSubjects()
                val extractedSubjects = SubjectIconMapper.extractUniqueSubjects(apiSessions)
                subjectsCache = if (extractedSubjects.isEmpty()) {
                    // If extraction fails, return empty list
                    emptyList()
                } else {
                    extractedSubjects
                }
            } catch (e: Exception) {
                // Log error for debugging
                e.printStackTrace()
                // Return empty list if API fails
                return emptyList()
            }
        }
        return subjectsCache ?: emptyList()
    }
    
    fun getSubjectIconUrl(subjectName: String): String? {
        return SubjectIconMapper.getIconUrl(subjectName)
    }
    
    // Session operations
    fun getAllSessions(): Flow<List<StudySession>> = studySessionDao.getAllSessions()
    
    suspend fun insertSession(session: StudySession): Long {
        return studySessionDao.insertSession(session)
    }
    
    suspend fun deleteSession(session: StudySession) {
        studySessionDao.deleteSession(session)
    }
    
    // Filtering
    fun filterSessions(
        subject: String?,
        minFocus: Int?,
        maxFocus: Int?,
        startDate: Date?,
        endDate: Date?,
        searchQuery: String?
    ): Flow<List<StudySession>> {
        return studySessionDao.filterSessions(
            subject, minFocus, maxFocus, startDate, endDate, searchQuery
        )
    }
    
    // Summary calculations
    suspend fun getTotalDurationForWeek(startDate: Date, endDate: Date): Int {
        return studySessionDao.getTotalDurationForWeek(startDate, endDate) ?: 0
    }
    
    suspend fun getMostStudiedSubject(startDate: Date, endDate: Date): String? {
        val result = studySessionDao.getMostStudiedSubject(startDate, endDate)
        return result?.subjectName
    }
    
    suspend fun getAverageFocusLevel(startDate: Date, endDate: Date): Double {
        return studySessionDao.getAverageFocusLevel(startDate, endDate) ?: 0.0
    }
    
    suspend fun getWeeklySubjectDurations(startDate: Date, endDate: Date): List<SubjectDurationPair> {
        return studySessionDao.getWeeklySubjectDurations(startDate, endDate)
    }
    
    // Refresh subjects from API
    suspend fun refreshSubjects() {
        try {
            // API returns sessions, extract unique subjects
            val apiSessions = subjectApi.getSubjects()
            subjectsCache = SubjectIconMapper.extractUniqueSubjects(apiSessions)
        } catch (e: Exception) {
            // Keep existing cache on error
        }
    }
    
    // Sync API sessions to Room database
    suspend fun syncApiSessionsToDatabase() {
        try {
            val apiSessions = subjectApi.getSubjects()
            
            // Convert API sessions to StudySession entities
            val studySessions = apiSessions.mapNotNull { apiSession ->
                try {
                    convertApiSessionToStudySession(apiSession)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null // Skip invalid sessions
                }
            }
            
            // Get existing sessions from database to check duplicates
            // Use first() to get current snapshot of sessions
            val existingList = studySessionDao.getAllSessions().first()
            
            // Filter out duplicates (check by subject, date, duration)
            val newSessions = studySessions.filter { newSession ->
                !existingList.any { existing ->
                    existing.subjectName == newSession.subjectName &&
                    Math.abs(existing.date.time - newSession.date.time) < 60000 && // Same date (within 1 minute)
                    existing.duration == newSession.duration
                }
            }
            
            // Insert new sessions into database
            newSessions.forEach { session ->
                try {
                    studySessionDao.insertSession(session)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            android.util.Log.d("StudyRepository", "Synced ${newSessions.size} new sessions from API to database (${studySessions.size - newSessions.size} duplicates skipped)")
        } catch (e: Exception) {
            android.util.Log.e("StudyRepository", "Error syncing API sessions", e)
        }
    }
    
    private fun convertApiSessionToStudySession(apiSession: SessionApiResponse): StudySession? {
        // Parse date from API response
        val date = parseDate(apiSession.subjectDate) ?: return null
        
        // Get subject name (prioritize subject_name over name)
        val subjectName = when {
            apiSession.subjectName.isNotBlank() -> apiSession.subjectName
            apiSession.name?.isNotBlank() == true -> {
                // Check if name is a valid subject name
                val name = apiSession.name!!
                if (SubjectIconMapper.getIconUrl(name) != null) {
                    name
                } else {
                    return null // Skip if name is not a valid subject
                }
            }
            else -> return null
        }
        
        // Get duration (use duration from API, default to 0 if null)
        val duration = apiSession.duration ?: 0
        if (duration <= 0) return null // Skip invalid durations
        
        // Get focus level (use level from API, default to 3 if null)
        val focusLevel = apiSession.level?.coerceIn(1, 5) ?: 3
        
        // Get icon URL for subject
        val iconUrl = SubjectIconMapper.getIconUrl(subjectName)
        
        return StudySession(
            subjectName = subjectName,
            subjectIconUrl = iconUrl,
            date = date,
            duration = duration,
            focusLevel = focusLevel,
            notes = apiSession.notes?.ifBlank { null }
        )
    }
    
    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null
        
        // Handle different date formats from API
        val cleanDateString = dateString.trim()
        
        // Try ISO 8601 formats
        val formats = listOf(
            // Standard UTC format: 2025-07-14T09:00:00.000Z
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
            // Without milliseconds: 2025-07-14T09:00:00Z
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
            // With timezone offset: 2025-07-17T00:00:00+07:00
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            // Date only: 2025-10-27
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            // Without Z: 2025-07-14T09:00:00
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        )
        
        for (format in formats) {
            try {
                val date = format.parse(cleanDateString)
                if (date != null) {
                    return date
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        // Try parsing with timezone info (e.g., +07:00[Asia/Ho_Chi_Minh])
        try {
            // Remove timezone name if present: "2025-07-17T00:00:00+07:00[Asia/Ho_Chi_Minh]"
            val pattern = Regex("""(.+?)\[.*?\]""")
            val match = pattern.find(cleanDateString)
            val dateWithoutTzName = match?.groupValues?.get(1) ?: cleanDateString
            
            // Try parsing with timezone offset
            val offsetFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            return offsetFormat.parse(dateWithoutTzName)
        } catch (e: Exception) {
            // Ignore and return null
        }
        
        android.util.Log.w("StudyRepository", "Could not parse date: $dateString")
        return null
    }
}

