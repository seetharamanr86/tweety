package eu.micer.tweety.util.extensions

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

fun <T> Maybe<T>.runAllOnIoThread(): Maybe<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
}

/**
 * @ImplNote: requestOn = false in subscribeOn() is needed to avoid deadlock in emitter,
 * see https://stackoverflow.com/a/44921023/1101730
 */
fun <T> Flowable<T>.runAllOnIoThread(): Flowable<T> {
    return subscribeOn(Schedulers.io(), false)
        .observeOn(Schedulers.io())
}

fun <T> Flowable<T>.toLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}