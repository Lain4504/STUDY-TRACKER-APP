package com.example.studeytrackerapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val subjectIconUrl: String?,
    val date: Date,
    val duration: Int, // in minutes
    val focusLevel: Int, // 1-5
    val notes: String? = null
)

