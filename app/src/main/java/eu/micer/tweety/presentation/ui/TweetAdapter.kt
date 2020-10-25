package eu.micer.tweety.presentation.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import eu.micer.tweety.databinding.ItemTweetBinding
import eu.micer.tweety.domain.model.Tweet
import eu.micer.tweety.presentation.base.BaseViewHolder
import eu.micer.tweety.presentation.util.Constants
import java.text.SimpleDateFormat
import java.util.*

class TweetAdapter(private var items: List<Tweet>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder.createVH(parent, ItemTweetBinding::inflate)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemTweetBinding

        with(items[position]) {
            val formattedCreatedByDate = createdAt?.let {
                SimpleDateFormat(
                    Constants.Tweet.DATE_FORMAT_PATTERN,
                    Locale.getDefault()
                ).format(it)
            } ?: ""

            binding.let {
                it.tvCreatedAt.text = formattedCreatedByDate
                it.tvText.text = text
                it.tvUser.text = user
            }
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
