package com.dicoding.picodiploma.submission_story_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.submission_story_app.data.di.Injection
import com.dicoding.picodiploma.submission_story_app.data.repository.UserRepository
import com.dicoding.picodiploma.submission_story_app.model.LoginModel
import com.dicoding.picodiploma.submission_story_app.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository, private val loginPreferences: UserPreferences): ViewModel() {

    fun login(email: String, pass: String) =
        userRepository.login(email, pass)

    fun checkSession(): Flow<LoginModel> {
        return loginPreferences.isFirstTime()
    }

    fun logout() {
        viewModelScope.launch {
            loginPreferences.logout()
        }
    }

    class LoginViewModelFactory private constructor(
        private val userRepository: UserRepository,
        private val loginPreferences: UserPreferences
    ) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(userRepository, loginPreferences) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: LoginViewModelFactory? = null
            fun getInstance(
                loginPreferences: UserPreferences
            ): LoginViewModelFactory =
                instance ?: synchronized(this) {
                    instance ?: LoginViewModelFactory(
                        Injection.provideUserRepository(),
                        loginPreferences
                    )
                }
        }
    }
}



