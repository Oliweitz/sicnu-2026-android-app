package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model

import androidx.annotation.StringRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 模型层：顾问能介绍的语言。
 *
 * 每个枚举项只记住"名字的字符串资源 ID"，不存真正的文字。
 * 这样模型层就不需要 Context，取文字是视图层的事。
 */
enum class Language(@param:StringRes val displayNameRes: Int) {
    CHINESE(R.string.lang_zh),
    ENGLISH(R.string.lang_en),
    FRENCH(R.string.lang_fr),
    JAPANESE(R.string.lang_ja),
    GERMAN(R.string.lang_de),
}
