package com.example.studeytrackerapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [StudySession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
}

