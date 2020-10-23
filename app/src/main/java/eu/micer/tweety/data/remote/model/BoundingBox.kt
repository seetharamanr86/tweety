package eu.micer.tweety.data.remote.model

import com.google.gson.annotations.SerializedName

data class BoundingBox(
    @SerializedName("coordinates")
    val coordinates: List<List<Any>>,
    @SerializedName("type")
    val type: String
)
