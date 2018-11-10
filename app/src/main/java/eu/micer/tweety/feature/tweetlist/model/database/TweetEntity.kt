package eu.micer.tweety.feature.tweetlist.model.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class TweetEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var tweetId: Long,
    var text: String,
    var user: String,
    var createdAt: String,  // TODO convert to Date
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