package eu.micer.tweety.di

import android.arch.persistence.room.Room
import eu.micer.tweety.feature.tweetlist.model.TweetDatabase
import org.koin.dsl.module.applicationContext

/**
 * In-Memory Room Database definition for testing purposes.
 */
val roomTestModule = applicationContext {
    bean {
        Room.inMemoryDatabaseBuilder(get(), TweetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}