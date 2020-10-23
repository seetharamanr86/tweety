package eu.micer.tweety.feature.tweetlist.model

import eu.micer.tweety.data.local.entity.TweetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class TweetEntityUnitTest {
    @Test
    fun `objects are equal when they have same tweet id`() {
        val tweetEntity1 = TweetEntity(
            id = 20,
            tweetId = 999,
            text = "text",
            user = "user",
            createdAt = Date()
        )
        val tweetEntity2 = TweetEntity(
            id = 21,
            tweetId = 999,
            text = "text1",
            user = "user1",
            createdAt = Date()
        )
        assertEquals(tweetEntity1, tweetEntity2)
    }

    @Test
    fun `objects are NOT equal when they have different tweet id`() {
        val tweetEntity1 = TweetEntity(
            id = 20,
            tweetId = 900,
            text = "text",
            user = "user",
            createdAt = Date()
        )
        val tweetEntity2 = TweetEntity(
            id = 20,
            tweetId = 999,
            text = "text",
            user = "user",
            createdAt = Date()
        )
        assertNotEquals(tweetEntity1, tweetEntity2)
    }
}
