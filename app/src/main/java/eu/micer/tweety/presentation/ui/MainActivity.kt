package eu.micer.tweety.presentation.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import eu.micer.tweety.R
import eu.micer.tweety.databinding.ActivityMainBinding
import eu.micer.tweety.presentation.base.BaseActivity
import eu.micer.tweety.presentation.base.BaseViewModel
import eu.micer.tweety.presentation.util.UserPreference
import eu.micer.tweety.presentation.util.extensions.hideKeyboard
import eu.micer.tweety.presentation.util.extensions.toEditable
import eu.micer.tweety.presentation.vm.TweetListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val tweetListViewModel: TweetListViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var tweetAdapter: TweetAdapter

    override fun getViewModel(): BaseViewModel {
        return tweetListViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        // setup LiveData observers
        tweetListViewModel.isReceivingData().observe(this, { isReceiving ->
            // needed for cases when ie. receiving, putting app to background and back
            isReceiving?.let {
                if (it) binding.animStartStop.showSecond()
                else binding.animStartStop.showFirst()
            }
        })

        tweetListViewModel.tweetsLiveData
            .observe(this, {
                it?.let { list ->
                    val newItems = tweetListViewModel.removeExpiredItemsFromList(list)
                    tweetAdapter.updateItems(newItems)
                }
            })

        tweetListViewModel.getOfflineTweetsLiveData().observe(this, {
            it?.let { list ->
                tweetAdapter.updateItems(list)
                // no need to keep observing, one-time only
                tweetListViewModel.getOfflineTweetsLiveData().removeObservers(this)
            }
        })

        tweetListViewModel.startClearingTask()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_all -> {
                tweetListViewModel.clearOfflineTweets()
                tweetAdapter.clearItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        tweetListViewModel.stopReceivingData()
    }

    private fun setupViews() {
        with(binding) {
            rvTweetList.layoutManager = LinearLayoutManager(this@MainActivity)

            // init adapter with empty data
            tweetAdapter = TweetAdapter(ArrayList())

            rvTweetList.adapter = tweetAdapter

            // retrieve last search text
            etSearchText.text = UserPreference.lastSearchText.toEditable()

            // Start / Stop button
            animStartStop.setOnClickListener(this@MainActivity::onButtonStartStopClick)
        }
    }

    private fun onButtonStartStopClick(view: View) {
        with(binding) {
            if (tweetListViewModel.isReceivingData().value == true) {
                // stop receiving tweets
                tweetListViewModel.stopReceivingData()
                animStartStop.morph()
            } else {
                // start receiving tweets
                if (etSearchText.text.isNotEmpty()) {
                    view.hideKeyboard()
                    animStartStop.morph()
                    val searchText = getSearchText()

                    // save search text to Shared Prefs
                    UserPreference.lastSearchText = searchText

                    tweetListViewModel.loadTweetsLiveData(searchText)
                }
            }
        }
    }

    private fun getSearchText(): String {
        return binding.etSearchText.text.toString().trim()
    }
}
