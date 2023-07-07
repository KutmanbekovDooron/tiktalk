package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

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
import android.widget.Toast
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
import com.google.android.gms.common.util.IOUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_profile_add_photos.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import org.jetbrains.anko.alert
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

private const val CAMERA_REQUEST = 666
private const val GALLERY_REQUEST = 777
private const val CROP_REQUEST = 567

class ProfileAddPhotosActivity : BaseInjectActivity(), AddPhotosContract.View {

    companion object {
        fun startActivity(context: Context, email: String, phone: String, codeCountry: String, name: String, dob: String, location: String, gender: String) {
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

    private lateinit var adapter: PhotoAdapter

    @Inject lateinit var presenter: AddPhotosPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    private var email = ""
    private var phone = ""
    private var codeCountry = ""
    private var name = ""
    private var dob = ""
    private var location = ""
    private var gender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_add_photos)


        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        codeCountry = intent.getStringExtra("codeCountry") ?: ""
        name = intent.getStringExtra("name") ?: ""
        dob = intent.getStringExtra("dob") ?: ""
        location = intent.getStringExtra("location") ?: ""
        gender = intent.getStringExtra("gender") ?: ""

        tvToolbarTitle.text = this string R.string.create_profile_title
        initListeners()
        setupList()
    }

    private fun setupList() {
        PhotoHolderMetadata.edit = false
        gridview.layoutManager= GridLayoutManager(this@ProfileAddPhotosActivity, 3)
        adapter = PhotoAdapter(this, R.layout.item_profile_photo, PhotoHolder::class.java, UserMetadata.photos, true, setClickListener())

        gridview.adapter = adapter

        val helper = DragHelper(adapter)
        val touchHelper = ItemTouchHelper(helper)
        touchHelper.attachToRecyclerView(gridview)

//        adapter.updateList(UserMetadata.photos)
    }

    private fun setClickListener(): SRListener<Image> {
        return object : SRListener<Image> {
            override fun onItemClick(pos: Int, item: Image, type: Int) {
                Timber.e("Click on photo $item, type = $type")
                if (item.url.isEmpty()) {
                    openSmallPhotoMenu()
                }
                else {
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
                    0 -> openCamera()
                    1 -> openGallery()
                }
                dialogInterface.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun initListeners() {
        btnToolbarBack.setOnClickListener { finish() }
        btnDone.setOnClickListener {
            Timber.e("email = $email, phone = $phone, codeCountry = $codeCountry, name = $name, dob = $dob, location = $location, gender = $gender")
            if (canOpen()) {
                presenter.register(email, phone, codeCountry, name, dob, location, location, gender)
            }
        }
    }

    private fun checkIfCanDone() {
        btnDone.isEnabled = UserMetadata.hasAtLeastOnePhoto()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST -> {
                    // create file
                    outputFileUri = Uri.fromFile(createFile())

                    val inputStream = contentResolver.openInputStream(data?.data!!)!!
                    val fileOutputStream = FileOutputStream(outputFileUri.path)
                    IOUtils.copyStream(inputStream, fileOutputStream)
                    fileOutputStream.close()
                    inputStream.close()

                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, outputFileUri)
                        if (bitmap != null) {
                            val gotoCropImage = Intent(this@ProfileAddPhotosActivity, CropImageActivity::class.java)
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
                        val inputStream = contentResolver.openInputStream(outputFileUri)!!
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

                    val gotoCropImage = Intent(this@ProfileAddPhotosActivity, CropImageActivity::class.java)
                    gotoCropImage.data = Uri.fromFile(file)
                    startActivityForResult(gotoCropImage, CROP_REQUEST)
                }
                CROP_REQUEST -> {
                    data?.data?.path?.let {
                        Timber.e("path = $it")
                        val index = UserMetadata.getIndexToInsert()
                        UserMetadata.photos.set(index, Image(0,it))
                        adapter.updateObject(index, Image((100..10000).random(), it))
                        checkIfCanDone()
                    }
//                    data?.data?.path?.let { uploadPhoto(it) }
                }
            }
        }
    }

    private fun deletePhoto(pos: Int) {
        alert("Do you want to delete this photo?") {
            isCancelable = true
            positiveButton("Delete") {
                adapter.removeObject(pos)
                adapter.addObject(UserMetadata.photos.size, Image(0,""))
                adapter.notifyDataSetChanged()
                checkIfCanDone()
            }
            negativeButton("Cancel") { it.dismiss() }
        }.show()
    }


    private fun openGallery() {
        var grantedPermCount = 0

        RxPermissions(this)
//            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri)
                            this@ProfileAddPhotosActivity.startActivityForResult(cameraIntent, CAMERA_REQUEST)
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
                Toast.makeText(this@ProfileAddPhotosActivity, "error ${it.message}", Toast.LENGTH_LONG).show()
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

    override fun onPhotosLoaded() {

    }

    override fun onRegistered() {
        val intent = Intent(this@ProfileAddPhotosActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
