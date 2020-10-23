package eu.micer.tweety.presentation.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.ajalt.timberkt.Timber.e
import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.domain.repository.TweetRepository
import eu.micer.tweety.presentation.base.BaseViewModel
import eu.micer.tweety.presentation.util.Constants
import eu.micer.tweety.presentation.util.extensions.runAllOnIoThread
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber.d
import java.util.concurrent.TimeUnit

class TweetListViewModel(private val tweetRepository: TweetRepository) : BaseViewModel() {

    // Solution with Transformations.switchMap as presented in https://youtu.be/2rO4r-JOQtA.
    // We should always use Transformations and not passing the reference to one LiveData object to prevent having more
    // observers to same data - which we don't want in this particular case.

    // String to track, taken from user input
    private val track = MutableLiveData<String>()

    val tweetsLiveData: LiveData<List<Tweet>> = Transformations.switchMap(track) { track ->
        tweetRepository.getTweetsLiveData(track, showErrorEvent)
    }

    fun loadTweetsLiveData(userInput: String) {
        track.value = userInput
    }

    fun isReceivingData(): MutableLiveData<Boolean> {
        return tweetRepository.receiveRemoteData
    }

    fun stopReceivingData() {
        tweetRepository.receiveRemoteData.postValue(false)
    }

    fun clearOfflineTweets() {
        tweetRepository.removeAllTweets()
            .runAllOnIoThread()
            .subscribe({
                d("database rows cleared")
            }, {
                e(it)
            }).addTo(compositeDisposable)
    }

    fun startClearingTask() {
        Observable.interval(Constants.Tweet.CLEARING_PERIOD_SECONDS, TimeUnit.SECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .startWith(0L)
            .subscribe({
                removeExpiredTweets()
            }, {
                e(it)
            })
            .addTo(compositeDisposable)
    }

    /**
     * This is probably wrong as we shouldn't return the reference to LiveData and use Transformations to rather create
     * a new instance for the view. Source: https://youtu.be/2rO4r-JOQtA.
     */
    fun getOfflineTweetsLiveData(): LiveData<List<Tweet>> {
        return tweetRepository.getOfflineTweetsLiveData()
    }

    fun removeExpiredItemsFromList(list: List<Tweet>): List<Tweet> {
        return list.toMutableList().filter {
            it.timestamp > getMinimalTimestamp()
        }
    }

    private fun removeExpiredTweets() {
        d("trying to remove expired tweets")
        val timestampMin = getMinimalTimestamp()
        // remove from database
        tweetRepository.removeExpiredTweets(timestampMin)
            .runAllOnIoThread()
            .ignoreElement()
            .doOnError { e(it) }
            .subscribe()
            .addTo(compositeDisposable)
    }

    private fun getMinimalTimestamp(): Long {
        val lifespanMs = TimeUnit.SECONDS.toMillis(Constants.Tweet.LIFESPAN_SECONDS)
        return System.currentTimeMillis() - lifespanMs
    }
}
