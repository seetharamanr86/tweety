package eu.micer.tweety.feature.tweetlist.ui

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import eu.micer.tweety.R
import eu.micer.tweety.base.BaseActivity
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.feature.tweetlist.ui.adapter.TweetAdapter
import eu.micer.tweety.feature.tweetlist.vm.TweetListViewModel
import eu.micer.tweety.util.extensions.hideKeyboard
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.architecture.ext.viewModel

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
        tweetListViewModel.tweetList.observe(this, Observer {
            it?.let { list ->
                tweetAdapter.updateItems(list)
            }
        })

        tweetListViewModel.isReceivingData().observe(this, Observer { isReceiving ->
            isReceiving?.let {
                btn_start_stop.text = getString(if (it) R.string.stop else R.string.track)
            }
        })

        tweetListViewModel.startClearingTask()
        tweetListViewModel.loadLastData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.clear_all -> {
                tweetListViewModel.clearAll()
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

        tweetListViewModel.tweetList.value?.let {
            tweetAdapter = TweetAdapter(it, this)
        }

        rv_tweet_list.adapter = tweetAdapter

        btn_start_stop.setOnClickListener { view ->
            if (btn_start_stop.text == getString(R.string.track)) {
                // start receiving tweets
                if (et_search_text.text.isNotEmpty()) {
                    view.hideKeyboard()
                    btn_start_stop.text = getString(R.string.stop)
                    tweetListViewModel.receiveTweets(et_search_text.text.toString().trim())
                }
            } else {
                // stop receiving tweets
                tweetListViewModel.stopReceivingData()
                btn_start_stop.text = getString(R.string.track)
            }
        }
    }
}
