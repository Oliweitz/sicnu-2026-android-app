package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityPersonInfoBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Gender
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Hobby
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.showAvatar

/**
 * 信息修改 Activity。
 *
 * 保存时只更新内存中的 [PlayerRepository]，不写文件；
 * 主 Activity 在 onResume 里重新读取，所以返回后立即看到新内容。
 */
class PersonInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonInfoBinding
    private var selectedAvatarUri: String? = null

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        try {
            // 让系统图库授予的读权限在本次应用运行期间一直有效
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // 个别图库不支持持久授权，本次会话内仍能正常显示
        }

        selectedAvatarUri = uri.toString()
        binding.ivAvatar.showAvatar(selectedAvatarUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerRepository.ensureInitialized(this)

        binding = ActivityPersonInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnPickAvatar.setOnClickListener {
            pickAvatar.launch(arrayOf("image/*"))
        }
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnCancel.setOnClickListener { finish() }

        loadProfile()
    }

    private fun loadProfile() {
        val profile = PlayerRepository.current

        selectedAvatarUri = profile.avatarUri
        binding.ivAvatar.showAvatar(profile.avatarUri)

        binding.etName.setText(profile.name)
        binding.etStudentId.setText(profile.studentId)
        binding.etPhone.setText(profile.phone)
        binding.etEmail.setText(profile.email)

        binding.rgGender.check(
            if (profile.gender == Gender.FEMALE) R.id.rb_female else R.id.rb_male
        )

        binding.cbTravel.isChecked = Hobby.TRAVEL in profile.hobbies
        binding.cbReading.isChecked = Hobby.READING in profile.hobbies
        binding.cbGame.isChecked = Hobby.GAME in profile.hobbies
        binding.cbShopping.isChecked = Hobby.SHOPPING in profile.hobbies
    }

    private fun saveProfile() {
        val name = binding.etName.text?.toString().orEmpty().trim()
        val studentId = binding.etStudentId.text?.toString().orEmpty().trim()
        val phone = binding.etPhone.text?.toString().orEmpty().trim()
        val email = binding.etEmail.text?.toString().orEmpty().trim()

        val nameValid = setFieldError(binding.tilName, name.isNotEmpty(), R.string.error_name_required)
        val idValid = setFieldError(
            binding.tilStudentId,
            studentId.isNotEmpty(),
            R.string.error_student_id_required
        )
        val phoneValid = setFieldError(
            binding.tilPhone,
            phone.isNotEmpty() && Patterns.PHONE.matcher(phone).matches(),
            if (phone.isEmpty()) R.string.error_phone_required else R.string.error_phone_invalid
        )
        val emailValid = setFieldError(
            binding.tilEmail,
            email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            if (email.isEmpty()) R.string.error_email_required else R.string.error_email_invalid
        )

        if (!nameValid || !idValid || !phoneValid || !emailValid) return

        val updated = PlayerRepository.current.copy(
            name = name,
            studentId = studentId,
            gender = if (binding.rbFemale.isChecked) Gender.FEMALE else Gender.MALE,
            phone = phone,
            email = email,
            hobbies = selectedHobbies(),
            avatarUri = selectedAvatarUri
        )
        PlayerRepository.save(updated)

        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun selectedHobbies(): Set<Hobby> {
        val hobbies = mutableSetOf<Hobby>()
        if (binding.cbTravel.isChecked) hobbies.add(Hobby.TRAVEL)
        if (binding.cbReading.isChecked) hobbies.add(Hobby.READING)
        if (binding.cbGame.isChecked) hobbies.add(Hobby.GAME)
        if (binding.cbShopping.isChecked) hobbies.add(Hobby.SHOPPING)
        return hobbies
    }

    private fun setFieldError(
        layout: TextInputLayout,
        isValid: Boolean,
        @StringRes messageRes: Int
    ): Boolean {
        layout.error = if (isValid) null else getString(messageRes)
        return isValid
    }
}
