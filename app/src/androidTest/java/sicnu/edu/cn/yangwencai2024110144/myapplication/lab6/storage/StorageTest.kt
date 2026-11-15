package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Gender
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Hobby
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerProfile

/**
 * 文件存储测试：验证 openFileOutput / openFileInput 与对象流配合时，
 * 个人信息和游戏历史都能原样写入并读回。
 */
@RunWith(AndroidJUnit4::class)
class StorageTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearFiles() {
        context.deleteFile(ProfileStore.FILE_NAME)
        context.deleteFile(GameHistoryStore.TWO_CARD_FILE)
        context.deleteFile(GameHistoryStore.THREE_CARD_FILE)
    }

    @Test
    fun profileRoundTripsThroughPrivateFile() {
        val store = ProfileStore(context)
        val profile = PlayerProfile(
            name = "杨文才",
            studentId = "2024110144",
            gender = Gender.MALE,
            phone = "13800138000",
            email = "2024110144@sicnu.edu.cn",
            hobbies = setOf(Hobby.READING, Hobby.GAME),
            avatarUri = "content://demo/avatar"
        )

        store.save(profile)

        assertTrue(context.getFileStreamPath(ProfileStore.FILE_NAME).exists())
        assertEquals(profile, store.load())
    }

    @Test
    fun gameHistoryKeepsNewestFirstAndRestoresFromFile() {
        val store = GameHistoryStore(context, GameHistoryStore.TWO_CARD_FILE)

        val first = GameRecord(score = 10, playedAt = 1_000L, cardCount = 16, cardsInMatch = 2)
        val second = GameRecord(score = 14, playedAt = 2_000L, cardCount = 16, cardsInMatch = 2)

        var history = store.load()
        assertTrue(history.isEmpty())

        history = store.add(first, history)
        history = store.add(second, history)

        assertEquals(listOf(second, first), store.load())
        assertEquals(14, store.highScore())
    }
}
