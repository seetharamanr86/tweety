package eu.micer.tweety.feature.tweetlist.model.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [TweetEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TweetDatabase : RoomDatabase() {
    abstract fun tweetDao(): TweetDao
}