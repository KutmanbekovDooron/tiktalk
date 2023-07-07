package com.andyshon.tiktalk.ui.zoneSingle

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.os.StrictMode
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import kotlinx.android.synthetic.main.activity_zone_single.*
import kotlinx.android.synthetic.main.app_toolbar_zone_single.*
import javax.inject.Inject
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.data.services.MediaService
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.chatSingle.ChatSingleActivity
import com.andyshon.tiktalk.ui.dialogs.AttachFileDialog
import com.andyshon.tiktalk.ui.viewContact.ViewContactActivity
import com.andyshon.tiktalk.ui.zoneSingle.privateRoom.ZonePrivateFragment
import com.andyshon.tiktalk.ui.zoneSingle.publicRoom.ZonePublicRoomFragment
import com.andyshon.tiktalk.utils.extensions.hide
import com.andyshon.tiktalk.utils.extensions.show
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.chat.CallbackListener
import com.twilio.chat.Channel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.app_toolbar_chat_single_tap.*
import org.jetbrains.anko.startActivity
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import org.jetbrains.anko.*
import java.io.File

private const val CAMERA_REQUEST = 101
private const val GALLERY_REQUEST = 102
private const val FILE_REQUEST = 103
private const val MUSIC_REQUEST = 104
private const val VIDEO_REQUEST = 105
private const val RC_SELECT_CONTACT = 111

class ZoneSingleActivity : BaseInjectActivity(), ZoneSingleContract.View, ZoneSingleListener, AttachFileDialog.AttachFileClickListener {

    companion object {
        fun startActivity(context: Context, place: PlacesResult) {
            val intent = Intent(context, ZoneSingleActivity::class.java).apply {
                putExtra("placeId", place.placeId)
                putExtra("name", place.name)
                putExtra("usersCount", place.usersCount)
            }
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var presenter: ZoneSinglePresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var rxEventBus: RxEventBus

    private var placeId = ""
    private var placeName = ""
    private var userCount = 0

    private lateinit var fragmentPublicRoom: ZonePublicRoomFragment
    private lateinit var fragmentPrivateRoom: ZonePrivateFragment

    private lateinit var outputFileUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
//        this.window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        setContentView(R.layout.activity_zone_single)

        intent?.let {
            placeId = it.getStringExtra("placeId")
            placeName = it.getStringExtra("name")
            userCount = it.getIntExtra("usersCount", 0)
            tvZoneName.text = placeName.plus(" (").plus(userCount).plus(")")

            presenter.getPlace(placeName.plus("_").plus(placeId))
        }

        fragmentPublicRoom = ZonePublicRoomFragment()
        fragmentPrivateRoom = ZonePrivateFragment()

        initListeners()
        setupViewPager()
        presenter.observe()
//        tabs.setupWithViewPager(singleZoneViewPager)
    }

    fun getChannel(): Channel? = presenter.channel

    private fun initListeners() {
        toolbarZoneSingleBtnBack.setOnClickListener {
            finish()
        }
        btnUsersDropdown.setOnClickListener {
            showUsersDropdown()
        }
        toolbarChatSingleBtnBack.setOnClickListener {
            fragmentPublicRoom.pressedBackSelected()
        }
    }

    private fun showUsersDropdown() {
        val bottomSheetFragment = UsersDropdownBSDFragment()
        bottomSheetFragment.rxEventBus = rxEventBus
        bottomSheetFragment.users = presenter.users
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(fragmentPublicRoom, "Public room")
        adapter.addFragment(fragmentPrivateRoom, "Private rooms")
        singleZoneViewPager.adapter = adapter

        singleZoneViewPager.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                Timber.e("position = $position")
                when(position) {
                    0 -> {

                    }
                    else -> {
                        fragmentPrivateRoom.show()
                    }
                }
            }
        })
    }

    override fun setToolbarStateSimple() {
        toolbarChatSingle.show()
        toolbarChatSingleTap.hide()
    }

    override fun setToolbarStateTapped() {
        toolbarChatSingle.hide()
        toolbarChatSingleTap.show()
    }

    override fun setToolbartNumberCounter(n: Int) {
        tvChatSingleNumberCounter.text = n.toString()
    }

    // get place & set members count
    override fun setMembersCount(members: Int) {
        userCount = members
        tvZoneName.text = placeName.plus(" (").plus(userCount).plus(")")

        fragmentPrivateRoom.setUsers(presenter.users)

        setupViewPager()
        tabs.setupWithViewPager(singleZoneViewPager)
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = arrayListOf<Fragment>()
        private val mFragmentTitleList = arrayListOf<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList.get(position)
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    override fun openSingleChat(item: ChannelModel) {
        val userName = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userName2
        else item.userData?.userName1
        val userPhoto = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhoto2
        else item.userData?.userPhoto1
        val userPhone = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhone2
        else item.userData?.userPhone1

        item.getChannel(object: CallbackListener<Channel>(){
            override fun onSuccess(channel: Channel?) {
                channel?.let {
                    startActivity<ChatSingleActivity>(
                        Constants.EXTRA_CHANNEL to it,
                        Constants.EXTRA_CHANNEL_SID to it.sid,
                        Constants.EXTRA_CHANNEL_OPPONENT_NAME to userName,
                        Constants.EXTRA_CHANNEL_OPPONENT_PHOTO to userPhoto,
                        Constants.EXTRA_CHANNEL_OPPONENT_PHONE to userPhone
                    )
                }
            }
        })
    }

    override fun openUserProfile(name: String, photo: String, phone: String) {
        startActivity<ViewContactActivity>(
            Constants.EXTRA_CHANNEL to presenter.channel,
            Constants.EXTRA_CHANNEL_NAME to name,
            Constants.EXTRA_CHANNEL_PHOTO to photo,
            Constants.EXTRA_CHANNEL_PHONE to phone
        )
    }


























    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("onActivityResult, Path = ${data?.data?.path}, ${data?.data?.toString()}")

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_SELECT_CONTACT -> {
                    data?.let {
                        val contactName = it.getStringExtra("name")
                        val contactPhone = it.getStringExtra("phone")
//                        val contactPhoto = it.getParcelableExtra<Bitmap>("photo")
//                        presenter.sendContactMessage(contactName, contactPhone/*, contactPhoto*/)
                    }
                }
                GALLERY_REQUEST -> {
                    Timber.e("Path = ${data?.data?.path}, ${data?.data?.toString()}")

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data?.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_IMAGE
                    )
                }
                CAMERA_REQUEST -> {
                    Timber.e("CAMERA_REQUEST, outputFileUri = $outputFileUri")
                    val name = "IMG_" + System.currentTimeMillis() + ".JPG"
                    val file = File(cacheDir, name)

                    val maxBufferSize = 1 * 1024 * 1024

                    try {
                        val fos = FileOutputStream(file.path)
                        var realImage = MediaStore.Images.Media.getBitmap(contentResolver, outputFileUri)

                        Timber.e("realImage width 1 = ${realImage.width}, ${realImage.height}")
                        //E/ChatSingleActivity: realImage width = 4032, 3024

                        val exif = ExifInterface(file.path)

                        Timber.e("EXIF orientation = ${exif.getAttribute(ExifInterface.TAG_ORIENTATION)}")

                        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("6", ignoreCase = true)) {
                            realImage = rotate(realImage, 90)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("8", ignoreCase = true)) {
                            realImage = rotate(realImage, 270)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("3", ignoreCase = true)) {
                            realImage = rotate(realImage, 180)
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equals("0", ignoreCase = true)) {
                            realImage = rotate(realImage, 90)
                        }

                        Timber.e("realImage width 2 = ${realImage.width}, ${realImage.height}")
                        //E/ChatSingleActivity: realImage width 2 = 3024, 4032

                        val bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        Timber.e("BOO = $bo")
                        fos.close()

                        val uri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                contentResolver,
                                file.path,
                                null,
                                null
                            )
                        )

                        val inputStream = contentResolver.openInputStream(uri)
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

                        Timber.e("File stored in cache dir, $file, name: $name")
                        Timber.e("File stored in cache dir, path = ${Uri.parse(file.path)}")

                        Timber.e("Uriiii = $uri")



                        startService<MediaService>(
                            MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                            MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                            MediaService.EXTRA_MEDIA_URI to uri.toString(),
                            MediaService.EXTRA_MEDIA_URI_TO_DELETE to name,
                            MediaService.EXTRA_MEDIA_FILE_NAME to name,
                            MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_CAMERA   // for camera use TYPE_FILE
                        )

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                FILE_REQUEST -> {
                    Timber.e("FILE_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${data?.data?.path?.split("/")?.last()}")
                    //    FILE_REQUEST, Path = /storage/emulated/0/Download/13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf,
                    //    file:///storage/emulated/0/Download/13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf.
                    //    fileName = 13.07.2019zacz6kp0TkOattyJcAEmUQ.pdf


                    MediaScannerConnection.scanFile(this, arrayOf(data?.data?.path), null) { p0, uri ->
                        Timber.e("onScanCompleted, p0 = $p0, uri = $uri")

                        startService<MediaService>(
                            MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                            MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                            MediaService.EXTRA_MEDIA_URI to uri?.toString(),
                            MediaService.EXTRA_MEDIA_FILE_NAME to data?.data?.path?.split("/")?.last(),
                            MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_FILE
                        )
                    }
                }
                MUSIC_REQUEST -> {
                    Timber.e("MUSIC_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${data?.data?.path?.split("/")?.last()}")

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_MUSIC
                    )
                }
                VIDEO_REQUEST -> {
                    Timber.e("VIDEO_REQUEST, Path = ${data?.data?.path}, ${data?.data?.toString()}. fileName = ${data?.data?.path?.split("/")?.last()}")

                    startService<MediaService>(
                        MediaService.EXTRA_ACTION to MediaService.EXTRA_ACTION_UPLOAD,
                        MediaService.EXTRA_CHANNEL to presenter.channel as Parcelable,
                        MediaService.EXTRA_MEDIA_URI to data?.data.toString(),
                        MediaService.EXTRA_UPLOAD_DATA_TYPE to Constants.Chat.Media.TYPE_VIDEO
                    )
                }
            }
        }
    }






    private fun rotate(bitmap: Bitmap, degree: Int): Bitmap {
        val mtx = Matrix()
//        mtx.postRotate(degree.toFloat())
        mtx.setRotate(degree.toFloat())

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mtx, true)
    }





















    private fun openGallery() {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST)
    }

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
                            Timber.e("Create file, outputFileUri = $outputFileUri")

                            cameraIntent.action = MediaStore.ACTION_IMAGE_CAPTURE
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                            this@ZoneSingleActivity.startActivityForResult(cameraIntent, CAMERA_REQUEST)
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
                Toast.makeText(this@ZoneSingleActivity, "error ${it.message}", Toast.LENGTH_LONG).show()
            }).addTo(getDestroyDisposable())
    }

    private fun createFile(): File {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root)
        if (!myDir.exists())
            myDir.mkdirs()

        val newfile = File(myDir, "IMG___" + System.currentTimeMillis() + ".JPG")
//        val newfile = File(cacheDir, presenter.channel?.)
        try {
            newfile.createNewFile()
        } catch (e: IOException) { }
        return newfile
    }




    override fun galleryAttachDialog() {
        openGallery()
    }

    override fun cameraAttachDialog() {
        openCamera()
    }

    override fun videoAttachDialog() {
//        openVideoPicker()
    }

    override fun musicAttachDialog() {
//        openMusicPicker()
    }

    override fun fileAttachDialog() {
//        openFile()
    }

    override fun locationAttachDialog() {

    }

    override fun contactAttachDialog() {
//        openSelectContact()
    }


}
