package eu.micer.tweety.feature.tweetlist.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.feature.tweetlist.model.database.TweetDao
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.network.model.Tweet
import eu.micer.tweety.util.event.Event1
import eu.micer.tweety.util.extensions.default
import eu.micer.tweety.util.extensions.runAllOnIoThread
import eu.micer.tweety.util.extensions.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.Action
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import timber.log.Timber
import timber.log.Timber.d
import timber.log.Timber.e
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TweetRepository(private val twitterApi: TwitterApi, private val tweetDao: TweetDao) {
    private var tweetEntityList: List<TweetEntity> = ArrayList()
    val receiveRemoteData = MutableLiveData<Boolean>().default(false)

    fun getTweetsLiveData(track: String, showErrorEvent: MutableLiveData<Event1<String>>): LiveData<List<TweetEntity>> {
        receiveRemoteData.value = true
        return twitterApi.getTweetsStream(track)
            .subscribeOn(Schedulers.io())
            .flatMapObservable { responseBody ->
                createJsonReaderObservable(responseBody)
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .runAllOnIoThread()
            .map { tweet: Tweet? ->
                TweetEntity(
                    tweetId = tweet?.id ?: 0,
                    text = tweet?.text ?: "",
                    createdAt = getDateFromString(tweet?.createdAt),
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
                Timber.e(it)
                it.message?.let { msg ->
                    showErrorEvent.postValue(Event1(msg))
                }
            }
            .onErrorResumeNext(Function {
                receiveRemoteData.postValue(false)
                // return tweets from local database in case of any error
                d("using local database")
                tweetDao.findAll()
            })
            .doOnComplete {
                d("Receiving tweets has been completed.")
            }
            .toLiveData()
    }

    /**
     * Loads data from database.
     */
    fun getOfflineTweetsLiveData(): LiveData<List<TweetEntity>> {
        return tweetDao.findAllLiveData()
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
    private fun createJsonReaderObservable(responseBody: ResponseBody): Observable<Tweet>? {
        return Observable.create<Tweet> { emitter ->
            JsonReader(responseBody.charStream())
                .also { it.isLenient = true }
                .use { reader ->
                    while (receiveRemoteData.value == true && reader.hasNext()) {
                        emitter.onNext(Gson().fromJson<Tweet>(reader, Tweet::class.java))
                    }
                    emitter.onComplete()
                }
        }
    }

    private fun getDateFromString(datetime: String?): Date? {
        datetime?.let {
            try {
                return SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault()).parse(datetime)
            } catch (parseException: ParseException) {
                e("Could not parse Tweet createdBy: ${parseException.message}")
            }
        }
        return null
    }
}