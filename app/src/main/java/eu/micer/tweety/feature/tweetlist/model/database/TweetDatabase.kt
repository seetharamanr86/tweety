package eu.micer.tweety.feature.tweetlist.model.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [TweetEntity::class], version = 1)
abstract class TweetDatabase : RoomDatabase() {
    abstract fun tweetDao(): TweetDao
}