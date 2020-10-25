package eu.micer.tweety.presentation.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import eu.micer.tweety.R
import eu.micer.tweety.databinding.ActivityMainBinding
import eu.micer.tweety.presentation.base.BaseActivity
import eu.micer.tweety.presentation.base.BaseViewModel
import eu.micer.tweety.presentation.intent.TweetListIntent
import eu.micer.tweety.presentation.ui.TweetAdapter
import eu.micer.tweety.presentation.util.UserPreference
import eu.micer.tweety.presentation.util.extensions.hideKeyboard
import eu.micer.tweety.presentation.util.extensions.toEditable
import eu.micer.tweety.presentation.viewstate.TweetListViewState
import eu.micer.tweety.presentation.vm.TweetListViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<TweetListIntent, TweetListViewState>() {

    private val tweetListViewModel: TweetListViewModel by viewModel()
    private var isReceivingTweets: Boolean = false  // TODO do not save here
    private lateinit var binding: ActivityMainBinding
    private lateinit var tweetAdapter: TweetAdapter

    // TODO use RxBinding instead
    private val startTrackingIntentPublisher =
        PublishSubject.create<TweetListIntent.StartTrackingIntent>()
    private val stopTrackingIntentPublisher =
        PublishSubject.create<TweetListIntent.StopTrackingIntent>()
    private val clearOfflineTweetsIntentPublisher =
        PublishSubject.create<TweetListIntent.ClearOfflineTweetsIntent>()

    override fun getViewModel(): BaseViewModel<TweetListIntent, TweetListViewState> =
        tweetListViewModel

    override fun intents(): Observable<TweetListIntent> =
        Observable.merge(
            initialIntent(),
            startTrackingIntent(),
            stopTrackingIntent(),
            clearOfflineTweetsIntent()
        )

    private fun initialIntent(): Observable<TweetListIntent.InitialIntent> =
        Observable.just(TweetListIntent.InitialIntent)

    private fun startTrackingIntent(): Observable<TweetListIntent.StartTrackingIntent> =
        startTrackingIntentPublisher

    private fun stopTrackingIntent(): Observable<TweetListIntent.StopTrackingIntent> =
        stopTrackingIntentPublisher

    private fun clearOfflineTweetsIntent(): Observable<TweetListIntent.ClearOfflineTweetsIntent> =
        clearOfflineTweetsIntentPublisher

    override fun render(state: TweetListViewState) {
        with (state.isReceivingTweets) {
            isReceivingTweets = this
            if (this) {
                binding.animStartStop.showSecond()
            } else {
                binding.animStartStop.showFirst()
            }
            binding.etSearchText.isEnabled = !this
        }

        with (state.tweetList) {
            val newItems = tweetListViewModel.removeExpiredItemsFromList(this)
            tweetAdapter.updateItems(newItems)
        }

        if (state.errorMessage.isNotBlank()) {
            Toast.makeText(this, state.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun bind() {
        getViewModel().states()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::render)
            .addTo(disposables)

        getViewModel().processIntents(intents())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        bind()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_all -> {
                clearOfflineTweetsIntentPublisher.onNext(TweetListIntent.ClearOfflineTweetsIntent)
                tweetAdapter.clearItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        stopTrackingIntentPublisher.onNext(TweetListIntent.StopTrackingIntent)
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
            if (!isReceivingTweets) {
                if (etSearchText.text.isNotEmpty()) {
                    view.hideKeyboard()
                    animStartStop.morph()
                    val searchText = getSearchText()

                    // save search text to Shared Prefs
                    UserPreference.lastSearchText = searchText

                    startTrackingIntentPublisher.onNext(
                        TweetListIntent.StartTrackingIntent(
                            getSearchText()
                        )
                    )
                }
            } else {
                animStartStop.morph()
                stopTrackingIntentPublisher.onNext(TweetListIntent.StopTrackingIntent)
            }
        }
    }

    private fun getSearchText(): String {
        return binding.etSearchText.text.toString().trim()
    }
}
