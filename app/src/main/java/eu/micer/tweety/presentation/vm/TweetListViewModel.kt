package eu.micer.tweety.presentation.vm

import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.action.TweetListAction
import eu.micer.tweety.presentation.base.BaseViewModel
import eu.micer.tweety.presentation.intent.TweetListIntent
import eu.micer.tweety.presentation.processor.TweetListActionProcessorHolder
import eu.micer.tweety.presentation.result.TweetListResult
import eu.micer.tweety.presentation.util.Constants
import eu.micer.tweety.presentation.util.extensions.notOfType
import eu.micer.tweety.presentation.viewstate.TweetListViewState
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TweetListViewModel(private val actionProcessorHolder: TweetListActionProcessorHolder) :
    BaseViewModel<TweetListIntent, TweetListViewState>() {

    private val intentsSubject: PublishSubject<TweetListIntent> = PublishSubject.create()
    private val statesObservable: Observable<TweetListViewState> = compose()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<TweetListIntent, TweetListIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(TweetListIntent.InitialIntent::class.java).take(1),
                    shared.notOfType(TweetListIntent.InitialIntent::class.java)
                )
            }
        }

    override fun processIntents(intents: Observable<TweetListIntent>) {
        intents.subscribe(intentsSubject::onNext).addTo(disposables)
    }

    override fun states(): Observable<TweetListViewState> = statesObservable

    /**
     * Compose all components to create the stream logic
     */
    private fun compose(): Observable<TweetListViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(TweetListViewState.idle(), reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)
    }

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    private fun actionFromIntent(intent: TweetListIntent): TweetListAction {
        return when (intent) {
            is TweetListIntent.InitialIntent -> TweetListAction.InitialAction
            is TweetListIntent.StartTrackingIntent -> TweetListAction.StartTrackingAction(intent.searchText)
            is TweetListIntent.StopTrackingIntent -> TweetListAction.StopTrackingAction
            is TweetListIntent.ClearOfflineTweetsIntent -> TweetListAction.ClearOfflineTweetsAction
        }
    }

    fun removeExpiredItemsFromList(list: List<Tweet>): List<Tweet> {
        return list.toMutableList().filter {
            it.timestamp > getMinimalTimestamp()
        }
    }

    private fun getMinimalTimestamp(): Long {
        val lifespanMs = TimeUnit.SECONDS.toMillis(Constants.Tweet.LIFESPAN_SECONDS)
        return System.currentTimeMillis() - lifespanMs
    }

    companion object {

        /**
         * The Reducer is where [MviViewState], that the [MviView] will use to
         * render itself, are created.
         * It takes the last cached [MviViewState], the latest [MviResult] and
         * creates a new [MviViewState] by only updating the related fields.
         * This is basically like a big switch statement of all possible types for the [MviResult]
         */
        private val reducer =
            BiFunction { previousState: TweetListViewState, result: TweetListResult ->
                when (result) {
                    is TweetListResult.ErrorResult -> processError(result.throwable, previousState)
                    is TweetListResult.GetOfflineDataResult ->
                        when (result) {
                            is TweetListResult.GetOfflineDataResult.Success ->
                                previousState.copy(
                                    tweetList = result.tweetList
                                )
                            is TweetListResult.GetOfflineDataResult.Failure ->
                                processError(result.error, previousState)
                        }
                    is TweetListResult.TrackingResult ->
                        when (result) {
                            is TweetListResult.TrackingResult.Success ->
                                previousState.copy(
                                    tweetList = result.tweetList
                                )
                            is TweetListResult.TrackingResult.Failure ->
                                processError(result.error, previousState)
                        }
                }
            }

        private fun processError(
            error: Throwable,
            previousState: TweetListViewState
        ): TweetListViewState {
            Timber.e(error, "Exception: ")
            return previousState.copy(
                isReceivingTweets = false,
                errorMessage = error.message ?: ""
            )
        }
    }
}
