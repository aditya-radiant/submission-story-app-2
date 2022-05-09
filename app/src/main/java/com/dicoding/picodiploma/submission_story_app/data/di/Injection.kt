package com.dicoding.picodiploma.submission_story_app.data.di

import android.content.Context
import com.dicoding.picodiploma.submission_story_app.data.api.ApiConfig
import com.dicoding.picodiploma.submission_story_app.data.local.StoryDatabase
import com.dicoding.picodiploma.submission_story_app.data.repository.StoryRepository
import com.dicoding.picodiploma.submission_story_app.data.repository.UserRepository


object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val apiService = ApiConfig().getApiService()
        return StoryRepository(database, apiService)
    }

    fun provideUserRepository(): UserRepository {
        val apiService = ApiConfig().getApiService()
        return UserRepository.getInstance(apiService)
    }
}