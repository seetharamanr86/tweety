package eu.micer.tweety.util.extensions

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.subscribeObserveInBackground(): Observable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
}