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
import eu.micer.tweety.util.extensions.runAllOnIoThread
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
        var cnt = 0     // TODO remove, debug only
        tweetRepository.getTweetsFlowable(track)
            .doOnSubscribe {
                isReceivingDataMutableLiveData.postValue(true)
            }
            .subscribe({ tweetEntity: TweetEntity ->
                if (tweetRepository.receiveData) {
                    d("tweet cnt: ${++cnt}")
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
            .subscribe({
                d("trying to remove expired tweets")
                removeExpiredTweets()
            }, {
                e(it)
            })
            .addTo(compositeDisposable)
    }

    fun loadLastData() {
        tweetRepository.getOfflineTweetsFlowable()
            .runAllOnIoThread()
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
            tweetListLiveData.postValue(list)
        }
    }

    private fun clearTweetListLiveData() {
        tweetListLiveData.value = ArrayList()
    }

    private fun removeExpiredTweets() {
        val timestampMin = getMinimalTimestamp()
        // remove from database
        tweetRepository.removeExpiredTweets(timestampMin)
            .runAllOnIoThread()
            .ignoreElement()
            .doOnError { e(it) }
            .subscribe()
            .addTo(compositeDisposable)

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
