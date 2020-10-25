package eu.micer.tweety.presentation.result

import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.base.MviResult

sealed class TweetListResult : MviResult {

    data class ErrorResult(val throwable: Throwable) : TweetListResult()

    sealed class GetOfflineDataResult : TweetListResult() {
        data class Success(val tweetList: List<Tweet>) : GetOfflineDataResult()
        data class Failure(val error: Throwable) : GetOfflineDataResult()
    }

    sealed class TrackingResult : TweetListResult() {
        data class Success(val tweetList: List<Tweet>) : TrackingResult()
        data class Failure(val error: Throwable) : TrackingResult()
    }
}
