package com.andyshon.tiktalk.ui.editProfile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.Image
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.entity.PhotoHolderMetadata
import com.andyshon.tiktalk.ui.auth.createProfile.addPhotos.*
import com.andyshon.tiktalk.ui.auth.createProfile.crop.CropImageActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.editProfile.basicInfo.BasicInfoActivity
import com.andyshon.tiktalk.ui.editProfile.moreLanguages.MoreLanguagesActivity
import com.andyshon.tiktalk.ui.editProfile.userPageWatch.UserPageWatchActivity
import com.andyshon.tiktalk.utils.extensions.getAge
import com.andyshon.tiktalk.utils.extensions.hide
import com.andyshon.tiktalk.utils.extensions.show
import com.google.android.gms.common.util.IOUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.app_toolbar_edit_profile.*
import org.jetbrains.anko.alert
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import javax.inject.Inject

private const val LANGUAGES_REQUEST = 111
private const val CAMERA_REQUEST = 666
private const val GALLERY_REQUEST = 777
private const val CROP_REQUEST = 567

class EditProfileActivity : BaseInjectActivity(), EditProfileContract.View {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, EditProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject lateinit var presenter: EditProfilePresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = null

    @Inject lateinit var prefs: PreferenceManager

    private lateinit var adapter: PhotoAdapter

    private var education = ""
    private var work = ""
    private var about = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
//        this.window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        setContentView(R.layout.activity_edit_profile)

        initListeners()
        restorePrefs()
        setupList()
    }

    override fun onResume() {
        super.onResume()
        setBasicInfoText()
    }

    private fun initListeners() {
        toolbarBtnBack.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            finish()
        }
        toolbarBtnWatchProfile.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            if (canOpen())
                UserPageWatchActivity.startActivity(this)
        }
        btnBasicInfo.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            if (canOpen())
                BasicInfoActivity.startActivity(this)
        }
        btnLiving.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.living(tvLiving.text.toString())
        }
        btnChildren.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.children(tvChildren.text.toString())
        }
        btnSmoking.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.smoking(tvSmoking.text.toString())
        }
        btnDrinking.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.drinking(tvDrinking.text.toString())
        }
        btnHeight.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.height(tvHeight.text.toString())
        }
        btnRelationship.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.relationship(tvRelationship.text.toString())
        }
        btnSexuality.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.sexuality(tvSexuality.text.toString())
        }
        btnZodiac.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            presenter.zodiac(tvZodiac.text.toString())
        }
        btnISpeak.setOnClickListener {
            etEducation.clearFocus()
            etAbout.clearFocus()
            if (canOpen()) {
                val intent = Intent(this, MoreLanguagesActivity::class.java)
                startActivityForResult(intent, LANGUAGES_REQUEST)
            }
        }
        education = etEducation.text.toString().trim()
        etEducation.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (education != etEducation.text.toString().trim()) {
                    presenter.education(etEducation.text.toString().trim())
                }
            }
        }
        etEducation.setOnEditorActionListener(TextView.OnEditorActionListener { view, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                if (education != etEducation.text.toString().trim()) {
                    presenter.education(etEducation.text.toString().trim())
                }
                return@OnEditorActionListener true
            }
            false
        })
        about = etAbout.text.toString().trim()
        etAbout.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (about != etAbout.text.toString().trim()) {
                    presenter.about(etAbout.text.toString().trim())
                }
            }
        }
        etAbout.setOnEditorActionListener(TextView.OnEditorActionListener { view, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                if (about != etAbout.text.toString().trim()) {
                    presenter.about(etAbout.text.toString().trim())
                }
                return@OnEditorActionListener true
            }
            false
        })
        work = etWork.text.toString().trim()
        etWork.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                tvWork.show()
                if (work != etWork.text.toString().trim()) {
                    presenter.work(etWork.text.toString().trim())
                }
                etWork.setText("")
            }
            else {
                tvWork.hide()
            }
        }
        etWork.setOnEditorActionListener(TextView.OnEditorActionListener { view, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                if (work != etWork.text.toString().trim()) {
                    tvWork.text = etWork.text.toString().trim()
                    presenter.work(etWork.text.toString().trim())
                    tvWork.show()
                }
                etWork.setText("")
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun updated() {

    }

    override fun setEducation(status: String) {
        education = status
        etEducation.setText(education)
        etEducation.clearFocus()
    }

    override fun setWork(status: String) {
        work = status
        Timber.e("WORK = $status")
        tvWork.text = status
        etWork.clearFocus()
    }

    override fun setAbout(status: String) {
        about = status
        etAbout.setText(about)
        etAbout.clearFocus()
    }

    override fun setRelationship(status: String) {
        tvRelationship.text = status
    }

    override fun setSexuality(status: String) {
        tvSexuality.text = status
    }

    override fun setHeight(height: Int) {
        tvHeight.text = getString(R.string.height_, height)
    }

    override fun setLiving(status: String) {
        tvLiving.text = status
    }

    override fun setChildren(status: String) {
        tvChildren.text = status
    }

    override fun setSmoking(status: String) {
        tvSmoking.text = status
    }

    override fun setDrinking(status: String) {
        tvDrinking.text = status
    }

    override fun setZodiac(status: String) {
        tvZodiac.text = status
    }

    private fun setISpeak() {
        val selectedLanguages = prefs.getStringSet(Preference.KEY_USER_I_SPEAK) ?: UserMetadata.languages
        if (selectedLanguages.isNotEmpty()) {
            val languages = StringBuilder()
            selectedLanguages.forEach {
                if (selectedLanguages.indexOf(it) != selectedLanguages.size - 1)
                    languages.append(it.plus(", "))
                else languages.append(it)
            }
            tvISpeak.text = languages
        }
    }

    private fun setBasicInfoText() {
        val userName = prefs.getObject(Preference.KEY_USER_NAME, String::class.java) ?: ""
        val userGender = prefs.getObject(Preference.KEY_USER_GENDER, String::class.java) ?: "Male"

        tvBasicInfo.text = userName.plus(", ").plus(getAge(UserMetadata.birthday)).plus(", ").plus(userGender)
    }

    private fun restorePrefs() {
        val relationship = prefs.getObject(Preference.KEY_USER_RELATIONSHIP, String::class.java) ?: ""
        tvRelationship.text = relationship.replace("_", " ")

        val sexuality = prefs.getObject(Preference.KEY_USER_SEXUALITY, String::class.java) ?: ""
        tvSexuality.text = sexuality.replace("_", " ")

        val height = prefs.getObject(Preference.KEY_USER_HEIGHT, String::class.java) ?: ""
        tvHeight.text = height.replace("_", " ")

        val living = prefs.getObject(Preference.KEY_USER_LIVING, String::class.java) ?: ""
        tvLiving.text = living.replace("_", " ")

        val children = prefs.getObject(Preference.KEY_USER_CHILDREN, String::class.java) ?: ""
        tvChildren.text = children.replace("_", " ")

        val smoking = prefs.getObject(Preference.KEY_USER_SMOKING, String::class.java) ?: ""
        tvSmoking.text = smoking.replace("_", " ")

        val drinking = prefs.getObject(Preference.KEY_USER_DRINKING, String::class.java) ?: ""
        tvDrinking.text = drinking.replace("_", " ")

        val zodiac = prefs.getObject(Preference.KEY_USER_ZODIAC, String::class.java) ?: ""
        tvZodiac.text = zodiac.replace("_", " ")

        etEducation.setText(prefs.getObject(Preference.KEY_USER_EDUCATION, String::class.java) ?: "")
        etAbout.setText(prefs.getObject(Preference.KEY_USER_ABOUT, String::class.java) ?: "")
        tvWork.text = prefs.getObject(Preference.KEY_USER_WORK, String::class.java) ?: ""
        setISpeak()
    }

    private fun setupList() {
        PhotoHolderMetadata.edit = true
        gridview.layoutManager= GridLayoutManager(this@EditProfileActivity, 3)
        adapter = PhotoAdapter(this, R.layout.item_profile_photo, PhotoHolder::class.java, arrayListOf(), true, setClickListener())

        gridview.adapter = adapter

        val helper = DragHelper(adapter)
        val touchHelper = ItemTouchHelper(helper)
        touchHelper.attachToRecyclerView(gridview)

        if (UserMetadata.photos.size >= 8) {
            UserMetadata.photos.removeAt(8)
            UserMetadata.photos.removeAt(7)
            UserMetadata.photos.removeAt(6)
        }

        UserMetadata.photos.forEach {
            Timber.e("imgggg 1 = ${it.id}, ${it.url}")
        }

        val num = 6 - UserMetadata.photos.size
        Timber.e("NUM = $num")
        for (i in 1..num) {
            Timber.e("nnn = $i")
//            if (UserMetadata.photos.size-1>i) {
            UserMetadata.photos.add(Image(0,""))
//            }
        }
        Timber.e("ss = ${UserMetadata.photos.size}")
        UserMetadata.photos.forEach {
            Timber.e("imgggg 2 = ${it.id}, ${it.url}")
        }
//        UserMetadata.photos.set(0, UserMetadata..mainPhoto)
//        adapter.updateList(UserMetadata.photos)
        adapter.updateList(UserMetadata.photos)
    }

    private fun setClickListener(): SRListener<Image> {
        return object : SRListener<Image> {
            override fun onItemClick(pos: Int, item: Image, type: Int) {
                Timber.e("Click on photo, pos = $pos, item = $item, type = $type")
                if (item.url.isEmpty()) {
                    openPhotoMenu()
                }
                else {
                    deletePhoto(pos)
                }
            }
        }
    }

    private fun openPhotoMenu() {
        val actions = resources.getStringArray(R.array.profile_photo_main_menu)
        val dialog = AlertDialog.Builder(this)
            .setItems(actions) { dialogInterface, selectedIndex ->
                when (selectedIndex) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
                dialogInterface.dismiss()
            }
            .create()
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                LANGUAGES_REQUEST -> {
                    setISpeak()
                }
                GALLERY_REQUEST -> {
                    // create file
                    outputFileUri = Uri.fromFile(createFile())

                    val inputStream = contentResolver.openInputStream(data?.data)
                    val fileOutputStream = FileOutputStream(outputFileUri.path)
                    IOUtils.copyStream(inputStream, fileOutputStream)
                    fileOutputStream.close()
                    inputStream.close()

                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, outputFileUri)
                        if (bitmap != null) {
                            val gotoCropImage = Intent(this@EditProfileActivity, CropImageActivity::class.java)
                            gotoCropImage.data = outputFileUri
                            startActivityForResult(gotoCropImage, CROP_REQUEST)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                CAMERA_REQUEST -> {
                    val name = "IMG_" + System.currentTimeMillis() + ".JPG"
                    val file = File(cacheDir, name)

                    val maxBufferSize = 1 * 1024 * 1024

                    try {
                        val inputStream = contentResolver.openInputStream(outputFileUri)
                        val bytesAvailable = inputStream.available()
                        val bufferSize = Math.min(bytesAvailable, maxBufferSize)
                        val buffers = ByteArray(bufferSize)

                        val outputStream = FileOutputStream(file)

                        var bytesRead: Int
                        while(inputStream.read(buffers).also { bytesRead = it } >=0) {
                            outputStream.write(buffers, 0, bytesRead)
                        }
                        inputStream.close()
                        outputStream.close()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val gotoCropImage = Intent(this@EditProfileActivity, CropImageActivity::class.java)
                    gotoCropImage.data = Uri.fromFile(file)
                    startActivityForResult(gotoCropImage, CROP_REQUEST)
                }
                CROP_REQUEST -> {
                    data?.data?.path?.let {
                        Timber.e("path = $it")
                        val index = UserMetadata.getIndexToInsert()
                        UserMetadata.photos.set(index, Image(0,it))
                        adapter.updateObject(index, Image((100..10000).random(), it))

                        // update images via api
                        presenter.addPhotos()
                    }
                }
            }
        }
    }

    private fun deletePhoto(pos: Int) {
        alert("Do you want to delete this photo?") {
            isCancelable = true
            positiveButton("Delete") {
                presenter.deletePhotos(pos)
                adapter.removeObject(pos)
                adapter.addObject(UserMetadata.photos.size, Image(0,""))
            }
            negativeButton("Cancel") { it.dismiss() }
        }.show()
    }


    private fun openGallery() {
        var grantedPermCount = 0

        RxPermissions(this)
            .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe ({ permission ->
                if (permission.granted) {
                    grantedPermCount++
                    if (grantedPermCount == 2) {
                        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(i, GALLERY_REQUEST)
                    }
                }
                else if (permission.shouldShowRequestPermissionRationale) {
                    alert("Чтобы продолжить необходимо разрешить доступ к хранилищу") {
                        isCancelable = false
                        positiveButton("OK") { }
                    }.show()
                }
            }, {

            }).addTo(getDestroyDisposable())
    }

    private lateinit var outputFileUri: Uri

    private fun openCamera() {
        //to avoid exposed beyond app through ClipData.Item.getUri()
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        var grantedPermCount = 0
        var isShowAlready = false

        RxPermissions(this)
            .requestEach(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe ({ permission ->
                if (permission.granted) {
                    grantedPermCount++
                    if (grantedPermCount == 2) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (cameraIntent.resolveActivity(packageManager) != null) {
                            // create file
                            outputFileUri = Uri.fromFile(createFile())

                            cameraIntent.action = MediaStore.ACTION_IMAGE_CAPTURE
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                            this@EditProfileActivity.startActivityForResult(cameraIntent, CAMERA_REQUEST)
                        }
                    }
                }
                else if (permission.shouldShowRequestPermissionRationale) {
                    if (isShowAlready.not()) {
                        isShowAlready = true
                        alert("Чтобы продолжить необходимо разрешить доступ к камере и фото") {
                            isCancelable = false
                            positiveButton("OK") { isShowAlready = false }
                        }.show()
                    }
                }
            }, {
                Timber.e("onError = ${it.message}")
                Toast.makeText(this@EditProfileActivity, "error ${it.message}", Toast.LENGTH_LONG).show()
            }).addTo(getDestroyDisposable())
    }

    private fun createFile(): File {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + File.separator + "DefaultFolderrr")
        if (!myDir.exists())
            myDir.mkdirs()

        val newfile = File(myDir, "IMG_" + System.currentTimeMillis() + ".JPG")
        try {
            newfile.createNewFile()
        } catch (e: IOException) { }
        return newfile
    }
}
