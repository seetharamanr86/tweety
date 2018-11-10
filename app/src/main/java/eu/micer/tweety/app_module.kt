package eu.micer.tweety

import android.arch.persistence.room.Room
import eu.micer.tweety.feature.tweetlist.model.TweetDatabase
import eu.micer.tweety.feature.tweetlist.vm.TweetListViewModel
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.util.Constants
import eu.micer.tweety.util.UserPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.SigningInterceptor
import java.util.concurrent.TimeUnit

/**
 * KOIN - keywords:
 * applicationContext — declare your Koin application context
 * bean — declare a singleton instance component (unique instance)
 * factory — declare a factory instance component (new instance on each demand)
 * bind — declare an assignable class/interface to the provided component
 * get — retrieve a component, for provided definition function
 */

val appModule = applicationContext {
    viewModel { TweetListViewModel(get(), get()) }
}

val networkModule = applicationContext {
    bean { (get() as Retrofit).create(TwitterApi::class.java) as TwitterApi }
    bean {
        Retrofit.Builder()
            .addCallAdapterFactory(get())
            .addConverterFactory(get())
            .baseUrl(Constants.Network.URL_TWITTER_BASE)
            .client((get() as OkHttpClient.Builder).build())
            .build() as Retrofit
    }
    bean { RxJava2CallAdapterFactory.create() as retrofit2.CallAdapter.Factory }
//    bean { GsonBuilder().setDateFormat("EEE MMM dd HH:mm:ss Z yyyy").create() as Gson }
    bean { GsonConverterFactory.create() as retrofit2.Converter.Factory }
    bean {
        OkHttpClient.Builder()
            .addInterceptor(
                SigningInterceptor(OkHttpOAuthConsumer(
                    UserPreference.consumerKey,
                    UserPreference.consumerSecret
                ).apply {
                    setTokenWithSecret(
                        UserPreference.token,
                        UserPreference.tokenSecret
                    )
                })
            )
            .addInterceptor(HttpLoggingInterceptor()
                .apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
                as OkHttpClient.Builder
    }
}

val databaseModule = applicationContext {
    // Tweet Room database instance
    bean {
        Room.databaseBuilder(androidApplication(), TweetDatabase::class.java, "tweet-db")
            .build()
    }

    // Tweet DAO interface instance
    bean { get<TweetDatabase>().tweetDao() }
}

// Gather all app modules
val allModules = listOf(
    appModule,
    networkModule,
    databaseModule
)