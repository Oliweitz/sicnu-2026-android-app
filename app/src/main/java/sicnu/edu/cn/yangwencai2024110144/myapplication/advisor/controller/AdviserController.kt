package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.controller

import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.LanguageAdviser
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.view.AdviserView

/**
 * 控制层：区二的中介。
 *
 * 它同时拿着视图和模型的引用，但两件事都不自己做：
 *   - 用户选了哪门语言，问视图要；
 *   - 这门语言该给什么建议，问模型要；
 *   - 查到的建议怎么显示，交回视图。
 *
 * 所以这里没有任何 findViewById，也没有一句 getString——
 * 换一套界面，这个类一行都不用改。
 */
class AdviserController(
    private val view: AdviserView,
    private val model: LanguageAdviser = LanguageAdviser,
) {

    /** "确定"按钮的事件响应：取输入 → 查模型 → 送回视图 */
    fun onConfirmClicked() {
        val advice = model.advise(view.selectedLanguage())
        view.showAdvice(advice)
    }
}
