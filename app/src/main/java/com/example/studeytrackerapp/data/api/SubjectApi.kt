package com.example.studeytrackerapp.data.api

import retrofit2.http.GET

interface SubjectApi {
    @GET("subjects")
    suspend fun getSubjects(): List<SessionApiResponse>
}

