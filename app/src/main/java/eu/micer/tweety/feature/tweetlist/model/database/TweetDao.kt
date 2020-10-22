package eu.micer.tweety.feature.tweetlist.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Flowable
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

    @Query("DELETE FROM TweetEntity WHERE timestamp < :timestampMin")
    fun deleteExpired(timestampMin: Long)

    @Query("SELECT * FROM TweetEntity WHERE id = :id")
    fun findById(id: Int): Maybe<TweetEntity>

    @Query("SELECT * FROM TweetEntity")
    fun findAll(): Flowable<List<TweetEntity>>

    @Query("SELECT * FROM TweetEntity")
    fun findAllLiveData(): LiveData<List<TweetEntity>>

    @Query("SELECT * FROM TweetEntity")
    fun findAllSync(): List<TweetEntity>
}
