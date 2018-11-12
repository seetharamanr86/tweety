package eu.micer.tweety.base

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

abstract class BaseActivity : AppCompatActivity() {

    abstract fun getViewModel(): BaseViewModel

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
}