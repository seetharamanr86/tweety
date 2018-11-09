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
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber.d


class TweetListViewModel(private val api: TwitterApi) : BaseViewModel() {
    private val tweetListLiveData = MutableLiveData<ArrayList<Tweet>>().default(ArrayList())
    private var stopReading = false

    val tweetList: LiveData<ArrayList<Tweet>>
        get() = tweetListLiveData

    /**
     * @ImplNote: requestOn = false in subscribeOn() is needed to avoid deadlock in emitter,
     * see https://stackoverflow.com/a/44921023/1101730
     */
    fun getTweets(track: String) {
        api.getTweetsStream(track)
            .flatMapObservable { responseBody ->
                Observable.create<Tweet> { emitter ->
                    JsonReader(responseBody.charStream())
                        .also { it.isLenient = true }
                        .use { reader ->
                            stopReading = false
                            while (!stopReading && reader.hasNext()) {
                                emitter.onNext(Gson().fromJson<Tweet>(reader, Tweet::class.java))
                            }
                            emitter.onComplete()
                            d("emitter on complete")
                        }
                }
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ tweet: Tweet ->
                d("tweet created at: ${tweet.createdAt}")
                addNewTweet(tweet)
            }, { t: Throwable ->
                // TODO java.net.SocketTimeoutException: timeout -> retry???
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
