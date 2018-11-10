package eu.micer.tweety.feature.tweetlist.model

import android.arch.persistence.room.*

@Dao
interface TweetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTweet(tweetEntity: TweetEntity)

    @Delete
    fun deleteTweet(tweetEntity: TweetEntity)

    @Query("SELECT * FROM TweetEntity")
    fun getTweets(): List<TweetEntity>
}