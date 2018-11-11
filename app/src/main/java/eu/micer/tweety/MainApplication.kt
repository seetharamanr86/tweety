package eu.micer.tweety

import android.support.multidex.MultiDexApplication
import com.github.ajalt.timberkt.Timber
import com.github.ajalt.timberkt.Timber.DebugTree
import com.squareup.leakcanary.LeakCanary
import eu.micer.tweety.di.allModules
import eu.micer.tweety.util.UserPreference
import org.koin.android.ext.android.startKoin

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        Timber.plant(DebugTree())

        // Start Koin
        startKoin(this, allModules)

        // Store secret data to SharedPreferences
        UserPreference.consumerKey = getString(R.string.consumer_key)
        UserPreference.consumerSecret = getString(R.string.consumer_secret)
        UserPreference.token = getString(R.string.token)
        UserPreference.tokenSecret = getString(R.string.token_secret)
    }
}