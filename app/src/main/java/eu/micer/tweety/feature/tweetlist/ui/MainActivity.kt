package eu.micer.tweety.feature.tweetlist.ui

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
                rv_tweet_list.scrollToPosition(0)
            }
        })

        tweetListViewModel.isReceivingData().observe(this, Observer { isReceiving ->
            isReceiving?.let {
                btn_start_stop.text = getString(if (it) R.string.stop else R.string.track)
            }
        })
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
                    tweetListViewModel.getTweets(et_search_text.text.toString())
                }
            } else {
                // stop receiving tweets
                tweetListViewModel.stopReceivingData()
                btn_start_stop.text = getString(R.string.track)
            }
        }
    }
}
