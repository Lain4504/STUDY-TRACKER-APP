package com.example.studeytrackerapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<StudySession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long
    
    @Delete
    suspend fun deleteSession(session: StudySession)
    
    @Query("SELECT * FROM study_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsByDateRange(startDate: Date, endDate: Date): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE subjectName = :subject ORDER BY date DESC")
    fun getSessionsBySubject(subject: String): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE focusLevel >= :minFocus AND focusLevel <= :maxFocus ORDER BY date DESC")
    fun getSessionsByFocusRange(minFocus: Int, maxFocus: Int): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE notes LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    fun searchSessionsByNotes(searchQuery: String): Flow<List<StudySession>>
    
    // Filter by multiple criteria
    @Query("""
        SELECT * FROM study_sessions 
        WHERE (:subject IS NULL OR subjectName = :subject)
        AND (:minFocus IS NULL OR focusLevel >= :minFocus)
        AND (:maxFocus IS NULL OR focusLevel <= :maxFocus)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:searchQuery IS NULL OR notes LIKE '%' || :searchQuery || '%')
        ORDER BY date DESC
    """)
    fun filterSessions(
        subject: String?,
        minFocus: Int?,
        maxFocus: Int?,
        startDate: Date?,
        endDate: Date?,
        searchQuery: String?
    ): Flow<List<StudySession>>
    
    // Aggregation queries for summary
    @Query("""
        SELECT SUM(duration) FROM study_sessions 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getTotalDurationForWeek(startDate: Date, endDate: Date): Int?
    
    @Query("""
        SELECT subjectName, SUM(duration) as totalDuration 
        FROM study_sessions 
        WHERE date >= :startDate AND date <= :endDate
        GROUP BY subjectName 
        ORDER BY totalDuration DESC 
        LIMIT 1
    """)
    suspend fun getMostStudiedSubject(startDate: Date, endDate: Date): SubjectDurationPair?
    
    @Query("""
        SELECT AVG(focusLevel) FROM study_sessions 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getAverageFocusLevel(startDate: Date, endDate: Date): Double?
    
    @Query("""
        SELECT subjectName, SUM(duration) as totalDuration 
        FROM study_sessions 
        WHERE date >= :startDate AND date <= :endDate
        GROUP BY subjectName
    """)
    suspend fun getWeeklySubjectDurations(startDate: Date, endDate: Date): List<SubjectDurationPair>
}

data class SubjectDurationPair(
    val subjectName: String,
    val totalDuration: Long
)

