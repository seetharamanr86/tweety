package eu.micer.tweety.feature.tweetlist.model

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.feature.tweetlist.model.database.TweetDao
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.network.model.Tweet
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TweetRepository(private val twitterApi: TwitterApi, private val tweetDao: TweetDao) {
    var receiveData = false

    fun getTweetsFlowable(track: String): Flowable<TweetEntity> {
        receiveData = true
        return twitterApi.getTweetsStream(track)
            .flatMapObservable { responseBody ->
                Observable.create<Tweet> { emitter ->
                    JsonReader(responseBody.charStream())
                        .also { it.isLenient = true }
                        .use { reader ->
                            while (receiveData && reader.hasNext()) {
                                emitter.onNext(Gson().fromJson<Tweet>(reader, Tweet::class.java))
                            }
                            emitter.onComplete()
                            Timber.d("Receiving tweets has been completed.")
                        }
                }
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .map { tweet: Tweet ->
                TweetEntity(
                    tweetId = tweet.id,
                    text = tweet.text,
                    createdAt = tweet.createdAt,
                    user = tweet.user.name,
                    timestamp = tweet.timestampMs
                )
            }
            .onErrorResumeNext(Function {
                // return tweets from local database in case of any error
                tweetDao.getAll()
                    .toFlowable()
            })
            .doOnNext(tweetDao::insert) // insert every new tweet into database
            /**
             * @ImplNote: requestOn = false in subscribeOn() is needed to avoid deadlock in emitter,
             * see https://stackoverflow.com/a/44921023/1101730
             */
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun clearOfflineData(): Maybe<Int> {
        return Maybe.fromAction(tweetDao::deleteAll)
    }
}