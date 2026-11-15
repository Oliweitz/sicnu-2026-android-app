package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 翻牌游戏历史记录的存储。
 *
 * 两种玩法各用一个文件，互不影响。写入的是一整个 ArrayList<GameRecord>，
 * 由 ObjectOutputStream 序列化，读取时再由 ObjectInputStream 还原。
 *
 * 历史列表按“最新在前”的顺序保存，最多保留 [MAX_RECORDS] 条，
 * 避免文件无限增长。
 */
class GameHistoryStore(
    context: Context,
    private val fileName: String
) {

    private val appContext = context.applicationContext

    fun load(): List<GameRecord> {
        return try {
            appContext.openFileInput(fileName).use { file ->
                ObjectInputStream(file).use { input ->
                    val data = input.readObject() as? List<*>
                    data?.filterIsInstance<GameRecord>().orEmpty()
                }
            }
        } catch (_: FileNotFoundException) {
            emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "读取游戏历史失败：$fileName", e)
            emptyList()
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "游戏历史文件类型不兼容：$fileName", e)
            emptyList()
        }
    }

    fun save(records: List<GameRecord>) {
        try {
            appContext.openFileOutput(fileName, Context.MODE_PRIVATE).use { file ->
                ObjectOutputStream(file).use { output ->
                    output.writeObject(ArrayList(records))
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "保存游戏历史失败：$fileName", e)
        }
    }

    /** 在列表最前面插入本局记录并立即写入文件，返回更新后的列表。 */
    fun add(record: GameRecord, existing: List<GameRecord>): List<GameRecord> {
        val updated = (listOf(record) + existing).take(MAX_RECORDS)
        save(updated)
        return updated
    }

    /** 历史最高分；没有记录或全部为负分时按 0 处理。 */
    fun highScore(): Int {
        return (load().maxOfOrNull { it.score } ?: 0).coerceAtLeast(0)
    }

    companion object {
        const val TWO_CARD_FILE = "game_history_two.dat"
        const val THREE_CARD_FILE = "game_history_three.dat"

        /** 每个玩法最多保留的历史条数。 */
        const val MAX_RECORDS = 50

        private const val TAG = "GameHistoryStore"
    }
}
