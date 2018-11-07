package eu.micer.tweety.feature.tweetlist.vm

import android.os.Build
import com.github.ajalt.timberkt.e
import eu.micer.tweety.base.BaseViewModel
import eu.micer.tweety.network.TwitterApi
import eu.micer.tweety.util.extensions.subscribeObserveInBackground
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import okhttp3.ResponseBody
import okio.BufferedSource
import java.io.IOException
import java.io.UncheckedIOException


class TweetListViewModel(private val api: TwitterApi) : BaseViewModel() {
    fun getTweets() {
        api.getTweetsStream("trump")
            .subscribeObserveInBackground()
            .flatMap { responseBody: ResponseBody -> processEvents(responseBody.source()) }
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribe({ string: String ->
                println(string)
            }, { t: Throwable ->
                e(t)
            })
            .addTo(compositeDisposable)
    }

    private fun processEvents(source: BufferedSource): Observable<String> {
        return Observable.create { emitter ->
            var isCompleted = false
            try {
                while (!source.exhausted()) {
                    val next = source.readUtf8Line()
                    if (next != null) {
                        emitter.onNext(next)
                    }
                }
                emitter.onComplete()
            } catch (e: IOException) {
                if (e.message.equals("Socket closed")) {
                    isCompleted = true
                    emitter.onComplete()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        throw UncheckedIOException(e)
                    } else {
                        throw e
                    }
                }
            }
            if (!isCompleted) {
                emitter.onComplete()
            }
        }
    }
}
