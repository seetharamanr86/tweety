package eu.micer.tweety.app

import android.app.Application
import com.github.ajalt.timberkt.Timber
import com.github.ajalt.timberkt.Timber.DebugTree
import eu.micer.tweety.R
import eu.micer.tweety.app.di.allModules
import eu.micer.tweety.presentation.util.UserPreference
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())

        // Start Koin
        startKoin {
            // declare used Android context
            androidContext(this@MainApplication)
            // declare modules
            modules(allModules)
        }

        // Store secret data to SharedPreferences
        UserPreference.consumerKey = getString(R.string.consumer_key)
        UserPreference.consumerSecret = getString(R.string.consumer_secret)
        UserPreference.token = getString(R.string.token)
        UserPreference.tokenSecret = getString(R.string.token_secret)
    }
}
