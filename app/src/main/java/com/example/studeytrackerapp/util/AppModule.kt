package com.example.studeytrackerapp.util

import android.content.Context
import com.example.studeytrackerapp.data.api.GeminiApi
import com.example.studeytrackerapp.data.api.SubjectApi
import com.example.studeytrackerapp.data.database.StudyDatabase
import com.example.studeytrackerapp.data.repository.AIRepository
import com.example.studeytrackerapp.data.repository.StudyRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {
    private const val BASE_URL = "https://687319aac75558e273535336.mockapi.io/api/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private var database: StudyDatabase? = null
    private var repository: StudyRepository? = null
    private var subjectApi: SubjectApi? = null
    private var geminiApi: GeminiApi? = null
    private var aiRepository: AIRepository? = null
    private var geminiApiKey: String = ""
    
    fun provideDatabase(context: Context): StudyDatabase {
        if (database == null) {
            database = androidx.room.Room.databaseBuilder(
                context,
                StudyDatabase::class.java,
                "study_database"
            ).build()
        }
        return database!!
    }
    
    fun provideSubjectApi(): SubjectApi {
        if (subjectApi == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            subjectApi = retrofit.create(SubjectApi::class.java)
        }
        return subjectApi!!
    }
    
    fun provideRepository(context: Context): StudyRepository {
        if (repository == null) {
            repository = StudyRepository(
                provideDatabase(context).studySessionDao(),
                provideSubjectApi()
            )
        }
        return repository!!
    }
    
    fun provideGeminiApi(apiKey: String): GeminiApi {
        if (geminiApi == null || geminiApiKey != apiKey) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(GEMINI_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            geminiApi = retrofit.create(GeminiApi::class.java)
            geminiApiKey = apiKey
        }
        return geminiApi!!
    }
    
    fun provideAIRepository(apiKey: String): AIRepository {
        if (aiRepository == null || geminiApiKey != apiKey) {
            val api = provideGeminiApi(apiKey)
            aiRepository = AIRepository(api, apiKey)
        }
        return aiRepository!!
    }
}

