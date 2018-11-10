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
        tweetDatabase.close()
        closeKoin()
    }

    @Test
    fun testInsertAll() {
        val entities = getTweetEntityList()

        tweetDao.insertAll(entities)
        val ids = entities.map { it.id }

        val requestedEntities = ids.map {
                tweetDao.findById(it).blockingGet()
        }

        Assert.assertEquals(entities, requestedEntities)
    }

    // TODO write more tests

    private fun getTweetEntityList(): List<TweetEntity> {
        return arrayListOf(
            TweetEntity(
                id = 1,
                tweetId = 987654,
                text = "Sample test text #1.",
                user = "Dave Lister",
                createdAt = "Fri Nov 09 20:47:39 +0000 2018"
            ),
            TweetEntity(
                id = 2,
                tweetId = 987653,
                text = "Sample test text #2.",
                user = "Arnold J. Rimmer",
                createdAt = "Fri Nov 09 20:47:39 +0000 2018"
            ),
            TweetEntity(
                id = 3,
                tweetId = 987652,
                text = "Sample test text #3.",
                user = "Cat",
                createdAt = "Fri Nov 09 20:47:39 +0000 2018"
            ),
            TweetEntity(
                id = 4,
                tweetId = 987651,
                text = "Sample test text #4.",
                user = "Christine",
                createdAt = "Fri Nov 09 20:47:39 +0000 2018"
            )
        )
    }
}