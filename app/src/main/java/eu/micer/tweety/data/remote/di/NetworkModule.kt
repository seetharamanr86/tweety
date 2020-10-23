package eu.micer.tweety.data.remote.di

import eu.micer.tweety.data.remote.api.TwitterApi
import eu.micer.tweety.presentation.util.Constants
import eu.micer.tweety.presentation.util.UserPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.SigningInterceptor
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { (get() as Retrofit).create(TwitterApi::class.java) as TwitterApi }
    single {
        Retrofit.Builder()
            .addCallAdapterFactory(get())
            .addConverterFactory(get())
            .baseUrl(Constants.Network.URL_TWITTER_BASE)
            .client((get() as OkHttpClient.Builder).build())
            .build() as Retrofit
    }
    single { RxJava2CallAdapterFactory.create() as CallAdapter.Factory }
    single { GsonConverterFactory.create() as Converter.Factory }
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                SigningInterceptor(
                    OkHttpOAuthConsumer(
                    UserPreference.consumerKey,
                    UserPreference.consumerSecret
                ).apply {
                    setTokenWithSecret(
                        UserPreference.token,
                        UserPreference.tokenSecret
                    )
                })
            )
            .addInterceptor(
                HttpLoggingInterceptor()
                .apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
    }
}
