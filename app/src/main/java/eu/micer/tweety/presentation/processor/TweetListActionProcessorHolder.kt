package eu.micer.tweety.presentation.processor

import com.github.ajalt.timberkt.e
import eu.micer.tweety.domain.repository.TweetRepository
import eu.micer.tweety.presentation.action.TweetListAction
import eu.micer.tweety.presentation.result.TweetListResult
import eu.micer.tweety.presentation.util.Constants
import eu.micer.tweety.presentation.util.extensions.runAllOnIoThread
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TweetListActionProcessorHolder(private val tweetRepository: TweetRepository) {

    private val startClearingTaskProcessor =
        ObservableTransformer<TweetListAction.StartCleaningTaskAction, TweetListResult> { actions ->
            actions.flatMap {
                    Observable.interval(Constants.Tweet.CLEARING_PERIOD_SECONDS, TimeUnit.SECONDS, Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .startWith(0L)
                        .subscribe({
                            removeExpiredTweets()
                        }, {
                            e(it)
                        })

                // no need to change the UI state
                Observable.empty()
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
                tweetRepository.getTweetsObservable(it.searchText)
                    .runAllOnIoThread()
                    .toObservable()
                    .map(TweetListResult.TrackingResult::Success)
                    .cast(TweetListResult.TrackingResult::class.java)
                    .onErrorReturn(TweetListResult.TrackingResult::Failure)
            }
        }

    private val stopTrackingProcessor =
        ObservableTransformer<TweetListAction.StopTrackingAction, TweetListResult> { actions ->
            actions.flatMap {
                tweetRepository.receiveRemoteData = false

                // no need to change the UI state
                Observable.empty()
            }
        }

    private val clearOfflineTweetsProcessor =
        ObservableTransformer<TweetListAction.ClearOfflineTweetsAction, TweetListResult> { actions ->
            actions.flatMap {
                tweetRepository.removeAllTweets()
                    .runAllOnIoThread()
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
}
