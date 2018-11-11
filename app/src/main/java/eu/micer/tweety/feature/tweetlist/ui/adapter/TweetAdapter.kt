package eu.micer.tweety.feature.tweetlist.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import eu.micer.tweety.R
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import kotlinx.android.synthetic.main.item_tweet.view.*

class TweetAdapter(private var items: List<TweetEntity>, private val context: Context) :
    RecyclerView.Adapter<ViewHolder>() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCreatedAt.text = items[position].createdAt
        holder.tvText.text = items[position].text
        holder.tvUser.text = items[position].user
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun updateItems(tweetList: List<TweetEntity>) {
        val sortedTweetList = tweetList.sortedWith(compareByDescending { it.timestamp })
        items = sortedTweetList
        notifyDataSetChanged()
        if (sortedTweetList.isNotEmpty() && items.isNotEmpty() && sortedTweetList[0] != items[0]) {
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