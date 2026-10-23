package sicnu.edu.cn.yangwencai2024110144.myapplication.lab3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityCameraBinding

/**
 * 二级界面三：用隐式 Intent 调用系统相机拍照，并把照片显示出来。
 *
 * 隐式 Intent 只写“动作”（MediaStore.ACTION_IMAGE_CAPTURE），不指定由哪个类处理，
 * 系统会弹出（或直接打开）能响应这个动作的相机应用。本页面不需要自己写预览和快门。
 *
 * 结果处理采用 registerForActivityResult：
 *   - RequestPermission：先申请 CAMERA 运行时权限；
 *   - StartActivityForResult：接收相机应用返回的数据。
 * 相机默认返回一张缩略图，放在 extra 的 "data" 键下。
 */
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    /** 权限申请结果：同意就继续拍照，拒绝就提示原因 */
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    /** 相机应用返回：RESULT_OK 时取出缩略图并显示 */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // "data" 是系统相机约定的缩略图键名；BundleCompat 兼容新旧 API 的读取方式
            val extras = result.data?.extras
            val imageBitmap = extras?.let {
                BundleCompat.getParcelable(it, EXTRA_PHOTO, Bitmap::class.java)
            }
            if (imageBitmap != null) {
                binding.ivPhoto.setImageBitmap(imageBitmap)
            } else {
                Toast.makeText(this, R.string.camera_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnCamera.setOnClickListener { takePhotoWithPermission() }
    }

    /**
     * 先检查运行时权限。
     * 清单里声明了 CAMERA 却未获授权时，直接启动相机应用会抛 SecurityException，
     * 所以必须先申请，而不能只依赖安装时授权。
     */
    private fun takePhotoWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    /** 发出一条隐式 Intent，交给系统相机应用处理 */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // resolveActivity 之前需要在清单里用 <queries> 声明要查询的动作，
        // 否则 Android 11 及以上的包可见性限制会让它返回 null。
        if (takePictureIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.camera_no_app, Toast.LENGTH_SHORT).show()
            return
        }

        takePicture.launch(takePictureIntent)
    }

    companion object {
        /** 系统相机放在 Intent extras 里的缩略图键名 */
        private const val EXTRA_PHOTO = "data"
    }
}
