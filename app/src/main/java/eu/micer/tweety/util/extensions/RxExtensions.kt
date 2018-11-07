package eu.micer.tweety.util.extensions

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.subscribeObserveInBackground(): Single<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
}