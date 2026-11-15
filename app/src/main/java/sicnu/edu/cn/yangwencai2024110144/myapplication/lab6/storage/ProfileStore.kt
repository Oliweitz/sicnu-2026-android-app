package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage

import android.content.Context
import android.util.Log
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerProfile
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 个人信息的文件存储。
 *
 * 实验要求使用 openFileOutput / openFileInput 读写应用私有目录，
 * 并用 ObjectOutputStream / ObjectInputStream 序列化对象，
 * 所以这里没有使用 SharedPreferences 或数据库。
 *
 * 文件不存在、格式不兼容或读取失败时一律返回 null，
 * 由调用方回退到默认个人资料，不让旧文件导致应用崩溃。
 */
class ProfileStore(context: Context) {

    private val appContext = context.applicationContext

    fun load(): PlayerProfile? {
        return try {
            appContext.openFileInput(FILE_NAME).use { file ->
                ObjectInputStream(file).use { input ->
                    input.readObject() as? PlayerProfile
                }
            }
        } catch (_: FileNotFoundException) {
            null
        } catch (e: IOException) {
            Log.w(TAG, "读取个人信息失败，将使用默认资料", e)
            null
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "个人信息文件类型不兼容，将使用默认资料", e)
            null
        }
    }

    fun save(profile: PlayerProfile) {
        try {
            appContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { file ->
                ObjectOutputStream(file).use { output ->
                    output.writeObject(profile)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "保存个人信息失败", e)
        }
    }

    companion object {
        /** 个人信息保存的文件名，位于 /data/data/<包名>/files/ 下。 */
        const val FILE_NAME = "player_profile.dat"

        private const val TAG = "ProfileStore"
    }
}
