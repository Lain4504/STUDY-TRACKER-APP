package com.example.studeytrackerapp.util

import java.util.Locale

object SubjectIconMapper {
    // Map subject names to icon URLs or emoji/icons
    // Since API doesn't provide icons, we'll create a mapping
    private val subjectIconMap = mapOf(
        "Mathematics" to "https://cdn-icons-png.flaticon.com/512/2103/2103633.png",
        "Physics" to "https://cdn-icons-png.flaticon.com/512/2103/2103662.png",
        "Chemistry" to "https://cdn-icons-png.flaticon.com/512/2103/2103651.png",
        "Biology" to "https://cdn-icons-png.flaticon.com/512/2103/2103632.png",
        "History" to "https://cdn-icons-png.flaticon.com/512/2103/2103683.png",
        "Geography" to "https://cdn-icons-png.flaticon.com/512/2103/2103659.png",
        "English Literature" to "https://cdn-icons-png.flaticon.com/512/2103/2103663.png",
        "Computer Science" to "https://cdn-icons-png.flaticon.com/512/2103/2103645.png",
        "Economics" to "https://cdn-icons-png.flaticon.com/512/2103/2103654.png",
        "Art" to "https://cdn-icons-png.flaticon.com/512/2103/2103642.png"
    )
    
    fun getIconUrl(subjectName: String): String? {
        // Try exact match first
        subjectIconMap[subjectName]?.let { return it }
        
        // Try case-insensitive match
        subjectIconMap.entries.firstOrNull { 
            it.key.equals(subjectName, ignoreCase = true) 
        }?.value?.let { return it }
        
        // Default icon for unknown subjects
        return "https://cdn-icons-png.flaticon.com/512/2103/2103637.png"
    }
    
    fun extractUniqueSubjects(sessions: List<com.example.studeytrackerapp.data.api.SessionApiResponse>): List<com.example.studeytrackerapp.data.api.SubjectResponse> {
        val uniqueSubjects = sessions
            .mapNotNull { session ->
                // Prioritize subject_name over name
                // subject_name is the actual subject, name might be person name
                val subjectName = when {
                    session.subjectName.isNotBlank() -> session.subjectName
                    session.name?.isNotBlank() == true -> {
                        // If subject_name is missing but name exists, check if it's a valid subject name
                        // Valid subject names are usually in our icon map or follow certain patterns
                        val name = session.name!!
                        if (subjectIconMap.keys.any { it.equals(name, ignoreCase = true) }) {
                            name
                        } else {
                            // If name doesn't match known subjects, skip this entry
                            null
                        }
                    }
                    else -> null
                }
                subjectName
            }
            .distinct()
            .sorted()
        
        return uniqueSubjects.mapIndexed { index, name ->
            com.example.studeytrackerapp.data.api.SubjectResponse(
                id = (index + 1).toString(),
                name = name,
                iconUrl = getIconUrl(name)
            )
        }
    }
}

