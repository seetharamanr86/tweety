package eu.micer.tweety.di

import androidx.room.Room
import eu.micer.tweety.feature.tweetlist.model.database.TweetDatabase
import org.koin.dsl.module

/**
 * In-Memory Room Database definition for testing purposes.
 */
val roomTestModule = module {
    single {
        Room.inMemoryDatabaseBuilder(get(), TweetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}
