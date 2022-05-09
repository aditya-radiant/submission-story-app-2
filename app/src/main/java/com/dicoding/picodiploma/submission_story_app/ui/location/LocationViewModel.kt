package com.dicoding.picodiploma.submission_story_app.ui.location

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.submission_story_app.data.repository.StoryRepository

class LocationViewModel(private val storyRepository: StoryRepository): ViewModel(){

    fun getStoriesLocation(token: String) = storyRepository.getAllStoriesWithLocation(token)
}