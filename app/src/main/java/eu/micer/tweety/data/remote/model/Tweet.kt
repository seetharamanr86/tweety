package eu.micer.tweety.data.remote.model

import com.google.gson.annotations.SerializedName

data class Tweet(
    @SerializedName("contributors")
    val contributors: Any,
    @SerializedName("coordinates")
    val coordinates: Any,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("entities")
    val entities: Entities,
    @SerializedName("favorite_count")
    val favoriteCount: Int,
    @SerializedName("favorited")
    val favorited: Boolean,
    @SerializedName("filter_level")
    val filterLevel: String,
    @SerializedName("geo")
    val geo: Any,
    @SerializedName("id")
    val id: Long,
    @SerializedName("id_str")
    val idStr: String,
    @SerializedName("in_reply_to_screen_name")
    val inReplyToScreenName: Any,
    @SerializedName("in_reply_to_status_id")
    val inReplyToStatusId: Any,
    @SerializedName("in_reply_to_status_id_str")
    val inReplyToStatusIdStr: Any,
    @SerializedName("in_reply_to_user_id")
    val inReplyToUserId: Any,
    @SerializedName("in_reply_to_user_id_str")
    val inReplyToUserIdStr: Any,
    @SerializedName("is_quote_status")
    val isQuoteStatus: Boolean,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("place")
    val place: Any,
    @SerializedName("quote_count")
    val quoteCount: Int,
    @SerializedName("reply_count")
    val replyCount: Int,
    @SerializedName("retweet_count")
    val retweetCount: Int,
    @SerializedName("retweeted")
    val retweeted: Boolean,
    @SerializedName("retweeted_status")
    val retweetedStatus: RetweetedStatus,
    @SerializedName("source")
    val source: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("timestamp_ms")
    val timestampMs: String,
    @SerializedName("truncated")
    val truncated: Boolean,
    @SerializedName("user")
    val user: User
)
