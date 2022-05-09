package com.dicoding.picodiploma.submission_story_app.data.api

import com.dicoding.picodiploma.submission_story_app.data.response.AddStoryResponse
import com.dicoding.picodiploma.submission_story_app.data.response.LoginResponse
import com.dicoding.picodiploma.submission_story_app.data.response.RegisterResponse
import com.dicoding.picodiploma.submission_story_app.data.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService{
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null
    ): StoriesResponse

    @Multipart
    @POST("stories")
    suspend fun postStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") latitude: RequestBody?,
        @Part("lon") longitude: RequestBody?
    ): AddStoryResponse

    @GET("stories?location=1")
    suspend fun getAllStoriesLocation(
        @Header("Authorization") token: String
    ): StoriesResponse

}