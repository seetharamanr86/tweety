package eu.micer.tweety.feature.tweetlist.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber.e
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.feature.tweetlist.model.TweetRepository
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.util.Constants
import eu.micer.tweety.util.extensions.runAllOnIoThread
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber.d
import java.util.concurrent.TimeUnit


class TweetListViewModel(private val tweetRepository: TweetRepository) : BaseViewModel() {

    fun getTweetsLiveData(track: String): LiveData<List<TweetEntity>> {
        return tweetRepository.getTweetsLiveData(track, showErrorEvent)
    }

    fun isReceivingData(): MutableLiveData<Boolean> {
        return tweetRepository.receiveData
    }

    fun stopReceivingData() {
        tweetRepository.receiveData.postValue(false)
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

    fun getOfflineTweetsLiveData(): LiveData<List<TweetEntity>> {
        return tweetRepository.getOfflineTweetsLiveData()
    }

    fun removeExpiredItemsFromList(list: List<TweetEntity>): List<TweetEntity> {
        return list.filter { it.timestamp > getMinimalTimestamp() }
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
