package com.dicoding.picodiploma.submission_story_app.ui.signup

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.submission_story_app.data.repository.UserRepository

class SignUpViewModel(private val storyRepository: UserRepository): ViewModel() {
    fun register(name: String, email: String, pass: String) =
        storyRepository.register(name,email, pass)

}