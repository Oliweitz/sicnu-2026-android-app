package sicnu.edu.cn.yangwencai2024110144.myapplication.player

import androidx.annotation.StringRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/** 把枚举翻译成字符串资源，避免在界面代码里写死“男/女/旅游……”等文案。 */
@StringRes
fun Gender.labelRes(): Int = when (this) {
    Gender.MALE -> R.string.gender_male
    Gender.FEMALE -> R.string.gender_female
}

@StringRes
fun Hobby.labelRes(): Int = when (this) {
    Hobby.TRAVEL -> R.string.hobby_travel
    Hobby.READING -> R.string.hobby_reading
    Hobby.GAME -> R.string.hobby_game
    Hobby.SHOPPING -> R.string.hobby_shopping
}
