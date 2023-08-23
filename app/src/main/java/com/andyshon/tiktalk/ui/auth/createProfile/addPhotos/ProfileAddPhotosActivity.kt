package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.Image
import com.andyshon.tiktalk.ui.MainActivity
import com.andyshon.tiktalk.data.entity.PhotoHolderMetadata
import com.andyshon.tiktalk.ui.auth.createProfile.crop.CropImageActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.string
import kotlinx.android.synthetic.main.activity_profile_add_photos.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import org.jetbrains.anko.alert
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

class ProfileAddPhotosActivity : BaseInjectActivity(), AddPhotosContract.View {

    private val adapter: PhotoAdapter by lazy(LazyThreadSafetyMode.NONE) {
        PhotoAdapter(
            this,
            R.layout.item_profile_photo,
            PhotoHolder::class.java,
            UserMetadata.photos,
            true,
            setClickListener()
        )
    }

    @Inject
    lateinit var presenter: AddPhotosPresenter
    override fun getPresenter(): BaseContract.Presenter<*> = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_add_photos)
        tvToolbarTitle.text = this string R.string.create_profile_title
        initListeners()
        setupList()
    }

    private fun setupList() {
        PhotoHolderMetadata.edit = false
        gridview.layoutManager = GridLayoutManager(this@ProfileAddPhotosActivity, 3)
        gridview.adapter = adapter
        val helper = DragHelper(adapter)
        val touchHelper = ItemTouchHelper(helper)
        touchHelper.attachToRecyclerView(gridview)
    }

    private fun initListeners() {
        btnToolbarBack.setOnClickListener { finish() }
        btnDone.setOnClickListener {
            if (canOpen()) {
                val email = intent.getStringExtra("email") ?: ""
                val phone = intent.getStringExtra("phone") ?: ""
                val codeCountry = intent.getStringExtra("codeCountry") ?: ""
                val name = intent.getStringExtra("name") ?: ""
                val dob = intent.getStringExtra("dob") ?: ""
                val location = intent.getStringExtra("location") ?: ""
                val gender = intent.getStringExtra("gender") ?: ""
                presenter.register(email, phone, codeCountry, name, dob, location, location, gender)
            }
        }
    }

    private fun setClickListener(): SRListener<Image> {
        return object : SRListener<Image> {
            override fun onItemClick(pos: Int, item: Image, type: Int) {
                Timber.e("Click on photo $item, type = $type")
                if (item.url.isEmpty()) {
                    openSmallPhotoMenu()
                } else {
                    deletePhoto(pos)
                }
            }
        }
    }

    private fun openSmallPhotoMenu() {
        val actions = resources.getStringArray(R.array.profile_photo_main_menu)
        val dialog = AlertDialog.Builder(this)
            .setItems(actions) { dialogInterface, selectedIndex ->
                when (selectedIndex) {
                    GALLERY_INDEX -> pickImageFileInCamera()
                    CAMERA_INDEX -> pickImageFileInGallery()
                }
                dialogInterface.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun checkIfCanDone() {
        btnDone.isEnabled = UserMetadata.hasAtLeastOnePhoto()
    }

    private val cropActivityActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.path?.let {
                val index = UserMetadata.getIndexToInsert()
                UserMetadata.photos.set(index, Image(0, it))
                adapter.updateObject(index, Image((100..10000).random(), it))
                checkIfCanDone()
            }
        }
    }


    private fun deletePhoto(pos: Int) {
        alert("Do you want to delete this photo?") {
            isCancelable = true
            positiveButton("Delete") {
                adapter.removeObject(pos)
                adapter.addObject(UserMetadata.photos.size, Image(0, ""))
                adapter.notifyDataSetChanged()
                checkIfCanDone()
            }
            negativeButton("Cancel") { it.dismiss() }
        }.show()
    }


    private fun pickImageFileInGallery() {
        if (galleryPermissionsGranted()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultPickPhotoOnGallery.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this, ALL_PERMISSIONS, REQUEST_CODE_GALLERY_PERMISSION
            )
        }
    }

    private fun pickImageFileInCamera() {
        if (cameraPermissionsGranted()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            resultCamera.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this, ALL_PERMISSIONS, REQUEST_CODE_CAMERA_PERMISSION
            )
        }
    }

    private val resultPickPhotoOnGallery = registerForActivityResult(
        createActivityResultContracts()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data: Intent? = result.data
        data?.data?.let { uri -> handlePickPhotoResult(uri) }
    }

    private fun handlePickPhotoResult(uri: Uri) {
        val file = convertUriToFile(uri) ?: return
        val outputFileUri = Uri.fromFile(file)
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, outputFileUri)
            if (bitmap != null) {
                val gotoCropImage =
                    Intent(this@ProfileAddPhotosActivity, CropImageActivity::class.java)
                gotoCropImage.data = outputFileUri
                cropActivityActivityLauncher.launch(gotoCropImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val resultCamera = registerForActivityResult(
        createActivityResultContracts()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val photo = result.data?.extras?.get(DATA) as? Bitmap ?: return@registerForActivityResult
        handlePickPhotoFromCamera(photo)
    }

    private fun handlePickPhotoFromCamera(bitmap: Bitmap) {
        val gotoCropImage = Intent(this@ProfileAddPhotosActivity, CropImageActivity::class.java)
        gotoCropImage.data = convertBitmapToFile(bitmap)
        cropActivityActivityLauncher.launch(gotoCropImage)
    }

    private fun createActivityResultContracts() = ActivityResultContracts.StartActivityForResult()

    private fun cameraPermissionsGranted() = ALL_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun galleryPermissionsGranted() = ALL_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION && cameraPermissionsGranted()) {
            pickImageFileInCamera()
        }
        if (requestCode == REQUEST_CODE_GALLERY_PERMISSION && galleryPermissionsGranted()) {
            pickImageFileInGallery()
        }
    }

    private fun convertUriToFile(documentUri: Uri?): File? {
        if (documentUri == null) return null
        val inputStream = contentResolver?.openInputStream(documentUri)
        var file: File?
        inputStream.use { input ->
            file = File(cacheDir, System.currentTimeMillis().toString() + ".png")
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int = -1
                while (input?.read(buffer).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file
    }

    private fun convertBitmapToFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }

    override fun onPhotosLoaded() {

    }

    override fun onRegistered() {
        val intent = Intent(this@ProfileAddPhotosActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    companion object {
        private const val DATA = "data"
        private const val REQUEST_CODE_CAMERA_PERMISSION = 10
        private const val REQUEST_CODE_GALLERY_PERMISSION = 11
        private const val GALLERY_INDEX = 0
        private const val CAMERA_INDEX = 1

        private val ALL_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun startActivity(
            context: Context,
            email: String,
            phone: String,
            codeCountry: String,
            name: String,
            dob: String,
            location: String,
            gender: String
        ) {
            val intent = Intent(context, ProfileAddPhotosActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("phone", phone)
            intent.putExtra("codeCountry", codeCountry)
            intent.putExtra("name", name)
            intent.putExtra("dob", dob)
            intent.putExtra("location", location)
            intent.putExtra("gender", gender)
            context.startActivity(intent)
        }
    }
}
