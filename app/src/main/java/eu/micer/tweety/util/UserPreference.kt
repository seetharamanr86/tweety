package eu.micer.tweety.util

import com.chibatching.kotpref.KotprefModel

object UserPreference : KotprefModel() {
    var consumerKey by stringPref()
    var consumerSecret by stringPref()
    var token by stringPref()
    var tokenSecret by stringPref()
}