package com.andyshon.tiktalk.ui.zones

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.andyshon.tiktalk.utils.extensions.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.app_toolbar_zone_list.*
import kotlinx.android.synthetic.main.fragment_zone_list.*
import timber.log.Timber
import javax.inject.Inject

class ZoneListFragment: BaseInjectFragment(), ZoneListContract.View {

    @Inject lateinit var presenter: ZoneListPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: ZoneListAdapter? = null

    private var listener: ZoneListListener? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_zone_list, container, false)
    }

    /**
     *
     * when user get nearest places - go through all and create channels if there aren't channels with that name
     * notice: name of the place is the friendly name for the channel
     * find out how name of the place returns by places api depends on user's device languages
     *
     * when user should be
     *
     * MessagesPresenter check friendlyName vs uniqueName line 50
     *
     *
     * */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)

        initListeners()
        setupToolbar()
        setupList()

        getPlaces()
    }

    private fun getPlaces() {
        prefs.putObject(Preference.KEY_USER_LAST_LATITUDE, "47.81981981981982", String::class.java)
        prefs.putObject(Preference.KEY_USER_LAST_LONGTITUDE, "35.04509757322189", String::class.java)
        val lastLocationLat = prefs.getObject(Preference.KEY_USER_LAST_LATITUDE, String::class.java)
        val lastLocationLng = prefs.getObject(Preference.KEY_USER_LAST_LONGTITUDE, String::class.java)
        Timber.e("lastLocationLat 1 = $lastLocationLat")
        Timber.e("lastLocationLng 1 = $lastLocationLng")

        lastLocationLat?.let {
//            presenter.getPlaces(lastLocationLat.plus(",").plus(lastLocationLng), 1000)
//            presenter.getPlaces("37.77657,-122.41750", 1000)
        }

        // Create the LocationRequest object
//        val mLocationRequest = LocationRequest.create()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
//            .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivityContext())
        RxPermissions(getActivityContext())
            .request(Manifest.permission.ACCESS_COARSE_LOCATION)
            .subscribe({
                if (it) {
                    if (ContextCompat.checkSelfPermission(getActivityContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                            Timber.e("Location = $lastLocationLat -> ${location.latitude}, $lastLocationLng -> ${location.longitude}")
                            Timber.e("lastLocationLng 4 = $location")

                            if (location != null) {
                                if (lastLocationLat?.toDouble()?:0 != location.latitude && lastLocationLng?.toDouble()?:0 != location.longitude) {
                                    prefs.putObject(Preference.KEY_USER_LAST_LATITUDE, location.latitude.toString(), String::class.java)
                                    prefs.putObject(Preference.KEY_USER_LAST_LONGTITUDE, location.longitude.toString(), String::class.java)

                                    presenter.getPlaces(location.latitude.toString().plus(",").plus(location.longitude), 1000)
                                }
                            }
                            else {
                                lastLocationLat?.let {
                                    presenter.getPlaces(lastLocationLat.plus(",").plus(lastLocationLng), 1000)
                                }
                            }
                        }
                    }
                }
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(getDestroyDisposable())
    }

    private fun setupToolbar() {
        toolbarUserAvatar.loadRoundCornersImage(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_100),
            url = UserMetadata.photos.first().url
        )
    }

    private fun initListeners() {
        btnRefresh.setOnClickListener {
            getPlaces()
        }
        toolbarUserAvatar.setOnClickListener {
            listener?.openSettings()
        }
    }

    private fun setupList() {
        zoneListRecyclerView?.let { it.layoutManager = LinearLayoutManager(getActivityContext()) }
        adapter = ZoneListAdapter(presenter.places, setClickListener())
        zoneListRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemClickListener<PlacesResult> {
        return object : ItemClickListener<PlacesResult> {
            override fun onItemClick(view: View, pos: Int, item: PlacesResult) {
                presenter.openPlace(item)
            }
        }
    }

    override fun showPlaces() {
        adapter?.notifyDataSetChanged()
    }

    override fun updatePlaceUsersCount(pos: Int, count: Int) {
        adapter?.updateUsersCount(pos, count)
    }

    override fun openSingleZone(place: PlacesResult) {
        listener?.openSingleZone(place)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ZoneListListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ZoneListListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}