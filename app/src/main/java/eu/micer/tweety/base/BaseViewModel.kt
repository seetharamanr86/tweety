package eu.micer.tweety.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import eu.micer.tweety.util.event.Event1
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {
    protected val compositeDisposable = CompositeDisposable()
    protected val showErrorEvent = MutableLiveData<Event1<String>>()

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    val showError: LiveData<Event1<String>>
        get() = showErrorEvent
}