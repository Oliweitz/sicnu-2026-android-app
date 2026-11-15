package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage

import org.junit.Assert.assertEquals
import org.junit.Test
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Gender
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Hobby
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerProfile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 对象序列化测试：不依赖 Android，直接验证 Serializable 对象的往返结果。
 * 设备上的真实文件读写由 androidTest 里的 StorageTest 覆盖。
 */
class SerializationTest {

    @Test
    fun playerProfileSurvivesObjectStream() {
        val profile = PlayerProfile(
            name = "杨文才",
            studentId = "2024110144",
            gender = Gender.FEMALE,
            phone = "13900139000",
            email = "test@example.com",
            hobbies = setOf(Hobby.TRAVEL, Hobby.SHOPPING),
            avatarUri = "content://demo/avatar"
        )

        val restored = serializeRoundTrip(profile) as PlayerProfile

        assertEquals(profile, restored)
    }

    @Test
    fun gameHistorySurvivesObjectStream() {
        val history = arrayListOf(
            GameRecord(score = 18, playedAt = 3_000L, cardCount = 24, cardsInMatch = 3),
            GameRecord(score = -2, playedAt = 2_000L, cardCount = 24, cardsInMatch = 3)
        )

        val restored = serializeRoundTrip(history) as ArrayList<*>

        assertEquals(history, restored)
    }

    private fun serializeRoundTrip(value: Any): Any {
        val bytes = ByteArrayOutputStream().use { output ->
            ObjectOutputStream(output).use { it.writeObject(value) }
            output.toByteArray()
        }
        return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
    }
}
