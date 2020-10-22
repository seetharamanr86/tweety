package eu.micer.tweety.network.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("bounding_box")
    val boundingBox: BoundingBox,
    @SerializedName("country")
    val country: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("place_type")
    val placeType: String,
    @SerializedName("url")
    val url: String
)