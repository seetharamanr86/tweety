package eu.micer.tweety.feature.tweetlist.model.database

import android.arch.persistence.room.*
import io.reactivex.Maybe

@Dao
interface TweetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tweetEntity: TweetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tweetEntityList: List<TweetEntity>)

    @Delete
    fun delete(tweetEntity: TweetEntity)

    @Query("DELETE FROM TweetEntity")
    fun deleteAll()

    @Query("SELECT * FROM TweetEntity WHERE id = :id")
    fun findById(id: Int): Maybe<TweetEntity>

    @Query("SELECT * FROM TweetEntity")
    fun getAll(): List<TweetEntity>
}