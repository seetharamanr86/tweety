package eu.micer.tweety.feature.tweetlist.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber.e
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.network.model.Tweet
import eu.micer.tweety.util.event.Event1
import eu.micer.tweety.util.extensions.default
import eu.micer.tweety.util.extensions.subscribeBackgroundObserveMain
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo


class TweetListViewModel(private val api: TwitterApi) : BaseViewModel() {
    private val tweetListLiveData = MutableLiveData<ArrayList<Tweet>>().default(ArrayList())

    val tweetList: LiveData<ArrayList<Tweet>>
        get() = tweetListLiveData

    fun getTweets() {
        api.getTweetsStream("apple")
            .flatMapObservable { responseBody ->
                Observable.create<Tweet> { emitter ->
                    JsonReader(responseBody.charStream())
                        .also { it.isLenient = true }
                        .use { reader ->
                            while (reader.hasNext()) {
                                emitter.onNext(Gson().fromJson<Tweet>(reader, Tweet::class.java))
                            }
                            emitter.onComplete()
                        }
                }
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeBackgroundObserveMain()
            .subscribe({ tweet: Tweet ->
                println("tweet created at: ${tweet.createdAt}")
                addNewTweet(tweet)
            }, { t: Throwable ->
                t.message?.let {
                    showErrorEvent.value = Event1(it)
                }
                e(t)
            })
            .addTo(compositeDisposable)
    }

    private fun addNewTweet(tweet: Tweet) {
        val list = tweetListLiveData.value
        list?.add(tweet)
        tweetListLiveData.value = list
    }
}
