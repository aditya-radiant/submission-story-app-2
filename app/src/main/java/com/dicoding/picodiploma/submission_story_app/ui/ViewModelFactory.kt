package com.dicoding.picodiploma.submission_story_app.ui


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.submission_story_app.data.di.Injection
import com.dicoding.picodiploma.submission_story_app.data.repository.StoryRepository
import com.dicoding.picodiploma.submission_story_app.data.repository.UserRepository
import com.dicoding.picodiploma.submission_story_app.ui.location.LocationViewModel
import com.dicoding.picodiploma.submission_story_app.ui.login.LoginViewModel
import com.dicoding.picodiploma.submission_story_app.ui.postStory.PostStoryViewModel
import com.dicoding.picodiploma.submission_story_app.ui.signup.SignUpViewModel
import com.dicoding.picodiploma.submission_story_app.ui.story.StoryViewModel

class ViewModelFactory(private val storyRepository: StoryRepository, private val userRepository: UserRepository) : ViewModelProvider.NewInstanceFactory()
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(PostStoryViewModel::class.java) -> {
                PostStoryViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(userRepository) as T
            }modelClass.isAssignableFrom(LocationViewModel::class.java) -> {
                LocationViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(Injection.provideStoryRepository(context), Injection.provideUserRepository())
            }.also { instance = it }
    }

}
