package eu.micer.tweety.feature.tweetlist.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TweetEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TweetDatabase : RoomDatabase() {
    abstract fun tweetDao(): TweetDao
}
