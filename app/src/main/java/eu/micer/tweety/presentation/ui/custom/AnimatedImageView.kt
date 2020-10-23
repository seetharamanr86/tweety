package eu.micer.tweety.presentation.ui.custom

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import eu.micer.tweety.R

class AnimatedImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var firstToSecond: AnimatedVectorDrawable? = null
    private var secondToFirst: AnimatedVectorDrawable? = null
    private var showingFirst = false

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedImageView,
            0, 0).apply {

            try {
                firstToSecond = getDrawable(R.styleable.AnimatedImageView_avdFirst) as AnimatedVectorDrawable
                secondToFirst = getDrawable(R.styleable.AnimatedImageView_avdSecond) as AnimatedVectorDrawable
            } finally {
                recycle()
            }
        }

        showingFirst = true
        setImageDrawable(firstToSecond)
    }

    fun showFirst() {
        if (!showingFirst) {
            morph()
        }
    }

    fun showSecond() {
        if (showingFirst) {
            morph()
        }
    }

    fun morph() {
        val drawable = if (showingFirst) firstToSecond else secondToFirst
        setImageDrawable(drawable)
        drawable?.start()
        showingFirst = !showingFirst
    }
}
