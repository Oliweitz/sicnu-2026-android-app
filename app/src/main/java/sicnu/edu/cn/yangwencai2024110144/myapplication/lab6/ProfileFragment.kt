package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.FragmentProfileBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Gender
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.Hobby
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.labelRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.showAvatar

/**
 * 第 1 个 Fragment：个人信息。
 *
 * 浏览模式显示头像、资料以及两种翻牌玩法的历史最高分；
 * 点击“修改个人信息”切换成编辑模式，保存时通过 [PlayerRepository]
 * 把 [sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerProfile]
 * 序列化写入应用私有文件，退出应用后仍可恢复。
 *
 * 历史最高分来自两个游戏历史文件，而不是另外存一份，
 * 这样只要历史记录还在，个人页就能正确显示。
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = checkNotNull(_binding) { "Fragment 视图尚未创建或已经销毁" }

    private lateinit var twoCardHistory: GameHistoryStore
    private lateinit var threeCardHistory: GameHistoryStore

    private var isEditing = false
    private var selectedAvatarUri: String? = null

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        try {
            // 让系统图库授予的读权限在本次应用运行期间一直有效
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // 个别图库不支持持久授权，本次会话内仍能正常显示
        }

        selectedAvatarUri = uri.toString()
        _binding?.ivAvatar?.showAvatar(selectedAvatarUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PlayerRepository.ensureInitialized(requireContext())

        twoCardHistory = GameHistoryStore(requireContext(), GameHistoryStore.TWO_CARD_FILE)
        threeCardHistory = GameHistoryStore(requireContext(), GameHistoryStore.THREE_CARD_FILE)

        binding.btnEditProfile.setOnClickListener { enterEditMode() }
        binding.btnPickAvatar.setOnClickListener { pickAvatar.launch(arrayOf("image/*")) }
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnCancel.setOnClickListener { leaveEditMode() }

        renderProfile()
    }

    override fun onResume() {
        super.onResume()
        // 从其他标签页或系统图库返回时刷新资料；编辑过程中不要覆盖表单。
        if (!isEditing) renderProfile()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && _binding != null && !isEditing) renderProfile()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /** 把仓库里的最新资料和历史最高分同步到浏览模式。 */
    private fun renderProfile() {
        val profile = PlayerRepository.current

        if (!isEditing) {
            binding.ivAvatar.showAvatar(profile.avatarUri)
        }

        binding.tvNameValue.text = profile.name
        binding.tvGenderValue.text = getString(profile.gender.labelRes())
        binding.tvStudentIdValue.text = profile.studentId
        binding.tvPhoneValue.text = profile.phone
        binding.tvEmailValue.text = profile.email

        binding.tvHobbiesValue.text = if (profile.hobbies.isEmpty()) {
            getString(R.string.hobbies_none)
        } else {
            profile.hobbies
                .sortedBy { it.ordinal }
                .joinToString(getString(R.string.list_separator)) { getString(it.labelRes()) }
        }

        binding.tvHighScoreTwoValue.text =
            getString(R.string.score_points_format, twoCardHistory.highScore())
        binding.tvHighScoreThreeValue.text =
            getString(R.string.score_points_format, threeCardHistory.highScore())
    }

    private fun enterEditMode() {
        isEditing = true
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

        clearFieldErrors()
        setEditingViewsVisible(true)
        binding.root.smoothScrollTo(0, 0)
    }

    private fun leaveEditMode() {
        isEditing = false
        selectedAvatarUri = PlayerRepository.current.avatarUri
        clearFieldErrors()
        setEditingViewsVisible(false)
        renderProfile()
    }

    private fun setEditingViewsVisible(editing: Boolean) {
        binding.profileDisplay.visibility = if (editing) View.GONE else View.VISIBLE
        binding.profileEdit.visibility = if (editing) View.VISIBLE else View.GONE
        binding.btnPickAvatar.visibility = if (editing) View.VISIBLE else View.GONE
        binding.tvAvatarTip.visibility = if (editing) View.VISIBLE else View.GONE
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
        // save 内部会用 ObjectOutputStream 写入 player_profile.dat
        PlayerRepository.save(updated)

        Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show()
        leaveEditMode()
    }

    private fun selectedHobbies(): Set<Hobby> {
        val hobbies = mutableSetOf<Hobby>()
        if (binding.cbTravel.isChecked) hobbies.add(Hobby.TRAVEL)
        if (binding.cbReading.isChecked) hobbies.add(Hobby.READING)
        if (binding.cbGame.isChecked) hobbies.add(Hobby.GAME)
        if (binding.cbShopping.isChecked) hobbies.add(Hobby.SHOPPING)
        return hobbies
    }

    private fun clearFieldErrors() {
        binding.tilName.error = null
        binding.tilStudentId.error = null
        binding.tilPhone.error = null
        binding.tilEmail.error = null
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
