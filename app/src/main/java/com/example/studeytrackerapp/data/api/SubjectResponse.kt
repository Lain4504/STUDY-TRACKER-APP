package com.example.studeytrackerapp.data.api

import com.google.gson.annotations.SerializedName

// API Response format - sessions from API
data class SessionApiResponse(
    val id: String,
    @SerializedName("subject_name")
    val subjectName: String,
    @SerializedName("subject_date")
    val subjectDate: String?,
    val duration: Int?,
    val level: Int?,
    val notes: String?,
    val name: String? // Some entries have "name" instead of other fields
)

// Subject list for UI
data class SubjectResponse(
    val id: String,
    val name: String,
    val iconUrl: String?
)

