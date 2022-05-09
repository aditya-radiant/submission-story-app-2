package com.dicoding.picodiploma.submission_story_app.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.submission_story_app.data.repository.StoryRepository
import com.dicoding.picodiploma.submission_story_app.data.response.ListStoryItem

class StoryViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepository.getPagingStories(token).cachedIn(viewModelScope).asLiveData()
    }
}