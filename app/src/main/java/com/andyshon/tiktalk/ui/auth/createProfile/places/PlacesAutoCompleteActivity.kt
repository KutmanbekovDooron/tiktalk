package com.andyshon.tiktalk.ui.auth.createProfile.places

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andyshon.tiktalk.R
import com.google.android.gms.common.api.Status
//import com.google.android.gms.location.places.Place
//import com.google.android.gms.location.places.ui.PlaceSelectionListener
//import com.google.android.gms.location.places.AutocompleteFilter
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import timber.log.Timber
//import com.google.android.gms.location.places.ui.PlaceAutocomplete
//import com.google.android.gms.location.places.AutocompletePrediction
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import javax.xml.datatype.DatatypeConstants.SECONDS
//import com.google.android.gms.location.places.AutocompletePredictionBuffer
//import com.google.android.gms.location.places.Places
import java.util.concurrent.TimeUnit
import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds

class PlacesAutoCompleteActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, PlacesAutoCompleteActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var mGoogleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_auto_complete)


//        mGoogleApiClient = GoogleApiClient.Builder(this)
//            .addApi(Places.GEO_DATA_API)
//            .addConnectionCallbacks(this)
//            .addOnConnectionFailedListener(this)
//            .build()

//        getAutocomplete("Ukr")



//        val autocompleteFragment =
//            fragmentManager.findFragmentById(R.id.autocomplete_fragment) as PlaceAutocompleteFragment

        /*
        * The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
        * set a filter returning only results with a precise address.
        */
        /*val typeFilter = AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
            .build()
        autocompleteFragment.setFilter(typeFilter)

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Timber.e("Place: ${place.name}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Timber.e("An error occurred: $status")
            }
        })*/
    }

    override fun onConnected(p0: Bundle?) {
        Timber.e("onConnected, $p0")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Timber.e("onConnectionFailed, ${p0.isSuccess}, ${p0.errorCode}, ${p0.errorMessage}")
    }

    override fun onConnectionSuspended(p0: Int) {
        Timber.e("onConnectionSuspended, $p0")
    }


    /*private fun getAutocomplete(constraint: CharSequence): ArrayList<PlaceAutocomplete>? {
        if (mGoogleApiClient != null) {
//            Log.i(FragmentActivity.TAG, "Starting autocomplete query for: $constraint")
            Timber.e("Starting autocomplete query for: $constraint")

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            val results = Places.GeoDataApi
                .getAutocompletePredictions(
                    mGoogleApiClient, constraint.toString(),
                    *//*mBounds*//*LatLngBounds(LatLng(3.0, 4.0), LatLng(10.0, 15.0)), *//*mPlaceFilter*//*null
                )

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            val autocompletePredictions = results
                .await(0, TimeUnit.SECONDS)

            // Confirm that the query completed successfully, otherwise return null
            val status = autocompletePredictions.status
            if (!status.isSuccess) {
                Toast.makeText(this, "Error contacting API: $status", Toast.LENGTH_SHORT).show()
                Timber.e("Error getting autocomplete prediction API call: $status")
                autocompletePredictions.release()
                return null
            }

            Timber.e("Query completed. Received ${autocompletePredictions.count} predictions.")

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).

            val iterator = autocompletePredictions.iterator()
            val resultList = ArrayList<PlaceAutocomplete>()
            while (iterator.hasNext()) {
                val prediction = iterator.next()
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
//                resultList.add(prediction)
//                resultList.add(
//                    PlaceAutocomplete(
//                        prediction.placeId,
//                        prediction.getDescription()
//                    )
//                )
                Timber.e("prediction = $prediction, ${prediction.placeId}")
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release()

            return resultList
        }
//        Log.e(FragmentActivity.TAG, "Google API client is not connected for autocomplete query.")
        return null
    }*/
}
