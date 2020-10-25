package eu.micer.tweety.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.micer.tweety.presentation.util.event.Event1
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel<I : MviIntent, S : MviViewState> : ViewModel(), MviViewModel<I, S> {
    protected val disposables = CompositeDisposable()
    protected val showErrorEvent = MutableLiveData<Event1<String>>()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    val showError: LiveData<Event1<String>>
        get() = showErrorEvent
}
