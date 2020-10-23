package eu.micer.tweety.presentation.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eu.micer.tweety.R
import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.util.Constants
import kotlinx.android.synthetic.main.item_tweet.view.*
import java.text.SimpleDateFormat
import java.util.*

class TweetAdapter(private var items: List<Tweet>, private val context: Context) :
    RecyclerView.Adapter<ViewHolder>() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(items[position]) {
            val formattedCreatedByDate = createdAt?.let {
                SimpleDateFormat(
                    Constants.Tweet.DATE_FORMAT_PATTERN,
                    Locale.getDefault()
                ).format(it)
            } ?: ""
            holder.tvCreatedAt.text = formattedCreatedByDate
            holder.tvText.text = text
            holder.tvUser.text = user
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    /**
     *  DiffUtil calculates the difference between two lists and outputs the list of modifications
     *  to be done(insert, update, delete) to ensure the transition from the old list to the new one.
     */
    fun updateItems(newItems: List<Tweet>) {
        val newItemsSorted = newItems.sortedWith(compareByDescending { it.timestamp })
        val firstItemTweetId = if (items.isNotEmpty()) items.first().tweetId else 0
        val postDiffCallback = PostDiffCallback(items, newItemsSorted)
        val diffResult = DiffUtil.calculateDiff(postDiffCallback)

        (items as ArrayList).clear()
        (items as ArrayList).addAll(newItemsSorted)
        diffResult.dispatchUpdatesTo(this)

        if (newItemsSorted.isNotEmpty() && newItemsSorted.first().tweetId != firstItemTweetId) {
            recyclerView?.scrollToPosition(0)
        }
    }

    fun clearItems() {
        items = ArrayList()
        notifyDataSetChanged()
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvCreatedAt: TextView = view.tv_created_at
    val tvText: TextView = view.tv_text
    val tvUser: TextView = view.tv_user
}

class PostDiffCallback(private val oldTweets: List<Tweet>, private val newTweets: List<Tweet>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldTweets.size
    }

    override fun getNewListSize(): Int {
        return newTweets.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTweets[oldItemPosition].tweetId == newTweets[newItemPosition].tweetId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTweets[oldItemPosition] == newTweets[newItemPosition]
    }
}
