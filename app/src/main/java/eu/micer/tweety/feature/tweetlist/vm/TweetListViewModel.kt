package eu.micer.tweety.feature.tweetlist.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber.e
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.feature.tweetlist.model.TweetRepository
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.util.event.Event1
import eu.micer.tweety.util.extensions.default
import io.reactivex.rxkotlin.addTo
import timber.log.Timber.d


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
                    saveNewTweet(tweetEntity)
                }
            }, { t: Throwable ->
                isReceivingDataMutableLiveData.postValue(tweetRepository.receiveData)
                t.message?.let {
                    showErrorEvent.value = Event1(it)
                }
                e(t)
            })
            .addTo(compositeDisposable)
    }

    private fun saveNewTweet(tweetEntity: TweetEntity) {
        val list = tweetListLiveData.value
        list?.add(tweetEntity)
        tweetListLiveData.value = list
    }

    fun stopReceivingData() {
        tweetRepository.receiveData = false
    }
}
