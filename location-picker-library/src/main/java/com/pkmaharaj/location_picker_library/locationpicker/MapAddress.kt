package com.pkmaharaj.location_picker_library.locationpicker

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MapAddress(
   @field:SerializedName("Address")
   var Address: String? = "",
   @field:SerializedName("City")
   var City: String? = "",
   @field:SerializedName("State")
   var State: String? = "",
   @field:SerializedName("Country")
   var Country: String? = "",
   @field:SerializedName("Postalcode")
   var Postalcode: String? = "",
   @field:SerializedName("Knownname")
   var Knownname: String? = "",
   @field:SerializedName("Latitude")
   var Latitude: Double? = 0.0,
   @field:SerializedName("Longitude")
   var Longitude: Double? = 0.0,
   ): Serializable


