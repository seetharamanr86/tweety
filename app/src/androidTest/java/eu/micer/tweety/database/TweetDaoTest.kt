package eu.micer.tweety.database

import android.support.test.runner.AndroidJUnit4
import eu.micer.tweety.di.roomTestModule
import eu.micer.tweety.feature.tweetlist.model.database.TweetDao
import eu.micer.tweety.feature.tweetlist.model.database.TweetDatabase
import eu.micer.tweety.feature.tweetlist.model.database.TweetEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.util.*

@RunWith(AndroidJUnit4::class)
class TweetDaoTest : KoinTest {

    private val tweetDatabase: TweetDatabase by inject()
    private val tweetDao: TweetDao by inject()

    @Before()
    fun before() {
        loadKoinModules(roomTestModule)
    }

    @After
    fun after() {
        // FIXME failing when more then one test - why?
//        tweetDatabase.close()
//        closeKoin()
    }

    @Test
    fun testInsert() {
        val entity = TweetEntity(
            id = 1,
            tweetId = 987654,
            text = "text",
            user = "user",
            createdAt = Date()
        )

        tweetDao.insert(entity)
        val insertedId = entity.id

        val requestedEntity = tweetDao.findById(insertedId).blockingGet()

        Assert.assertEquals(entity, requestedEntity)

        // FIXME remove when execution in @After is working
        tweetDatabase.close()
        closeKoin()
    }

    @Test
    fun testInsertAll() {
        val entities = getTweetEntityList()

        tweetDao.insertAll(entities)

        val requestedEntities = tweetDao.findAllSync()

        Assert.assertEquals(entities, requestedEntities)
    }

    @Test
    fun testDelete() {
        val entity = TweetEntity(
            id = 100,
            tweetId = 987654,
            text = "text",
            user = "user",
            createdAt = Date()
        )

        tweetDao.insert(entity)
        val insertedId = entity.id
        tweetDao.delete(entity)

        val requestedEntity = tweetDao.findById(insertedId).blockingGet()

        Assert.assertNull(requestedEntity)
    }

    @Test
    fun testDeleteAll() {
        val entities = getTweetEntityList()

        tweetDao.insertAll(entities)

        tweetDao.deleteAll()

        val requestedEntities = tweetDao.findAllSync()

        Assert.assertTrue(requestedEntities.isEmpty())
    }

    @Test
    fun testDeleteExpired() {
        val entity = TweetEntity(
            id = 101,
            tweetId = 987654,
            text = "text",
            user = "user",
            createdAt = Date(),
            timestamp = 1000L
        )
        val entityId = entity.id

        val expiredEntity = TweetEntity(
            id = 102,
            tweetId = 987654,
            text = "text",
            user = "user",
            createdAt = Date(),
            timestamp = 500L
        )
        val expiredEntityId = expiredEntity.id

        val timestampMin = 800L

        tweetDao.insert(entity)
        tweetDao.insert(expiredEntity)

        tweetDao.deleteExpired(timestampMin)

        val ent = tweetDao.findById(entityId).blockingGet()
        val expiredEnt = tweetDao.findById(expiredEntityId).blockingGet()

        Assert.assertNotNull(ent)
        Assert.assertNull(expiredEnt)
    }

    private fun getTweetEntityList(): List<TweetEntity> {
        return arrayListOf(
            TweetEntity(
                id = 1,
                tweetId = 987654,
                text = "Sample test text #1.",
                user = "Dave Lister",
                createdAt = Date()
            ),
            TweetEntity(
                id = 2,
                tweetId = 987653,
                text = "Sample test text #2.",
                user = "Arnold J. Rimmer",
                createdAt = Date()
            ),
            TweetEntity(
                id = 3,
                tweetId = 987652,
                text = "Sample test text #3.",
                user = "Cat",
                createdAt = Date()
            ),
            TweetEntity(
                id = 4,
                tweetId = 987651,
                text = "Sample test text #4.",
                user = "Christine",
                createdAt = Date()
            )
        )
    }
}