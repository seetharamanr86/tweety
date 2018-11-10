package eu.micer.tweety.feature.tweetlist.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber.e
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.feature.tweetlist.model.TweetRepository
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.util.Constants
import eu.micer.tweety.util.event.Event1
import eu.micer.tweety.util.extensions.default
import eu.micer.tweety.util.extensions.runInBackground
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber.d
import java.util.concurrent.TimeUnit


class TweetListViewModel(private val tweetRepository: TweetRepository) : BaseViewModel() {
    private val tweetListLiveData = MutableLiveData<ArrayList<TweetEntity>>().default(ArrayList())
    private val isReceivingDataMutableLiveData = MutableLiveData<Boolean>().default(false)

    val tweetList: LiveData<ArrayList<TweetEntity>>
        get() = tweetListLiveData

    fun isReceivingData() = isReceivingDataMutableLiveData

    fun receiveTweets(track: String) {
        tweetRepository.getTweetsFlowable(track)
            .doOnSubscribe {
                isReceivingDataMutableLiveData.postValue(true)
            }
            .subscribe({ tweetEntity: TweetEntity ->
                if (tweetRepository.receiveData) {
                    d("tweet created at: ${tweetEntity.createdAt}")
                    addNewTweet(tweetEntity)
                }
            }, { t: Throwable ->
                t.message?.let {
                    showErrorEvent.value = Event1(it)
                }
                e(t)
            })
            .addTo(compositeDisposable)
    }

    fun stopReceivingData() {
        tweetRepository.receiveData = false
        isReceivingDataMutableLiveData.postValue(false)
    }

    fun clearAll() {
        clearTweetListLiveData()
        tweetRepository.clearOfflineData()
            .runInBackground()
            .subscribe({
                d("database rows cleared")
            }, {
                e(it)
            }).addTo(compositeDisposable)
    }

    fun startClearingTask() {
        Observable.interval(Constants.Tweet.CLEARING_PERIOD_SECONDS, TimeUnit.SECONDS, Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
                d("trying to remove expired tweets")
                removeExpiredTweets()
            }, {
                e(it)
            })
            .addTo(compositeDisposable)
    }

    fun loadLastData() {
        tweetRepository.getOfflineTweetsFlowable()
            .runInBackground()
            .subscribe({
                it.forEach(this::addNewTweet)
            }, {
                e(it)
            })
            .addTo(compositeDisposable)
    }

    private fun addNewTweet(tweetEntity: TweetEntity) {
        val list = tweetListLiveData.value ?: ArrayList()
        if (tweetEntity !in list) {
            list.add(tweetEntity)
            tweetListLiveData.value = list
        }
    }

    private fun clearTweetListLiveData() {
        tweetListLiveData.value = ArrayList()
    }

    private fun removeExpiredTweets() {
        val timestampMin = getMinimalTimestamp()
        // remove from database
        tweetRepository.removeExpiredTweets(timestampMin)
            .runInBackground()
            .subscribe({
                d("expired tweets removed")
            }, {
                e(it)
            }).addTo(compositeDisposable)

        // remove from LiveData
        var list = tweetListLiveData.value ?: ArrayList()
        list = list.filter { tweetEntity ->
            tweetEntity.timestamp > timestampMin
        } as ArrayList<TweetEntity>
        tweetListLiveData.value = list
    }

    private fun getMinimalTimestamp(): Long {
        val lifespanMs = TimeUnit.SECONDS.toMillis(Constants.Tweet.LIFESPAN_SECONDS)
        return System.currentTimeMillis() - lifespanMs
    }
}
