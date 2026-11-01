package sicnu.edu.cn.yangwencai2024110144.myapplication.player

/**
 * 玩家个人信息（应用级 Model）。
 *
 * 按实验要求，这些数据只在内存里保存，不写入文件；
 * 退出应用后会恢复为默认值。
 */
data class PlayerProfile(
    val name: String = "",
    val studentId: String = "",
    val gender: Gender = Gender.MALE,
    val phone: String = "",
    val email: String = "",
    val hobbies: Set<Hobby> = emptySet(),
    val highScore: Int = 0,
    val avatarUri: String? = null
)

enum class Gender {
    MALE,
    FEMALE
}

enum class Hobby {
    TRAVEL,
    READING,
    GAME,
    SHOPPING
}
