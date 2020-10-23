package eu.micer.tweety.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.micer.tweety.domain.model.Tweet
import java.util.*

@Entity
data class TweetEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var tweetId: Long,
    var text: String,
    var user: String,
    var createdAt: Date?,
    var timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        return if (other is TweetEntity) {
            tweetId == other.tweetId
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + tweetId.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}


fun TweetEntity.mapToDomainModel(): Tweet =
    Tweet(
        id = this.id,
        tweetId = this.tweetId,
        text = this.text,
        user = this.user,
        createdAt = this.createdAt,
        timestamp = this.timestamp
    )

fun List<TweetEntity>.mapToDomainModel(): List<Tweet> =
    this.map { it.mapToDomainModel() }
