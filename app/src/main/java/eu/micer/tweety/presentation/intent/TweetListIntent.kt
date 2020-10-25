package eu.micer.tweety.presentation.intent

import eu.micer.tweety.presentation.base.MviIntent

sealed class TweetListIntent : MviIntent {

    object InitialIntent : TweetListIntent()
    data class StartTrackingIntent(val searchText: String) : TweetListIntent()
    object StopTrackingIntent : TweetListIntent()
    object ClearOfflineTweetsIntent : TweetListIntent()
}
