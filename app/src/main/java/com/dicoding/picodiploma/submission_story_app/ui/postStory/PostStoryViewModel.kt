package com.dicoding.picodiploma.submission_story_app.ui.postStory

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.submission_story_app.data.repository.StoryRepository
import com.dicoding.picodiploma.submission_story_app.data.repository.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostStoryViewModel(private val storyRepository: StoryRepository): ViewModel(){
    fun postStory(
        token: String,
        description: RequestBody,
        imageMultipart: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ) = storyRepository.postStory(token, imageMultipart, description, lat, lon)
}