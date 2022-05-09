package com.dicoding.picodiploma.submission_story_app.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.picodiploma.submission_story_app.data.api.ApiService
import com.dicoding.picodiploma.submission_story_app.data.local.StoryDatabase
import com.dicoding.picodiploma.submission_story_app.data.response.ListStoryItem
import com.dicoding.picodiploma.submission_story_app.data.helper.Result
import com.dicoding.picodiploma.submission_story_app.data.remotemediator.StoryRemoteMediator
import com.dicoding.picodiploma.submission_story_app.data.response.AddStoryResponse
import com.dicoding.picodiploma.submission_story_app.data.response.StoriesResponse
import com.dicoding.picodiploma.submission_story_app.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {

    fun postStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): LiveData<Result<AddStoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.postStory("Bearer $token",imageMultipart, description,  lat, lon)
            if (!response.error) {
                emit(Result.Success(response))
            } else {
                Log.e(TAG, "PostStory Fail: ${response.message}")
                emit(Result.Error(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "PostStory Exception: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getPagingStories(token: String): Flow<PagingData<ListStoryItem>> {
        wrapEspressoIdlingResource {
            @OptIn(ExperimentalPagingApi::class)
            return Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getStories()
                }
            ).flow
        }
    }

    fun getAllStoriesWithLocation(token: String): LiveData<Result<List<ListStoryItem>>> =
        liveData {
            emit(Result.Loading)
            try {
                val bearerToken = generateBearerToken(token)
                val response = apiService.getStories(bearerToken, size = 30, location = 1)
                if (!response.error) {
                    emit(Result.Success(response.listStory))
                } else {
                    Log.e(TAG, "GetStoryMap Fail: ${response.message}")
                    emit(Result.Error(response.message))
                }

            } catch (e: Exception) {
                Log.e(TAG, "GetStoryMap Exception: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }


    private fun generateBearerToken(token: String): String {
        return "Bearer $token"
    }

    companion object {
        private const val TAG = "StoryRepository"
    }
}