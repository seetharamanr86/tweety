package eu.micer.tweety.presentation.util.event

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * https://medium.com/google-developers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
open class Event0 {

    var hasBeenHandled = false
        private set // Allow external read but not write

    fun shouldBeHandled() : Boolean {
        return if (hasBeenHandled) {
            false
        } else {
            hasBeenHandled = true
            true
        }
    }
}
