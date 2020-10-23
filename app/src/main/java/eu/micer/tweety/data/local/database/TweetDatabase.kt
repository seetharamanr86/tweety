package eu.micer.tweety.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.micer.tweety.data.local.converters.Converters
import eu.micer.tweety.data.local.dao.TweetDao
import eu.micer.tweety.data.local.entity.TweetEntity

@Database(entities = [TweetEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TweetDatabase : RoomDatabase() {
    abstract fun tweetDao(): TweetDao
}
