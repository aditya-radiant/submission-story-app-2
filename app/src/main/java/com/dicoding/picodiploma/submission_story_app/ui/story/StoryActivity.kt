package com.dicoding.picodiploma.submission_story_app.ui.story

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.submission_story_app.R
import com.dicoding.picodiploma.submission_story_app.data.response.ListStoryItem
import com.dicoding.picodiploma.submission_story_app.databinding.ActivityStoryBinding
import com.dicoding.picodiploma.submission_story_app.model.LoginModel
import com.dicoding.picodiploma.submission_story_app.model.UserPreferences
import com.dicoding.picodiploma.submission_story_app.ui.ViewModelFactory
import com.dicoding.picodiploma.submission_story_app.ui.adapter.LoadingStateAdapter
import com.dicoding.picodiploma.submission_story_app.ui.adapter.StoryAdapter
import com.dicoding.picodiploma.submission_story_app.ui.detail.DetailStoryActivity
import com.dicoding.picodiploma.submission_story_app.ui.location.LocationActivity
import com.dicoding.picodiploma.submission_story_app.ui.login.LoginActivity
import com.dicoding.picodiploma.submission_story_app.ui.login.LoginViewModel
import com.dicoding.picodiploma.submission_story_app.ui.postStory.PostStoryActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
class StoryActivity : AppCompatActivity() {
    private lateinit var adapter: StoryAdapter
    private lateinit var login: LoginModel

    private var binding: ActivityStoryBinding? = null
    private var isFromOtherScreen = false

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.LoginViewModelFactory.getInstance(
            UserPreferences.getInstance(dataStore)
        )
    }

    companion object {
        const val USER_DATA= "user"
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.title = getString(R.string.story)

        adapter = StoryAdapter()
        adapter.notifyDataSetChanged()
        login = intent.getParcelableExtra(USER_DATA)!!

        setupView()

        adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem) {
                Intent(this@StoryActivity, DetailStoryActivity::class.java).also {
                    it.putExtra(DetailStoryActivity.EXTRA_STORY, data)
                    startActivity(it)
                }
            }
        })

        binding?.addStory?.setOnClickListener{
            val intent = Intent(this, PostStoryActivity::class.java)
            intent.putExtra(PostStoryActivity.EXTRA_DATA, login)
            startActivity(intent)
        }


    }

    private fun setupView(){
        adapter = StoryAdapter().apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0 && isFromOtherScreen.not()) {
                        binding?.rvUserStory?.smoothScrollToPosition(0)
                    }
                }
            })
        }

        binding?.apply {
            rvUserStory.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter { adapter.retry() }
            )
            rvUserStory.setHasFixedSize(true)
            rvUserStory.layoutManager = LinearLayoutManager(this@StoryActivity)

            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collect{
                    swipeLayout.isRefreshing = it.mediator?.refresh is LoadState.Loading
                }
            }

            lifecycleScope.launch {
                adapter.loadStateFlow.collectLatest {loadStates ->
                    emptyRv.isVisible = loadStates.refresh is LoadState.Error
                }
                if (adapter.itemCount < 1) emptyRv.visibility = View.VISIBLE
                else emptyRv.visibility = View.GONE
            }

            swipeLayout.setOnRefreshListener {
                adapter.refresh()
                swipeLayout.isRefreshing = false
            }

        }

        storyViewModel.getStory(login.token).observe(this){
            adapter.submitData(lifecycle, it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bar, menu)
        var favButton = menu.findItem(R.id.map)
        favButton.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }else if (item.itemId == R.id.logout) {
            loginViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else if(item.itemId == R.id.map){
            val intent = Intent(this, LocationActivity::class.java)
            intent.putExtra(PostStoryActivity.EXTRA_DATA, login)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}