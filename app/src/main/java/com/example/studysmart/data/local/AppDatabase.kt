package com.example.studysmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studysmart.domain.modal.Session
import com.example.studysmart.domain.modal.Subject
import com.example.studysmart.domain.modal.Task

@Database(
    entities = [Subject::class, Session::class, Task::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(ColorListConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun subjectDao():SubjectDao
    abstract fun taskDao():TaskDao
    abstract fun sessionDao():SessionDao
}