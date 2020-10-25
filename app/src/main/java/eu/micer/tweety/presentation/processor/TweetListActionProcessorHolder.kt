package eu.micer.tweety.presentation.processor

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import eu.micer.tweety.data.local.entity.TweetEntity
import eu.micer.tweety.data.local.entity.mapToDomainModel
import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.domain.repository.TweetRepository
import eu.micer.tweety.presentation.action.TweetListAction
import eu.micer.tweety.presentation.result.TweetListResult
import eu.micer.tweety.presentation.util.Constants
import eu.micer.tweety.presentation.util.extensions.runAllOnIoThread
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TweetListActionProcessorHolder(private val tweetRepository: TweetRepository) {

    private var tweetEntityList: List<TweetEntity> = ArrayList()
    private val requestRepeatDelay = 3L
    var receiveRemoteData = false

    private val startClearingTaskProcessor =
        ObservableTransformer<TweetListAction.StartCleaningTaskAction, TweetListResult> { actions ->
            actions.flatMap {
                    Observable.interval(Constants.Tweet.CLEARING_PERIOD_SECONDS, TimeUnit.SECONDS, Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .startWith(0L)
                        .doOnNext {
                            removeExpiredTweets()
                            // TODO update UI when something was removed
                        }
                        .flatMap {
                            // no need to return any result
                            Observable.empty<TweetListResult>()
                        }
                        .onErrorReturn(TweetListResult::ErrorResult)
            }
        }

    private val getOfflineDataProcessor =
        ObservableTransformer<TweetListAction.GetOfflineData, TweetListResult.GetOfflineDataResult> { actions ->
            actions.flatMap {
                tweetRepository.getOfflineData()
                    .runAllOnIoThread()
                    .toObservable()
                    .map(TweetListResult.GetOfflineDataResult::Success)
                    .cast(TweetListResult.GetOfflineDataResult::class.java)
                    .onErrorReturn(TweetListResult.GetOfflineDataResult::Failure)
            }
        }

    private val startTrackingProcessor =
        ObservableTransformer<TweetListAction.StartTrackingAction, TweetListResult.TrackingResult> { actions ->
            actions.flatMap {
                getTweetsObservable(it.searchText)
                    .runAllOnIoThread()
                    .toObservable()
                    .map(TweetListResult.TrackingResult::Success)
                    .cast(TweetListResult.TrackingResult::class.java)
                    .onErrorReturn(TweetListResult.TrackingResult::Failure)
                    .startWith(TweetListResult.TrackingResult.InFlight)
            }
        }

    private val stopTrackingProcessor =
        ObservableTransformer<TweetListAction.StopTrackingAction, TweetListResult> { actions ->
            actions.flatMap {
                receiveRemoteData = false

                Observable.just(TweetListResult.StopTrackingResult.Success)
            }
        }

    private val clearOfflineTweetsProcessor =
        ObservableTransformer<TweetListAction.ClearOfflineTweetsAction, TweetListResult> { actions ->
            actions.flatMap {
                tweetRepository.removeAllTweets()
                    .runAllOnIoThread()
                    .map {
                        (tweetEntityList as ArrayList).clear()
                        it
                    }
                    .subscribe()

                // no need to change the UI state
                Observable.empty()
            }
        }

    internal var actionProcessor =
        ObservableTransformer<TweetListAction, TweetListResult> { actions ->
            actions.flatMap(this::mapActions)
                .publish { shared ->
                    Observable.mergeArray(
                        shared.ofType(TweetListAction.StartCleaningTaskAction::class.java).compose(
                            startClearingTaskProcessor
                        ),
                        shared.ofType(TweetListAction.GetOfflineData::class.java).compose(
                            getOfflineDataProcessor
                        ),
                        shared.ofType(TweetListAction.StartTrackingAction::class.java).compose(
                            startTrackingProcessor
                        ),
                        shared.ofType(TweetListAction.StopTrackingAction::class.java).compose(
                            stopTrackingProcessor
                        ),
                        shared.ofType(TweetListAction.ClearOfflineTweetsAction::class.java).compose(
                            clearOfflineTweetsProcessor
                        )
                    ).mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is TweetListAction.StartCleaningTaskAction
                                    && v !is TweetListAction.GetOfflineData
                                    && v !is TweetListAction.StartTrackingAction
                                    && v !is TweetListAction.StopTrackingAction
                                    && v !is TweetListAction.ClearOfflineTweetsAction
                        }.flatMap { w ->
                            Observable.error(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
                }
        }

    private fun mapActions(action: TweetListAction): Observable<TweetListAction> {
        return when (action) {
            TweetListAction.InitialAction -> Observable.just(
                // trigger multiple actions instead of one
                TweetListAction.StartCleaningTaskAction,
                TweetListAction.GetOfflineData
            )
            else -> Observable.just(action)
        }
    }

    private fun removeExpiredTweets() {
        Timber.d("trying to remove expired tweets")
        val timestampMin = getMinimalTimestamp()
        // remove from database
        tweetRepository.removeExpiredTweets(timestampMin)
            .runAllOnIoThread()
            .ignoreElement()
            .doOnError { com.github.ajalt.timberkt.Timber.e(it) }
            .subscribe()
    }

    private fun getMinimalTimestamp(): Long {
        val lifespanMs = TimeUnit.SECONDS.toMillis(Constants.Tweet.LIFESPAN_SECONDS)
        return System.currentTimeMillis() - lifespanMs
    }

    private fun getTweetsObservable(
        track: String
    ): Flowable<List<Tweet>> {
        receiveRemoteData = true
        return tweetRepository.getTweetsStream(track)
            .subscribeOn(Schedulers.io())
            .flatMapObservable { responseBody ->
                createJsonReaderObservable(responseBody)
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .runAllOnIoThread()
            .map { tweet: eu.micer.tweety.data.remote.model.Tweet? ->
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
            .doOnNext(tweetRepository::saveTweet) // insert every new tweet into database
            .flatMap { tweetEntity ->
                (tweetEntityList as ArrayList).add(tweetEntity)
                Flowable.just(tweetEntityList)
            }
            .doOnError {
                Timber.e(it)
                val message =
                    "Error when fetching data:\n\n${it.message}\n\nNext try in $requestRepeatDelay seconds."
                Observable.just(TweetListResult.ErrorMessageResult(message))
            }
            .onErrorResumeNext(Function {
                /**
                 * Return tweets from local database in case of any error. When using Flowable in Room db DAO, it keeps
                 * listening to changes and never emits onComplete(). Therefore I'm using Flowable.just(...).
                 */
                Timber.d("Using data from local database.")
                Flowable.just(tweetRepository.getOfflineDataSync())
            })
            .doOnComplete {
                Timber.d("Receiving tweets has been completed.")
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
     * Provides JSON reader to parse incoming stream of data and emit Tweet objects.
     */
    private fun createJsonReaderObservable(responseBody: ResponseBody): Observable<eu.micer.tweety.data.remote.model.Tweet>? {
        return Observable.create { emitter ->
            JsonReader(responseBody.charStream())
                .also { it.isLenient = true }
                .use { reader ->
                    while (receiveRemoteData && reader.hasNext()) {
                        emitter.onNext(Gson().fromJson(reader, eu.micer.tweety.data.remote.model.Tweet::class.java))
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
            Timber.e("Could not parse Tweet createdBy: ${parseException.message}")
            null
        }
    }
}
