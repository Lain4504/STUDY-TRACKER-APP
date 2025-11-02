package com.example.studeytrackerapp.data.repository

import com.example.studeytrackerapp.data.api.Content
import com.example.studeytrackerapp.data.api.GeminiApi
import com.example.studeytrackerapp.data.api.GeminiRequest
import com.example.studeytrackerapp.data.api.Part
import com.example.studeytrackerapp.data.database.StudySession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for AI-related operations following the Repository Pattern.
 * This separates the business logic from data sources and makes the code testable.
 */
class AIRepository(
    private val geminiApi: GeminiApi,
    private val apiKey: String
) {
    
    suspend fun generateStudyTips(sessions: List<StudySession>): Result<List<String>> = withContext(Dispatchers.IO) {
        if (sessions.isEmpty() || sessions.size < 3) {
            return@withContext Result.success(emptyList())
        }
        
        try {
            // Analyze study patterns
            val analysis = analyzeStudyPatterns(sessions)
            
            // Create prompt for Gemini
            val prompt = buildPrompt(analysis, sessions.size)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = geminiApi.generateContent(apiKey, request)
            
            // Parse response
            val tips = parseTipsFromResponse(response)
            
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildPrompt(analysis: StudyAnalysis, sessionCount: Int): String {
        return """
            Based on the following study session analysis, provide 2-3 personalized study tips in Vietnamese. 
            Each tip should be concise (under 100 words) and actionable.
            
            Study Analysis:
            - Total sessions: $sessionCount
            - Average focus level: ${String.format("%.1f", analysis.avgFocus)}
            - Most studied subject: ${analysis.mostStudiedSubject ?: "N/A"}
            - Total study time: ${analysis.totalMinutes} minutes
            - Sessions by time of day: ${analysis.timeOfDayDistribution}
            - Subject distribution: ${analysis.subjectDistribution}
            
            Provide tips in this format:
            1. [Title]: [Description]
            2. [Title]: [Description]
            3. [Title]: [Description]
        """.trimIndent()
    }
    
    private fun parseTipsFromResponse(response: com.example.studeytrackerapp.data.api.GeminiResponse): List<String> {
        val tips = mutableListOf<String>()
        response.candidates.firstOrNull()?.content?.parts?.forEach { part ->
            val text = part.text
            // Split by numbered lines
            val lines = text.split("\n")
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank() && (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3."))) {
                    tips.add(trimmed)
                }
            }
        }
        return tips.take(3)
    }
    
    private data class StudyAnalysis(
        val avgFocus: Double,
        val mostStudiedSubject: String?,
        val totalMinutes: Int,
        val timeOfDayDistribution: String,
        val subjectDistribution: String
    )
    
    private fun analyzeStudyPatterns(sessions: List<StudySession>): StudyAnalysis {
        val avgFocus = sessions.map { it.focusLevel }.average()
        val mostStudiedSubject = sessions.groupBy { it.subjectName }
            .mapValues { it.value.sumOf { s -> s.duration.toLong() } }
            .maxByOrNull { it.value }?.key
        
        val totalMinutes = sessions.sumOf { it.duration }
        
        // Time of day distribution
        val timeGroups = sessions.groupBy { session ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = session.date
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            when {
                hour < 12 -> "Morning"
                hour < 18 -> "Afternoon"
                else -> "Evening"
            }
        }
        val timeOfDayDist = timeGroups.map { "${it.key}: ${it.value.size} sessions" }.joinToString(", ")
        
        // Subject distribution
        val subjectDist = sessions.groupBy { it.subjectName }
            .mapValues { "${it.value.size} sessions" }
            .entries.joinToString(", ") { "${it.key} (${it.value})" }
        
        return StudyAnalysis(
            avgFocus = avgFocus,
            mostStudiedSubject = mostStudiedSubject,
            totalMinutes = totalMinutes,
            timeOfDayDistribution = timeOfDayDist,
            subjectDistribution = subjectDist
        )
    }
}

