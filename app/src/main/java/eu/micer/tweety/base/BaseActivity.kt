package eu.micer.tweety.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

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
