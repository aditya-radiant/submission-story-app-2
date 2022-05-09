package com.dicoding.picodiploma.submission_story_app.data.repository



import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.picodiploma.submission_story_app.data.api.ApiService
import com.dicoding.picodiploma.submission_story_app.data.helper.Result
import com.dicoding.picodiploma.submission_story_app.data.response.LoginResult
import com.dicoding.picodiploma.submission_story_app.data.response.RegisterResponse


class UserRepository private constructor(
    private val apiService: ApiService
){
    fun register(name: String, email: String, pass: String): LiveData<Result<RegisterResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, pass)
                if (!response.error) {
                    emit(Result.Success(response))
                } else {
                    Log.e(TAG, "Register Fail: ${response.message}")
                    emit(Result.Error(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register Exception: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }

    fun login(email: String, pass: String): LiveData<Result<LoginResult>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.login(email, pass)
                if (!response.error) {
                    emit(Result.Success(response.loginResult))
                } else {
                    Log.e(TAG, "Register Fail: ${response.message}")
                    emit(Result.Error(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register Exception: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }

    companion object {
        private val TAG = UserRepository::class.java.simpleName

        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(apiService: ApiService) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService)
            }.also { instance = it }
    }
}