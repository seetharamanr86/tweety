package eu.micer.tweety.presentation.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity<I : MviIntent, S : MviViewState> : AppCompatActivity(), MviView<I, S> {

    abstract fun getViewModel(): BaseViewModel<I, S>

    protected val disposables = CompositeDisposable()

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getViewModel().showError.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { errorString ->
                if (errorString.isNotEmpty()) {
                    toast?.cancel()
                    toast = Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }
        })
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
