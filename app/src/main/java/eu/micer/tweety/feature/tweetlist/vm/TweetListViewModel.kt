package eu.micer.tweety.feature.tweetlist.vm

import com.github.ajalt.timberkt.Timber.e
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.network.model.Tweet
import eu.micer.tweety.util.event.Event1
import eu.micer.tweety.util.extensions.subscribeObserveInBackground
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo


class TweetListViewModel(private val api: TwitterApi) : BaseViewModel() {

    fun getTweets() {
        api.getTweetsStream("trump")
            .subscribeObserveInBackground()
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
            .subscribe({ tweet: Tweet ->
                println("tweet created at: ${tweet.createdAt}")
            }, { t: Throwable ->
                t.message?.let {
                    showErrorEvent.value = Event1(it)
                }
                e(t)
            })
            .addTo(compositeDisposable)
    }
}
