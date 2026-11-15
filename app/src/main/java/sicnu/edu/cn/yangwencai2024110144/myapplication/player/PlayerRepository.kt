package sicnu.edu.cn.yangwencai2024110144.myapplication.player

import android.content.Context
import android.content.res.Resources
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.ProfileStore

/**
 * 玩家数据仓库。
 *
 * 实验六要求个人信息在退出应用后仍能恢复：
 * [ensureInitialized] 首次运行时会用 [ProfileStore] 从私有文件反序列化，
 * 文件不存在时才使用 strings.xml 里的默认值；
 * [save] 修改后立即把新对象序列化写回文件。
 *
 * 三个 Fragment 共用同一份内存数据，切换标签页后能看到最新内容。
 */
object PlayerRepository {

    private var initialized = false
    private var store: ProfileStore? = null
    private var profile = PlayerProfile()

    val current: PlayerProfile
        get() = profile

    /** 首次使用时加载本地文件；没有文件则用 strings.xml 里的默认资料。 */
    fun ensureInitialized(context: Context) {
        if (initialized) return

        val appContext = context.applicationContext
        val profileStore = ProfileStore(appContext)
        store = profileStore

        val loaded = profileStore.load()
        profile = loaded ?: defaultProfile(appContext.resources)
        if (loaded == null) {
            // 第一次运行就把默认资料写进文件，保证从此时起数据都能恢复。
            profileStore.save(profile)
        }
        initialized = true
    }

    /** 保存修改后的个人信息，并同步写入序列化文件。 */
    fun save(profile: PlayerProfile) {
        this.profile = profile
        store?.save(profile)
    }

    /**
     * 仅用于仪器测试：删除个人信息和游戏历史文件，恢复默认资料，
     * 保证每条 UI 测试都在相同的数据基础上运行。
     */
    fun resetForTesting(context: Context) {
        val appContext = context.applicationContext
        appContext.deleteFile(ProfileStore.FILE_NAME)
        appContext.deleteFile(GameHistoryStore.TWO_CARD_FILE)
        appContext.deleteFile(GameHistoryStore.THREE_CARD_FILE)

        initialized = false
        ensureInitialized(appContext)
    }

    /** 仅用于仪器测试：模拟进程重启，重新从文件加载个人信息。 */
    fun reloadForTesting(context: Context) {
        initialized = false
        ensureInitialized(context)
    }

    private fun defaultProfile(resources: Resources): PlayerProfile {
        return PlayerProfile(
            name = resources.getString(R.string.student_name),
            studentId = resources.getString(R.string.student_id),
            gender = Gender.MALE,
            phone = resources.getString(R.string.default_phone),
            email = resources.getString(R.string.default_email),
            hobbies = setOf(Hobby.READING, Hobby.GAME)
        )
    }
}
