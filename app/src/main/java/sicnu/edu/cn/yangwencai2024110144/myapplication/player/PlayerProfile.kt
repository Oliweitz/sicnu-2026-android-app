package sicnu.edu.cn.yangwencai2024110144.myapplication.player

import java.io.Serializable

/**
 * 玩家个人信息（应用级 Model）。
 *
 * 实验六要求用对象序列化保存个人信息：本类实现 [Serializable]，
 * 由 [sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.ProfileStore]
 * 通过 ObjectOutputStream / ObjectInputStream 写入和读取应用私有文件。
 *
 * [gender] 与 [hobbies] 的枚举同样可序列化，因此整个对象可以直接写入文件。
 */
data class PlayerProfile(
    val name: String = "",
    val studentId: String = "",
    val gender: Gender = Gender.MALE,
    val phone: String = "",
    val email: String = "",
    val hobbies: Set<Hobby> = emptySet(),
    val avatarUri: String? = null
) : Serializable {

    companion object {
        /** 序列化版本号：以后增减字段时可以控制旧文件是否还能读取。 */
        private const val serialVersionUID = 1L
    }
}

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
