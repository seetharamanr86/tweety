package eu.micer.tweety.feature.tweetlist.model

import android.arch.persistence.room.*
import io.reactivex.Single

@Dao
interface TweetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tweetEntity: TweetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tweetEntityList: List<TweetEntity>)

    @Delete
    fun delete(tweetEntity: TweetEntity)

    @Query("SELECT * FROM TweetEntity WHERE id = :id")
    fun findById(id: Int): Single<TweetEntity>

    @Query("SELECT * FROM TweetEntity")
    fun getAll(): List<TweetEntity>
}