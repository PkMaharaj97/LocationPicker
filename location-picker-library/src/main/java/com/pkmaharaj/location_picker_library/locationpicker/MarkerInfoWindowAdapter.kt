package com.pkmaharaj.location_picker_library.locationpicker
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.pkmaharaj.location_picker_library.R

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.marker_info_contents, null)
    private fun rendowWindowText(marker: Marker, view: View){

        val place = marker.tag as? SimplePlace
        val tvTitle = view.findViewById<TextView>(R.id.text_view_title)
        val tvSnippet = view.findViewById<TextView>(R.id.text_view_address)
        val tvSelect = view.findViewById<TextView>(R.id.text_view_rating)
        tvTitle.text = place?.name
        tvSnippet.text =  place?.address
        tvSelect.text =  "Select Address"
        tvSelect.setOnClickListener {
            Toast.makeText(context,"kbsdkjvbd",Toast.LENGTH_SHORT).show()
        }

    }

    override fun getInfoContents(marker: Marker): View? {
        rendowWindowText(marker, mWindow)
        return mWindow
    }


    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}

data class SimplePlace(
     var name:String,
     var address:String,
     var location:LatLng,
)





