package eu.micer.tweety.presentation.action

import eu.micer.tweety.presentation.base.MviAction

sealed class TweetListAction : MviAction {

    object InitialAction : TweetListAction()
    object StartCleaningTaskAction : TweetListAction()
    object GetOfflineData : TweetListAction()
    data class StartTrackingAction(val searchText: String) : TweetListAction()
    object StopTrackingAction : TweetListAction()
    object ClearOfflineTweetsAction : TweetListAction()
}
