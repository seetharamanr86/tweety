package eu.micer.tweety.util

object Constants {

    object Network {
        const val URL_TWITTER_BASE = "https://stream.twitter.com/1.1/"
        const val URL_TWITTER_STATUSES_FILTER = "statuses/filter.json"
    }

    object Tweet {
        const val CLEARING_PERIOD_SECONDS: Long = 2
        const val LIFESPAN_SECONDS: Long = 30
    }
}