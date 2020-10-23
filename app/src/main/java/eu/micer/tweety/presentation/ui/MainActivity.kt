package eu.micer.tweety.presentation.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import eu.micer.tweety.R
import eu.micer.tweety.presentation.base.BaseActivity
import eu.micer.tweety.presentation.base.BaseViewModel
import eu.micer.tweety.presentation.util.UserPreference
import eu.micer.tweety.presentation.util.extensions.hideKeyboard
import eu.micer.tweety.presentation.util.extensions.toEditable
import eu.micer.tweety.presentation.vm.TweetListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val tweetListViewModel: TweetListViewModel by viewModel()
    private lateinit var tweetAdapter: TweetAdapter

    override fun getViewModel(): BaseViewModel {
        return tweetListViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()

        // setup LiveData observers
        tweetListViewModel.isReceivingData().observe(this, { isReceiving ->
            // needed for cases when ie. receiving, putting app to background and back
            isReceiving?.let {
                if (it) anim_start_stop.showSecond()
                else anim_start_stop.showFirst()
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
        rv_tweet_list.layoutManager = LinearLayoutManager(this)

        // init adapter with empty data
        tweetAdapter = TweetAdapter(ArrayList(), this)

        rv_tweet_list.adapter = tweetAdapter

        // retrieve last search text
        et_search_text.text = UserPreference.lastSearchText.toEditable()

        // Start / Stop button
        anim_start_stop.setOnClickListener(this::onButtonStartStopClick)
    }

    private fun onButtonStartStopClick(view: View) {
        if (tweetListViewModel.isReceivingData().value == true) {
            // stop receiving tweets
            tweetListViewModel.stopReceivingData()
            anim_start_stop.morph()
        } else {
            // start receiving tweets
            if (et_search_text.text.isNotEmpty()) {
                view.hideKeyboard()
                anim_start_stop.morph()
                val searchText = getSearchText()

                // save search text to Shared Prefs
                UserPreference.lastSearchText = searchText

                tweetListViewModel.loadTweetsLiveData(searchText)
            }
        }
    }

    private fun getSearchText(): String {
        return et_search_text.text.toString().trim()
    }
}
