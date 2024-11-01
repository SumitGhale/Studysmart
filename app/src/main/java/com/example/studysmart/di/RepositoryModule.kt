package com.example.studysmart.di

import com.example.studysmart.data.repository.SessionRepositoryImplementation
import com.example.studysmart.data.repository.SubjectRepositoryImplementation
import com.example.studysmart.data.repository.TaskRepositoryImplementation
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindSubjectRepository(impl: SubjectRepositoryImplementation):SubjectRepository

    @Singleton
    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImplementation):TaskRepository

    @Singleton
    @Binds
    abstract fun sessionSubjectRepository(impl: SessionRepositoryImplementation):SessionRepository
}