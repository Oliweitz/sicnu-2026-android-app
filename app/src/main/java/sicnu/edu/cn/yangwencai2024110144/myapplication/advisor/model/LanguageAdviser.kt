package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model

import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 模型层：语言文化顾问。
 *
 * 职责只有一件事——给一门语言，查出对应的建议。
 * 它不认识 Activity，碰不到任何控件，也没有 Context，
 * 因此这一层可以脱离 Android 单独测试。
 */
object LanguageAdviser {

    /** 全部可选语言。区二的 Spinner 就是用这份数据填的，而不是写死的数组 */
    val languages: List<Language> = Language.entries

    /** 供外部按位置取语言（Spinner 给的是位置，不是对象） */
    fun languageAt(position: Int): Language = languages[position]

    /** 查询：输入语言，返回该语言的问候语和文化小贴士 */
    fun advise(language: Language): LanguageAdvice = when (language) {
        Language.CHINESE -> LanguageAdvice(R.string.greeting_zh, R.string.tip_zh)
        Language.ENGLISH -> LanguageAdvice(R.string.greeting_en, R.string.tip_en)
        Language.FRENCH -> LanguageAdvice(R.string.greeting_fr, R.string.tip_fr)
        Language.JAPANESE -> LanguageAdvice(R.string.greeting_ja, R.string.tip_ja)
        Language.GERMAN -> LanguageAdvice(R.string.greeting_de, R.string.tip_de)
    }
}
