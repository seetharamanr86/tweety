package eu.micer.tweety.presentation.viewstate

import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.base.MviViewState

data class TweetListViewState(
    val errorMessage: String,
    val tweetList: List<Tweet>,
    val isReceivingTweets: Boolean,
    val searchText: String
) : MviViewState {
    companion object {
        fun idle(): TweetListViewState =
            TweetListViewState(
                errorMessage = "",
                tweetList = emptyList(),
                isReceivingTweets = false,
                searchText = ""
            )
    }
}
