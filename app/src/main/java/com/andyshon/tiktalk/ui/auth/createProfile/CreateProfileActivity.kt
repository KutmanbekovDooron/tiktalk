package com.andyshon.tiktalk.ui.auth.createProfile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Patterns
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.auth.createProfile.addPhotos.ProfileAddPhotosActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.string
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_profile.*
import kotlinx.android.synthetic.main.app_toolbar_title.*
import javax.inject.Inject
import timber.log.Timber
import androidx.core.content.res.ResourcesCompat
import com.andyshon.tiktalk.BuildConfig
import com.andyshon.tiktalk.ui.dialogs.DatePickerFragmentDialog
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.jetbrains.annotations.NotNull
import java.util.*

class CreateProfileActivity : BaseInjectActivity(), CreateProfileContract.View, DatePickerFragmentDialog.DatePickerListener {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, CreateProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: CreateProfilePresenter

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val isFieldsValid
        get() = isNameValid
                && isEmailValid
                && isEmailUnique
                && isDateOfBirthValid
                && isLocationValid

    private var isNameValid = false
    private var isDateOfBirthValid = false
    private var isEmailValid = false
    private var isEmailUnique = false
    private var isLocationValid = true

    private var location = "Bishkek"
    private lateinit var etPlace: EditText


    override fun onPause() {
        super.onPause()
        etEmail.clearFocus()
        etName.clearFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        tvToolbarTitle.text = this string R.string.create_profile_title
        initListeners()

        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        val btnClear = autocompleteFragment.view?.findViewById(R.id.places_autocomplete_clear_button) as ImageView
        btnClear.setOnClickListener {
            etPlace.setText("Bishkek")
            isLocationValid = false
            checkIfCanCreateProfile()
        }

        etPlace = autocompleteFragment.view?.findViewById(R.id.places_autocomplete_search_input) as EditText
        etPlace.hint = "Tap to choose"

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorTextGreyDarkOrWhite, typedValue, true)
        @ColorInt val color = typedValue.data
        etPlace.setHintTextColor(color)
    }

    private fun validateAll(): Boolean {
        var errors = 0
        if (etName.text.toString().trim().isEmpty() || etName.text.length < 2) {
            (etName.parent.parent as TextInputLayout).error = "Name should be at least 2 characters"
            errors++
        }
        if (isDateOfBirthValid.not()) {
            (etDateOfBirth.parent.parent as TextInputLayout).error = "You need to be at least 16 years old to use this app"
            errors++
        }
        if (Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches().not()) {
            (etEmail.parent.parent as TextInputLayout).error = "Invalid email"
            errors++
        }
        if (location.isEmpty()) {
            etPlace.hint = "Invalid location"
            etPlace.setHintTextColor(Color.RED)
            errors++
        }
        return errors <= 0
    }

    override fun emailFree() {
        isEmailUnique = true
        (etEmail.parent.parent as TextInputLayout).isErrorEnabled = false
        (etEmail.parent.parent as TextInputLayout).error = null
        checkIfCanCreateProfile()
    }

    override fun emailTaken() {
        isEmailUnique = false
        (etEmail.parent.parent as TextInputLayout).isErrorEnabled = true
        (etEmail.parent.parent as TextInputLayout).error = "Email is already taken"
        checkIfCanCreateProfile()
    }

    private fun initListeners() {
        btnToolbarBack.setOnClickListener { finish() }
        switcherGender.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                val typeface = ResourcesCompat.getFont(this, R.font.roboto_regular)
                tvMale.setTypeface(typeface, Typeface.NORMAL)
                tvFemale.setTypeface(tvMale.typeface, Typeface.BOLD)
            } else {
                tvMale.setTypeface(tvMale.typeface, Typeface.BOLD)
                val typeface = ResourcesCompat.getFont(this, R.font.roboto_regular)
                tvFemale.setTypeface(typeface, Typeface.NORMAL)
            }
        }
        etDateOfBirth.setOnClickListener {
            etEmail.clearFocus()
            etName.clearFocus()
            val dialog = DatePickerFragmentDialog()
            showPickerDialog(dialog)
        }
        etEmail.setOnFocusChangeListener { view, b ->
            if (b.not()) {
                etEmail.clearFocus()
                checkIfCanCreateProfile()
            }
        }
        etName.setOnFocusChangeListener { view, b ->
            if (b.not()) {
                etName.clearFocus()
                checkIfCanCreateProfile()
            }
        }
        etEmail.doAfterTextChanged {
            isEmailValid = Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches()
            if (isEmailValid) {
                (etEmail.parent.parent as TextInputLayout).isErrorEnabled = false
                (etEmail.parent.parent as TextInputLayout).error = null
                presenter.checkIfEmailExists(etEmail.text.toString().trim())
            }
            else {
                (etEmail.parent.parent as TextInputLayout).isErrorEnabled = true
                (etEmail.parent.parent as TextInputLayout).error = "Invalid email"
            }
            checkIfCanCreateProfile()
        }
        etName.doAfterTextChanged {
            isNameValid = etName.text.toString().trim().isNotEmpty() && etName.text.length >= 2
            if (isNameValid) {
                (etName.parent.parent as TextInputLayout).isErrorEnabled = false
                (etName.parent.parent as TextInputLayout).error = null
            }
            else {
                (etName.parent.parent as TextInputLayout).isErrorEnabled = true
                (etName.parent.parent as TextInputLayout).error = "Name should be at least 2 characters"
            }
            checkIfCanCreateProfile()
        }
        btnNext.setOnClickListener {
            if (canOpen().not()) return@setOnClickListener

            if (validateAll()) {
                val gender = if (switcherGender.isChecked) "female" else "male"
                val phoneNumber = presenter.getUserPhoneNumber()
                val codeCountry = if (presenter.getUserCodeCountry().isEmpty()) "+380" else presenter.getUserCodeCountry()
                ProfileAddPhotosActivity.startActivity(
                    this,
                    etEmail.text.toString().trim(),
                    phoneNumber,
                    codeCountry,
                    etName.text.toString().trim(),
                    etDateOfBirth.text.toString().trim(),
                    //etLocation.text.toString().trim(),
                    location,
                    gender
                )
            }
        }

        // https://maps.googleapis.com/maps/api/place/nearbysearch/json?rankby=distance&location=37.77657,-122.417506&rankBy=50000&types=grocery_or_supermarket&sensor=true&key=AIzaSyDDaFXm_E2Mr1hMIizCC465p9pL6DqFhWk

    //  https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=37.77657,-122.41750&radius=1000&sensor=true&types=hospital|health&key=AIzaSyDDaFXm_E2Mr1hMIizCC465p9pL6DqFhWk


        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.PLACES_KEY)
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = (supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?)!!
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
        autocompleteFragment.setOnPlaceSelectedListener(placeSelectionListener)
    }

    private var placeSelectionListener: PlaceSelectionListener = object : PlaceSelectionListener {
        override fun onPlaceSelected(@NonNull place: Place) {
            Timber.e("Place = ${place.name}, ${place.id}, ${place.address}, ${place.attributions}, ${place.latLng}, ${place.openingHours}, ${place.phoneNumber}, ${place.photoMetadatas}, ${place.plusCode}, ${place.priceLevel}")

            location = place.name ?: place.address ?: ""
            isLocationValid = location.isNotEmpty()
            checkIfCanCreateProfile()

            etPlace.setText(place.name)

            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorTextGreyDarkOrWhite, typedValue, true)
            @ColorInt val color = typedValue.data
            etPlace.setTextColor(color)
        }
        override fun onError(@NotNull status: Status) {
            Timber.e("An error occurred: $status")
            checkIfCanCreateProfile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("resultCode = $resultCode, requestCode = $requestCode, data = $data")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPickerDialog(dialog: DialogFragment) {
        dialog.show(supportFragmentManager, "")
        if (etDateOfBirth.text.isNotEmpty()) {
            (dialog as DatePickerFragmentDialog).setDateFromString(etDateOfBirth.text.toString().trim())
        }
    }

    override fun onDatePicked(year: Int, month: Int, day: Int) {
        etDateOfBirth.setText("$day-${month + 1}-$year")
        isDateOfBirthValid = presenter.validateDOB(year, month + 1, day, etDateOfBirth)
        if (isDateOfBirthValid) {
            (etDateOfBirth.parent.parent as TextInputLayout).error = null
            (etDateOfBirth.parent.parent as TextInputLayout).isErrorEnabled = false
        }
        checkIfCanCreateProfile()
    }

    private fun checkIfCanCreateProfile() {
        btnNext.isEnabled = isFieldsValid
    }

    override fun onUserParamsVerified() {
//        ProfileAddPhotosActivity.startActivity(this)
    }

}
