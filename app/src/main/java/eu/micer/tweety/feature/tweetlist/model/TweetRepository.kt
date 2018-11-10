package eu.micer.tweety.feature.tweetlist.model

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.feature.tweetlist.model.database.TweetDao
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.network.model.Tweet
import eu.micer.tweety.util.extensions.runInBackground
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.Action
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.toFlowable
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
            .map { tweet: Tweet? ->
                TweetEntity(
                    tweetId = tweet?.id ?: 0,
                    text = tweet?.text ?: "",
                    createdAt = tweet?.createdAt ?: "",
                    user = tweet?.user?.name ?: ""
                )
            }
            .onErrorResumeNext(Function {
                // return tweets from local database in case of any error
                tweetDao.getAllSync().toFlowable()
            })
            .doOnNext(tweetDao::insert) // insert every new tweet into database
            .runInBackground()
    }

    fun getOfflineTweetsFlowable(): Flowable<List<TweetEntity>> {
        return tweetDao.getAll()
    }

    fun clearOfflineData(): Maybe<Void> {
        return Maybe.fromAction(tweetDao::deleteAll)
    }

    fun removeExpiredTweets(timestampMin: Long) : Maybe<Void>{
        return Maybe.fromAction(Action(fun() {
            tweetDao.deleteExpired(timestampMin)
        }))
    }
}