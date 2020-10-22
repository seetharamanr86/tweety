package eu.micer.tweety.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
