package eu.micer.tweety.data.local.di

import androidx.room.Room
import eu.micer.tweety.data.local.database.TweetDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    // Tweet Room database instance
    single {
        Room.databaseBuilder(androidApplication(), TweetDatabase::class.java, "tweet-db")
            .build()
    }

    // Tweet DAO interface instance
    single { get<TweetDatabase>().tweetDao() }
}
