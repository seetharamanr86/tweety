package eu.micer.tweety.domain.repository

import eu.micer.tweety.data.local.dao.TweetDao
import eu.micer.tweety.data.local.entity.TweetEntity
import eu.micer.tweety.data.local.entity.mapToDomainModel
import eu.micer.tweety.data.remote.api.TwitterApi
import eu.micer.tweety.domain.model.Tweet
import io.reactivex.Maybe
import io.reactivex.functions.Action

class TweetRepository(private val twitterApi: TwitterApi, private val tweetDao: TweetDao) {

    /**
     * Loads data from database.
     */
    fun getOfflineData(): Maybe<List<Tweet>> {
        return tweetDao.findAll().map {
            it.mapToDomainModel()
        }
    }

    fun getOfflineDataSync() = tweetDao.findAllSync()

    /**
     * Removes all data from database.
     */
    fun removeAllTweets(): Maybe<Void> {
        return Maybe.fromAction(tweetDao::deleteAll)
    }

    /**
     * Removes expired data from database.
     */
    fun removeExpiredTweets(timestampMin: Long): Maybe<Void> {
        return Maybe.fromAction(Action(fun() {
            tweetDao.deleteExpired(timestampMin)
        }))
    }

    fun getTweetsStream(track: String) = twitterApi.getTweetsStream(track)

    fun saveTweet(entity: TweetEntity) = tweetDao.insert(entity)
}
