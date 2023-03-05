package com.pkmaharaj.location_picker_library.locationpicker
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.pkmaharaj.location_picker_library.R
import com.pkmaharaj.location_picker_library.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var locationManager: LocationManager? = null
    private var location: Location? = null
    private var binding: ActivityMapsBinding? = null
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            drawMarker(location)
            locationManager!!.removeUpdates(this)
        }
        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
       val mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
       mapFragment.getMapAsync(this)
       initSearch()
        binding?.searchPlace?.setOnClickListener {
            onSearchCalled()
        }
    }


    fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN")
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                Log.i(TAG, "Place: " + place.name + ", " + place.id + ", " + place.address)
                addMarkers(place)
                // do query with address
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(
                    this@MapsActivity,
                    "Error: " + status.statusMessage,
                    Toast.LENGTH_LONG
                ).show()
                Log.i(TAG, status.statusMessage!!)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    private fun initSearch() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, Places_Api_Key)
        }
        Places.createClient(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        initMap()
        currentLocation

    }



    private fun drawMarker(location: Location) {
        if (mMap != null) {
            mMap.clear()
            val latLng = LatLng(location.latitude, location.longitude)
            val address=getAddress(location)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(address?.getAddressLine(0))
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            var marker= mMap.addMarker(markerOptions)!!
            marker.tag= SimplePlace("Current location",address?.getAddressLine(0)?:"",latLng)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            mMap.setOnInfoWindowClickListener {
                shareAddress(marker)
            }
            marker.showInfoWindow()

        }
    }

    private fun addMarkers(place:Place) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .position(place.latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                     mMap.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
                     mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            marker?.tag = SimplePlace(name = place.name, address = place.address,place.latLng)!!
        marker?.showInfoWindow()
        mMap.setOnInfoWindowClickListener {
            shareAddress(marker!!)
        }

    }
    fun getAddress(location: Location):Address?{
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        var addresses : List<Address>?=null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 3)
            Log.e("AddressLines","$addresses")

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses?.get(0)
    }
   public fun shareAddress(marker:Marker){
        var place=marker.tag as SimplePlace
       var placeAddress=if(place.address.contains(place.name,true)) place.address else (place.name+","+place.address)
        val mapAddress=MapAddress(
            Address =placeAddress,
            Longitude = place.location.longitude,
            Latitude = place.location.latitude)
        setResult(RESULT_OK, Intent().putExtra(LOCATION_SELECT_REQUEST,mapAddress))
        finish()
    }




    private fun initMap() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (mMap != null) {
                this.mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.uiSettings.setAllGesturesEnabled(true)
                mMap.uiSettings.isZoomControlsEnabled = true
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    12
                )
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    13
                )
            }
        }
    }

    private val currentLocation: Unit
        private get() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?:false
                val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)?:false
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(
                        applicationContext,
                        "Failed",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    location = null
                    if (isGPSEnabled) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            LOCATION_MIN_UPDATE_TIME.toLong(),
                            LOCATION_MIN_UPDATE_DISTANCE.toFloat(),
                            locationListener
                        )
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                    if (isNetworkEnabled) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                           LOCATION_MIN_UPDATE_TIME.toLong(),
                            LOCATION_MIN_UPDATE_DISTANCE.toFloat(),
                            locationListener
                        )
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                    if (location != null) {
                        drawMarker(location!!)
                    }
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        12
                    )
                }
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        13
                    )
                }
            }
        }
    companion object {
        const val TAG: String = "Location_Picker_ACTIVITY"
        private const val LOCATION_MIN_UPDATE_TIME = 10
        private const val LOCATION_MIN_UPDATE_DISTANCE = 1000
        private const val AUTOCOMPLETE_REQUEST_CODE = 100
        private const val LOCATION_SELECT_REQUEST = "LOCATION_SELECT_REQUEST"
        private  var Places_Api_Key = "LOCATION_SELECT_REQUEST"
        fun getMapWithKey(context: Context, bundle: Bundle?,PlaceApiKey:String): Intent {
            val destIntent = Intent(context, MapsActivity::class.java)
            Places_Api_Key=PlaceApiKey
            destIntent.putExtra("bundle", bundle)
            return destIntent
        }
    }
}