package eu.micer.tweety.domain.repository

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.data.local.dao.TweetDao
import eu.micer.tweety.data.local.entity.TweetEntity
import eu.micer.tweety.data.local.entity.mapToDomainModel
import eu.micer.tweety.data.remote.api.TwitterApi
import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.util.extensions.runAllOnIoThread
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.Action
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import timber.log.Timber.d
import timber.log.Timber.e
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import eu.micer.tweety.data.remote.model.Tweet as TweetRemote

class TweetRepository(private val twitterApi: TwitterApi, private val tweetDao: TweetDao) {
    private var tweetEntityList: List<TweetEntity> = ArrayList()
    private val requestRepeatDelay = 3L
    var receiveRemoteData = false

    // TODO this all should be in TweetListActionProcessorHolder!
    fun getTweetsObservable(
        track: String
    ): Flowable<List<Tweet>> {
        receiveRemoteData = true
        return twitterApi.getTweetsStream(track)
            .subscribeOn(Schedulers.io())
            .flatMapObservable { responseBody ->
                createJsonReaderObservable(responseBody)
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .runAllOnIoThread()
            .map { tweet: TweetRemote? ->
                TweetEntity(
                    tweetId = tweet?.id ?: 0,
                    text = tweet?.text ?: "",
                    createdAt = getDateFromString(tweet?.createdAt ?: ""),
                    user = tweet?.user?.name ?: ""
                )
            }
            .filter { tweetEntity: TweetEntity ->
                // don't show tweets with empty text
                tweetEntity.text != ""
            }
            .doOnNext(tweetDao::insert) // insert every new tweet into database
            .flatMap { tweetEntity ->
                (tweetEntityList as ArrayList).add(tweetEntity)
                Flowable.just(tweetEntityList)
            }
            .doOnError {
                e(it)
                val message =
                    "Error when fetching data:\n\n${it.message}\n\nNext try in $requestRepeatDelay seconds."
//                showErrorEvent.postValue(Event1(message))
                // TODO show error message
            }
            .onErrorResumeNext(Function {
                /**
                 * Return tweets from local database in case of any error. When using Flowable in Room db DAO, it keeps
                 * listening to changes and never emits onComplete(). Therefore I'm using Flowable.just(...).
                 */
                d("Using data from local database.")
                Flowable.just(tweetDao.findAllSync())
            })
            .doOnComplete {
                d("Receiving tweets has been completed.")
            }
            .repeatWhen {
                it.delay(requestRepeatDelay, TimeUnit.SECONDS)
            }
            .takeUntil {
                !receiveRemoteData
            }
            .map { it.mapToDomainModel() }
    }

    /**
     * Loads data from database.
     */
    fun getOfflineData(): Maybe<List<Tweet>> {
        return tweetDao.findAll().map {
            it.mapToDomainModel()
        }
    }

    /**
     * Removes all data from database.
     */
    fun removeAllTweets(): Maybe<Void> {
        (tweetEntityList as ArrayList).clear()
        return Maybe.fromAction(tweetDao::deleteAll)
    }

    /**
     * Removes expired data from database.
     */
    fun removeExpiredTweets(timestampMin: Long): Maybe<Void> {
        return Maybe.fromAction(Action(fun() {
            tweetDao.deleteExpired(timestampMin)
        }))
    }

    /**
     * Provides JSON reader to parse incoming stream of data and emit Tweet objects.
     */
    private fun createJsonReaderObservable(responseBody: ResponseBody): Observable<TweetRemote>? {
        return Observable.create { emitter ->
            JsonReader(responseBody.charStream())
                .also { it.isLenient = true }
                .use { reader ->
                    while (receiveRemoteData && reader.hasNext()) {
                        emitter.onNext(Gson().fromJson(reader, TweetRemote::class.java))
                    }
                    emitter.onComplete()
                }
        }
    }

    private fun getDateFromString(datetime: String): Date? {
        return try {
            SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault()).parse(
                datetime
            )
        } catch (parseException: ParseException) {
            e("Could not parse Tweet createdBy: ${parseException.message}")
            null
        }
    }
}
