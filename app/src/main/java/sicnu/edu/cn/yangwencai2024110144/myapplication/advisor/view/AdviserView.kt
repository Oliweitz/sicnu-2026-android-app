package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.view

import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.Language
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.LanguageAdvice

/**
 * 视图层对控制层暴露的接口。
 *
 * 控制层只认这个接口，不认 Activity，
 * 这样 MVC 的三方关系就是：视图 → 控制层 → 模型，模型和视图互不相识。
 */
interface AdviserView {

    /** 视图告诉控制层：用户当前选中的是哪门语言 */
    fun selectedLanguage(): Language

    /** 控制层告诉视图：模型查到的结果在这里，你来显示 */
    fun showAdvice(advice: LanguageAdvice)
}
