package sicnu.edu.cn.yangwencai2024110144.myapplication.player

import android.content.Context
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 玩家数据仓库。
 *
 * 个人信息和当前最高分都只保存在内存中（实验要求修改后的信息无需写入文件），
 * 但主界面、修改界面和游戏界面共用这一份数据，所以改动后返回主界面能立刻看到。
 *
 * 三个界面都在主线程访问它，因此没有额外加锁。
 */
object PlayerRepository {

    private var initialized = false
    private var profile = PlayerProfile()

    val current: PlayerProfile
        get() = profile

    /** 首次使用时用 strings.xml 里的默认姓名、学号、联系方式等初始化。 */
    fun ensureInitialized(context: Context) {
        if (initialized) return

        val resources = context.applicationContext.resources
        profile = PlayerProfile(
            name = resources.getString(R.string.student_name),
            studentId = resources.getString(R.string.student_id),
            gender = Gender.MALE,
            phone = resources.getString(R.string.default_phone),
            email = resources.getString(R.string.default_email),
            hobbies = setOf(Hobby.READING, Hobby.GAME)
        )
        initialized = true
    }

    /** 保存修改后的个人信息；最高分继续保留。 */
    fun save(profile: PlayerProfile) {
        this.profile = profile
    }

    /** 游戏每得一次分就调用；只有超过历史最高分才更新。 */
    fun recordHighScore(score: Int) {
        if (score > profile.highScore) {
            profile = profile.copy(highScore = score)
        }
    }

    /**
     * 仅用于仪器测试：把内存数据恢复成空白状态，
     * 保证每条 UI 测试都在相同的数据基础上运行。
     */
    fun resetForTesting() {
        initialized = false
        profile = PlayerProfile()
    }
}
