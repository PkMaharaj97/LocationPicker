package com.example.pickloc

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.pkmaharaj.location_picker_library.locationpicker.MapAddress
import com.pkmaharaj.location_picker_library.locationpicker.MapsActivity
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    val LOCATION_ADDRESS_REQUEST=101
    private  val LOCATION_SELECT_REQUEST = "LOCATION_SELECT_REQUEST"
private lateinit var locBtn:Button
private lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         locBtn=findViewById<Button>(R.id.btnLoc)
         textView=findViewById<TextView>(R.id.textView)
        locBtn.setOnClickListener {

            val destIntent= MapsActivity.getMapWithKey(this,null,this.resources.getString(R.string.api_key))
            startActivityForResult(destIntent,LOCATION_ADDRESS_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_ADDRESS_REQUEST && resultCode ==RESULT_OK) {
            data?.let {
                var data=it.getSerializableExtra(LOCATION_SELECT_REQUEST) as MapAddress
                val address=getAddress(data.Longitude?:0.0,data.Latitude?:0.0)
                data.City=address?.locality
                data.State=address?.adminArea
                data.Country=address?.countryName
                data.Postalcode=address?.postalCode
                data.Knownname=address?.featureName
                var string=StringBuilder()
                string.appendLine("Address: ${data.Address}")
                string.appendLine("Locality: ${data.Knownname}")
                string.appendLine("City: ${data.City}")
                string.appendLine("State: ${data.State}")
                string.appendLine("PostalCode: ${data.Postalcode}")
                textView.text=string.toString()
                   }

        }
    }
    fun getAddress(longitude:Double,latitude:Double): Address?{
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        var addresses : List<Address>?=null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            Log.e("AddressLines","$addresses")

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses?.get(0)
    }

}